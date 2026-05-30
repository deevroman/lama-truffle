package lama.truffle.parser;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.source.Source;
import lama.truffle.LamaLanguage;
import lama.truffle.ast.LamaAddNode;
import lama.truffle.ast.LamaAndNode;
import lama.truffle.ast.LamaArrayLiteralNode;
import lama.truffle.ast.LamaCallNode;
import lama.truffle.ast.LamaCaseNode;
import lama.truffle.ast.LamaComparisonNode;
import lama.truffle.ast.LamaComparisonNode.Kind;
import lama.truffle.ast.LamaConstantNode;
import lama.truffle.ast.LamaDivNode;
import lama.truffle.ast.LamaDoWhileNode;
import lama.truffle.ast.LamaExpressionNode;
import lama.truffle.ast.LamaForNode;
import lama.truffle.ast.LamaHeadNode;
import lama.truffle.ast.LamaIfNode;
import lama.truffle.ast.LamaLengthNode;
import lama.truffle.ast.LamaLetNode;
import lama.truffle.ast.LamaLoadArgumentNode;
import lama.truffle.ast.LamaLoadGlobalNode;
import lama.truffle.ast.LamaLoadIndexNode;
import lama.truffle.ast.LamaLoadLocalNode;
import lama.truffle.ast.LamaLoadUpvalueNode;
import lama.truffle.ast.LamaLongLiteralNode;
import lama.truffle.ast.LamaModuloNode;
import lama.truffle.ast.LamaMatchNode;
import lama.truffle.ast.LamaMulNode;
import lama.truffle.ast.LamaNegateNode;
import lama.truffle.ast.LamaNewClosureNode;
import lama.truffle.ast.LamaOrNode;
import lama.truffle.ast.LamaReadBuiltinNode;
import lama.truffle.ast.LamaRootNode;
import lama.truffle.ast.LamaSequenceNode;
import lama.truffle.ast.LamaSexpNode;
import lama.truffle.ast.LamaStoreGlobalNode;
import lama.truffle.ast.LamaStoreLocalNode;
import lama.truffle.ast.LamaStoreIndexNode;
import lama.truffle.ast.LamaStoreUpvalueNode;
import lama.truffle.ast.LamaSubNode;
import lama.truffle.ast.LamaStringLiteralNode;
import lama.truffle.ast.LamaStringifyNode;
import lama.truffle.ast.LamaTailNode;
import lama.truffle.ast.LamaUnimplementedNode;
import lama.truffle.ast.LamaWhileNode;
import lama.truffle.ast.LamaWriteBuiltinNode;
import lama.truffle.pattern.LamaArrayPattern;
import lama.truffle.pattern.LamaBindingPattern;
import lama.truffle.pattern.LamaLiteralLongPattern;
import lama.truffle.pattern.LamaPattern;
import lama.truffle.pattern.LamaSexpPattern;
import lama.truffle.pattern.LamaStringPattern;
import lama.truffle.pattern.LamaTypePattern;
import lama.truffle.pattern.LamaWildcardPattern;
import lama.truffle.parser.antlr.LamaBaseVisitor;
import lama.truffle.runtime.LamaFunction;
import lama.truffle.runtime.LamaCaptureSpec;
import lama.truffle.runtime.LamaFunctionTemplate;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LamaNodeParser {
    private LamaNodeParser() {
    }

    public static CallTarget parse(LamaLanguage language, Source source) {
        var lexer = new lama.truffle.parser.antlr.LamaLexer(CharStreams.fromString(source.getCharacters().toString()));
        var tokens = new CommonTokenStream(lexer);
        var parser = new lama.truffle.parser.antlr.LamaParser(tokens);
        var errorListener = new ThrowingErrorListener(source);

        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        var file = parser.file();
        var builder = new AstBuilder(language, source);
        return builder.build(file).getCallTarget();
    }

    private sealed interface Variable permits LocalVariable, UpvalueVariable, GlobalVariable {
    }

    private record LocalVariable(int slot) implements Variable {
    }

    private record UpvalueVariable(int slot) implements Variable {
    }

    private record GlobalVariable(String name) implements Variable {
    }

    private static class Scope {
        private final Scope parent;
        FunctionScope functionScope;
        private final Map<String, Variable> bindings = new HashMap<>();
        private final Map<String, Variable> infixBindings = new HashMap<>();

        private Scope(Scope parent) {
            this.parent = parent;
            this.functionScope = parent == null ? null : parent.functionScope;
        }
    }

    private static final class FunctionScope extends Scope {
        private final FrameDescriptor.Builder frameBuilder;
        private final Map<String, UpvalueVariable> capturesByName = new HashMap<>();
        private final List<Variable> captureSources = new ArrayList<>();

        private FunctionScope(Scope parent, FrameDescriptor.Builder frameBuilder, int depth) {
            super(parent);
            this.frameBuilder = frameBuilder;
            this.functionScope = this;
        }

        private UpvalueVariable capture(String name, Variable source) {
            return capturesByName.computeIfAbsent(name, ignored -> {
                var capture = new UpvalueVariable(captureSources.size());
                captureSources.add(source);
                return capture;
            });
        }
    }

    private static final class AstBuilder extends LamaBaseVisitor<LamaExpressionNode> {
        private final LamaLanguage language;
        private final Source source;
        private Scope scope;
        private final FunctionScope rootFunctionScope;

        private AstBuilder(LamaLanguage language, Source source) {
            this.language = language;
            this.source = source;
            this.rootFunctionScope = new FunctionScope(null, FrameDescriptor.newBuilder(), 0);
            this.scope = rootFunctionScope;
        }

        private LamaRootNode build(lama.truffle.parser.antlr.LamaParser.FileContext file) {
            LamaExpressionNode body = visitScopedScopeExpression(file.body);
            return new LamaRootNode(language, rootFunctionScope.frameBuilder.build(), body, source, source.getName());
        }

        @Override
        public LamaExpressionNode visitScopeExpression(lama.truffle.parser.antlr.LamaParser.ScopeExpressionContext ctx) {
            return visitScopedScopeExpression(ctx);
        }

        private LamaExpressionNode visitScopedScopeExpression(lama.truffle.parser.antlr.LamaParser.ScopeExpressionContext ctx) {
            return withScope(new Scope(scope), () -> {
                return visitScopeExpressionInCurrentScope(ctx);
            });
        }

        private LamaExpressionNode visitScopeExpressionInCurrentScope(lama.truffle.parser.antlr.LamaParser.ScopeExpressionContext ctx) {
            declareDefinitions(ctx.definitions);
            List<LamaExpressionNode> expressions = new ArrayList<>();
            for (var definition : ctx.definitions) {
                expressions.addAll(visitDefinitionAsStatements(definition));
            }
            expressions.add(visit(ctx.body));
            return withSection(compose(expressions), ctx);
        }

        @Override
        public LamaExpressionNode visitExpression(lama.truffle.parser.antlr.LamaParser.ExpressionContext ctx) {
            List<LamaExpressionNode> expressions = new ArrayList<>(ctx.exprs.size());
            for (var expr : ctx.exprs) {
                expressions.add(visit(expr));
            }
            return withSection(compose(expressions), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionRelationalOperand(lama.truffle.parser.antlr.LamaParser.ExpressionRelationalOperandContext ctx) {
            return visit(ctx.relationalOperand());
        }

        @Override
        public LamaExpressionNode visitExpressionIntegerLit(lama.truffle.parser.antlr.LamaParser.ExpressionIntegerLitContext ctx) {
            return withSection(new LamaLongLiteralNode(Long.parseLong(ctx.getText())), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionTrueLit(lama.truffle.parser.antlr.LamaParser.ExpressionTrueLitContext ctx) {
            return withSection(new LamaLongLiteralNode(1), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionFalseLit(lama.truffle.parser.antlr.LamaParser.ExpressionFalseLitContext ctx) {
            return withSection(new LamaLongLiteralNode(0), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionEta(lama.truffle.parser.antlr.LamaParser.ExpressionEtaContext ctx) {
            return withSection(variableLoad(resolveVariable(ctx.name.getText(), ctx.name)), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionInfixCall(lama.truffle.parser.antlr.LamaParser.ExpressionInfixCallContext ctx) {
            LamaExpressionNode[] args = ctx.args.stream().map(this::visit).toArray(LamaExpressionNode[]::new);
            return withSection(new LamaCallNode(variableLoad(resolveInfixOperator(ctx.name.getText(), ctx.start)), args), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionSkip(lama.truffle.parser.antlr.LamaParser.ExpressionSkipContext ctx) {
            return withSection(new LamaLongLiteralNode(0), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionName(lama.truffle.parser.antlr.LamaParser.ExpressionNameContext ctx) {
            return withSection(variableLoad(resolveVariable(ctx.getText(), ctx.start)), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionParen(lama.truffle.parser.antlr.LamaParser.ExpressionParenContext ctx) {
            return withSection(visitScopedScopeExpression(ctx.inner), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionNeg(lama.truffle.parser.antlr.LamaParser.ExpressionNegContext ctx) {
            return withSection(new LamaNegateNode(visit(ctx.rhs)), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionMultiplicative(lama.truffle.parser.antlr.LamaParser.ExpressionMultiplicativeContext ctx) {
            LamaExpressionNode left = visit(ctx.lhs);
            LamaExpressionNode right = visit(ctx.rhs);
            LamaExpressionNode node = switch (ctx.op.getStart().getType()) {
                case lama.truffle.parser.antlr.LamaParser.STAR -> new LamaMulNode(left, right);
                case lama.truffle.parser.antlr.LamaParser.SLASH -> new LamaDivNode(left, right);
                case lama.truffle.parser.antlr.LamaParser.PERCENT -> new LamaModuloNode(left, right);
                default -> throw new IllegalStateException("Unexpected multiplicative operator");
            };
            return withSection(node, ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionAdditive(lama.truffle.parser.antlr.LamaParser.ExpressionAdditiveContext ctx) {
            LamaExpressionNode left = visit(ctx.lhs);
            LamaExpressionNode right = visit(ctx.rhs);
            LamaExpressionNode node = switch (ctx.op.getStart().getType()) {
                case lama.truffle.parser.antlr.LamaParser.PLUS -> new LamaAddNode(left, right);
                case lama.truffle.parser.antlr.LamaParser.MINUS -> new LamaSubNode(left, right);
                default -> throw new IllegalStateException("Unexpected additive operator");
            };
            return withSection(node, ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionUserInfix(lama.truffle.parser.antlr.LamaParser.ExpressionUserInfixContext ctx) {
            return withSection(
                new LamaCallNode(
                    variableLoad(resolveInfixOperator(ctx.op.getText(), ctx.start)),
                    new LamaExpressionNode[]{visit(ctx.lhs), visit(ctx.rhs)}
                ),
                ctx
            );
        }

        @Override
        public LamaExpressionNode visitExpressionRelational(lama.truffle.parser.antlr.LamaParser.ExpressionRelationalContext ctx) {
            Kind kind = switch (ctx.op.getStart().getType()) {
                case lama.truffle.parser.antlr.LamaParser.EQ_EQ -> Kind.EQ;
                case lama.truffle.parser.antlr.LamaParser.BANG_EQ -> Kind.NE;
                case lama.truffle.parser.antlr.LamaParser.LT -> Kind.LT;
                case lama.truffle.parser.antlr.LamaParser.LT_EQ -> Kind.LE;
                case lama.truffle.parser.antlr.LamaParser.GT -> Kind.GT;
                case lama.truffle.parser.antlr.LamaParser.GT_EQ -> Kind.GE;
                default -> throw new IllegalStateException("Unexpected relational operator");
            };
            return withSection(new LamaComparisonNode(kind, visit(ctx.lhs), visit(ctx.rhs)), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionAnd(lama.truffle.parser.antlr.LamaParser.ExpressionAndContext ctx) {
            return withSection(new LamaAndNode(visit(ctx.lhs), visit(ctx.rhs)), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionOr(lama.truffle.parser.antlr.LamaParser.ExpressionOrContext ctx) {
            return withSection(new LamaOrNode(visit(ctx.lhs), visit(ctx.rhs)), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionAssign(lama.truffle.parser.antlr.LamaParser.ExpressionAssignContext ctx) {
            var nameContext = extractAssignedName(ctx.lhs);
            if (nameContext != null) {
                return withSection(variableStore(resolveVariable(nameContext.getText(), nameContext.start), visit(ctx.rhs)), ctx);
            }
            var indexContext = extractAssignedIndex(ctx.lhs);
            if (indexContext != null) {
                return withSection(new LamaStoreIndexNode(visit(indexContext.lhs), visit(indexContext.idx), visit(ctx.rhs)), ctx);
            }
            return withSection(new LamaUnimplementedNode("assignment target"), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionIf(lama.truffle.parser.antlr.LamaParser.ExpressionIfContext ctx) {
            var ifContext = ctx.ifExpression();
            LamaExpressionNode elseNode = ifContext.elseBranch == null
                ? new LamaLongLiteralNode(0)
                : visitScopedScopeExpression(ifContext.elseBranch);

            for (int i = ifContext.elifBranches.size() - 1; i >= 0; i--) {
                var branch = ifContext.elifBranches.get(i);
                elseNode = withSection(
                    new LamaIfNode(
                        visit(branch.cond),
                        visitScopedScopeExpression(branch.body),
                        elseNode
                    ),
                    branch
                );
            }

            return withSection(
                new LamaIfNode(
                    visit(ifContext.cond),
                    visitScopedScopeExpression(ifContext.thenBranch),
                    elseNode
                ),
                ctx
            );
        }

        @Override
        public LamaExpressionNode visitExpressionWhileDo(lama.truffle.parser.antlr.LamaParser.ExpressionWhileDoContext ctx) {
            var whileContext = ctx.whileDoExpression();
            return withSection(new LamaWhileNode(visit(whileContext.cond), visitScopedScopeExpression(whileContext.body)), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionDoWhile(lama.truffle.parser.antlr.LamaParser.ExpressionDoWhileContext ctx) {
            var doWhileContext = ctx.doWhileExpression();
            return withSection(withScope(new Scope(scope), () ->
                new LamaDoWhileNode(
                    visitScopeExpressionInCurrentScope(doWhileContext.body),
                    visit(doWhileContext.cond)
                )
            ), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionFor(lama.truffle.parser.antlr.LamaParser.ExpressionForContext ctx) {
            var forContext = ctx.forExpression();
            return withSection(withScope(new Scope(scope), () ->
                new LamaForNode(
                    visitScopeExpressionInCurrentScope(forContext.init),
                    visit(forContext.cond),
                    visit(forContext.post),
                    visitScopedScopeExpression(forContext.body)
                )
            ), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionCall(lama.truffle.parser.antlr.LamaParser.ExpressionCallContext ctx) {
            if (ctx.lhs instanceof lama.truffle.parser.antlr.LamaParser.ExpressionNameContext nameContext) {
                String name = nameContext.getText();
                if ("read".equals(name) && ctx.args.isEmpty()) {
                    return withSection(new LamaReadBuiltinNode(), ctx);
                }
                if ("write".equals(name) && ctx.args.size() == 1) {
                    return withSection(new LamaWriteBuiltinNode(visit(ctx.args.get(0))), ctx);
                }
            }

            LamaExpressionNode[] args = ctx.args.stream().map(this::visit).toArray(LamaExpressionNode[]::new);
            return withSection(new LamaCallNode(visit(ctx.lhs), args), ctx);
        }

        @Override
        public LamaExpressionNode visitDefinitionFunction(lama.truffle.parser.antlr.LamaParser.DefinitionFunctionContext ctx) {
            return buildFunctionDefinition(ctx.functionDefinition());
        }

        @Override
        public LamaExpressionNode visitDefinitionInfix(lama.truffle.parser.antlr.LamaParser.DefinitionInfixContext ctx) {
            return buildInfixDefinition(ctx.infixDefinition());
        }

        @Override
        public LamaExpressionNode visitExpressionFunLit(lama.truffle.parser.antlr.LamaParser.ExpressionFunLitContext ctx) {
            return withSection(buildFunction("<lambda>", ctx.params, ctx.body), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionListLit(lama.truffle.parser.antlr.LamaParser.ExpressionListLitContext ctx) {
            LamaExpressionNode node = new LamaSexpNode("Nil", new LamaExpressionNode[0]);
            for (int i = ctx.elems.size() - 1; i >= 0; i--) {
                node = new LamaSexpNode("Cons", new LamaExpressionNode[]{visit(ctx.elems.get(i)), node});
            }
            return withSection(node, ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionArrayLit(lama.truffle.parser.antlr.LamaParser.ExpressionArrayLitContext ctx) {
            LamaExpressionNode[] elements = ctx.elems.stream().map(this::visit).toArray(LamaExpressionNode[]::new);
            return withSection(new LamaArrayLiteralNode(elements), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionS(lama.truffle.parser.antlr.LamaParser.ExpressionSContext ctx) {
            LamaExpressionNode[] elements = ctx.args.stream().map(this::visit).toArray(LamaExpressionNode[]::new);
            return withSection(new LamaSexpNode(ctx.name.getText(), elements), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionCase(lama.truffle.parser.antlr.LamaParser.ExpressionCaseContext ctx) {
            var caseContext = ctx.caseExpression();
            List<LamaPattern> patterns = new ArrayList<>(caseContext.branches.size());
            List<LamaExpressionNode> bodies = new ArrayList<>(caseContext.branches.size());

            for (var branch : caseContext.branches) {
                withScope(new Scope(scope), () -> {
                    patterns.add(buildPattern(branch.pat));
                    bodies.add(visitScopedScopeExpression(branch.body));
                    return null;
                });
            }

            return withSection(
                new LamaCaseNode(
                    visit(caseContext.scrutinee),
                    patterns.toArray(LamaPattern[]::new),
                    bodies.toArray(LamaExpressionNode[]::new)
                ),
                ctx
            );
        }

        @Override
        public LamaExpressionNode visitExpressionLet(lama.truffle.parser.antlr.LamaParser.ExpressionLetContext ctx) {
            LamaExpressionNode initNode = visit(ctx.init);
            return withSection(withScope(new Scope(scope), () ->
                new LamaLetNode(
                    initNode,
                    buildPattern(ctx.pat),
                    visit(ctx.body)
                )
            ), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionIndex(lama.truffle.parser.antlr.LamaParser.ExpressionIndexContext ctx) {
            return withSection(new LamaLoadIndexNode(visit(ctx.lhs), visit(ctx.idx)), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionMethodCall(lama.truffle.parser.antlr.LamaParser.ExpressionMethodCallContext ctx) {
            if (ctx.PAREN_L() != null) {
                return withSection(new LamaUnimplementedNode("method calls"), ctx);
            }
            LamaExpressionNode receiver = visit(ctx.recv);
            return switch (ctx.callee.getText()) {
                case "length" -> withSection(new LamaLengthNode(receiver), ctx);
                case "string" -> withSection(new LamaStringifyNode(receiver), ctx);
                case "hd" -> withSection(new LamaHeadNode(receiver), ctx);
                case "tl" -> withSection(new LamaTailNode(receiver), ctx);
                default -> withSection(new LamaUnimplementedNode("method calls"), ctx);
            };
        }

        @Override
        public LamaExpressionNode visitExpressionStringLit(lama.truffle.parser.antlr.LamaParser.ExpressionStringLitContext ctx) {
            return withSection(new LamaStringLiteralNode(parseStringLiteral(ctx.getText())), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionCharLit(lama.truffle.parser.antlr.LamaParser.ExpressionCharLitContext ctx) {
            return withSection(new LamaLongLiteralNode(parseCharLiteral(ctx.getText())), ctx);
        }

        @Override
        public LamaExpressionNode visitExpressionCons(lama.truffle.parser.antlr.LamaParser.ExpressionConsContext ctx) {
            return withSection(new LamaSexpNode("Cons", new LamaExpressionNode[]{visit(ctx.lhs), visit(ctx.rhs)}), ctx);
        }

        private void declareDefinitions(List<lama.truffle.parser.antlr.LamaParser.DefinitionContext> definitions) {
            for (var definition : definitions) {
                if (definition instanceof lama.truffle.parser.antlr.LamaParser.DefinitionVariableContext variableContext) {
                    for (var item : variableContext.variableDefinition().vars) {
                        declareLocal(item.name.getText(), item.name);
                    }
                } else if (definition instanceof lama.truffle.parser.antlr.LamaParser.DefinitionFunctionContext functionContext) {
                    declareLocal(functionContext.functionDefinition().name.getText(), functionContext.functionDefinition().name);
                } else if (definition instanceof lama.truffle.parser.antlr.LamaParser.DefinitionInfixContext infixContext) {
                    declareInfix(infixContext.infixDefinition().name.getText(), infixContext.infixDefinition().name.start);
                }
            }
        }

        private List<LamaExpressionNode> visitDefinitionAsStatements(lama.truffle.parser.antlr.LamaParser.DefinitionContext ctx) {
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.DefinitionVariableContext variableContext) {
                return buildVariableDefinition(variableContext.variableDefinition());
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.DefinitionFunctionContext functionContext) {
                return List.of(buildFunctionDefinition(functionContext.functionDefinition()));
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.DefinitionInfixContext infixContext) {
                return List.of(buildInfixDefinition(infixContext.infixDefinition()));
            }
            throw new IllegalStateException("Unknown definition type");
        }

        private List<LamaExpressionNode> buildVariableDefinition(lama.truffle.parser.antlr.LamaParser.VariableDefinitionContext ctx) {
            List<LamaExpressionNode> statements = new ArrayList<>(ctx.vars.size());
            for (var item : ctx.vars) {
                Variable variable = resolveVariable(item.name.getText(), item.name);
                LamaExpressionNode initializer = item.init == null
                    ? new LamaLongLiteralNode(0)
                    : visit(item.init);
                statements.add(withSection(variableStore(variable, initializer), item));
            }
            return statements;
        }

        private LamaExpressionNode buildFunctionDefinition(lama.truffle.parser.antlr.LamaParser.FunctionDefinitionContext ctx) {
            Variable variable = resolveVariable(ctx.name.getText(), ctx.name);
            LamaFunctionTemplate template = buildFunctionTemplate(ctx.name.getText(), ctx.functionParameters(), ctx.body);
            return withSection(variableStore(variable, new LamaConstantNode(template)), ctx);
        }

        private LamaExpressionNode buildInfixDefinition(lama.truffle.parser.antlr.LamaParser.InfixDefinitionContext ctx) {
            Variable variable = resolveInfixOperator(ctx.name.getText(), ctx.name.start);
            LamaFunctionTemplate template = buildFunctionTemplate("infix " + ctx.name.getText(), ctx.functionParameters(), ctx.body);
            return withSection(variableStore(variable, new LamaConstantNode(template)), ctx);
        }

        private LamaExpressionNode buildFunction(
            String name,
            lama.truffle.parser.antlr.LamaParser.FunctionParametersContext paramCtx,
            lama.truffle.parser.antlr.LamaParser.FunctionBodyContext bodyCtx
        ) {
            BuiltFunction builtFunction = buildFunctionShape(name, paramCtx, bodyCtx);
            LamaExpressionNode[] captureNodes = builtFunction.captureSources().stream()
                .map(this::variableLoad)
                .toArray(LamaExpressionNode[]::new);
            return new LamaNewClosureNode(builtFunction.function(), captureNodes);
        }

        private LamaFunctionTemplate buildFunctionTemplate(
            String name,
            lama.truffle.parser.antlr.LamaParser.FunctionParametersContext paramCtx,
            lama.truffle.parser.antlr.LamaParser.FunctionBodyContext bodyCtx
        ) {
            BuiltFunction builtFunction = buildFunctionShape(name, paramCtx, bodyCtx);
            LamaCaptureSpec[] captureSpecs = builtFunction.captureSources().stream()
                .map(this::captureSpec)
                .toArray(LamaCaptureSpec[]::new);
            return new LamaFunctionTemplate(builtFunction.function(), captureSpecs);
        }

        private BuiltFunction buildFunctionShape(
            String name,
            lama.truffle.parser.antlr.LamaParser.FunctionParametersContext paramCtx,
            lama.truffle.parser.antlr.LamaParser.FunctionBodyContext bodyCtx
        ) {
            FunctionScope functionScope = new FunctionScope(scope, FrameDescriptor.newBuilder(), 0);
            return withScope(functionScope, () -> {
                List<LamaExpressionNode> bodyExpressions = new ArrayList<>();
                int arity = paramCtx.params.size();

                for (int i = 0; i < arity; i++) {
                    var param = paramCtx.params.get(i);
                    bodyExpressions.add(new LamaMatchNode(new LamaLoadArgumentNode(i + 1), buildPattern(param)));
                }

                bodyExpressions.add(visitScopedScopeExpression(bodyCtx.body));
                LamaExpressionNode body = compose(bodyExpressions);
                LamaRootNode root = new LamaRootNode(language, functionScope.frameBuilder.build(), body, source, name);
                return new BuiltFunction(new LamaFunction(root.getCallTarget(), arity), List.copyOf(functionScope.captureSources));
            });
        }

        private LamaCaptureSpec captureSpec(Variable variable) {
            if (variable instanceof LocalVariable local) {
                return LamaCaptureSpec.local(local.slot());
            }
            if (variable instanceof GlobalVariable global) {
                return LamaCaptureSpec.global(global.name());
            }
            UpvalueVariable upvalue = (UpvalueVariable) variable;
            return LamaCaptureSpec.upvalue(upvalue.slot());
        }

        private int declareLocal(String name, Token token) {
            if (scope.bindings.containsKey(name)) {
                throw parseError(token, "duplicate variable '%s'".formatted(name));
            }
            if (isTopLevelScope()) {
                scope.bindings.put(name, new GlobalVariable(name));
                return -1;
            }
            int slot = scope.functionScope.frameBuilder.addSlot(FrameSlotKind.Object, name, null);
            scope.bindings.put(name, new LocalVariable(slot));
            return slot;
        }

        private Variable declareInfix(String symbol, Token token) {
            if (scope.infixBindings.containsKey(symbol)) {
                throw parseError(token, "duplicate infix '%s'".formatted(symbol));
            }
            Variable variable;
            if (isTopLevelScope()) {
                variable = new GlobalVariable("$infix$" + symbol);
            } else {
                int slot = scope.functionScope.frameBuilder.addSlot(FrameSlotKind.Object, "$infix$" + symbol, null);
                variable = new LocalVariable(slot);
            }
            scope.infixBindings.put(symbol, variable);
            return variable;
        }

        private Variable resolveVariable(String name, Token token) {
            Scope cursor = scope;
            while (cursor != null && cursor.functionScope == scope.functionScope) {
                Variable local = cursor.bindings.get(name);
                if (local != null) {
                    return local;
                }
                cursor = cursor.parent;
            }
            UpvalueVariable captured = scope.functionScope.capturesByName.get(name);
            if (captured != null) {
                return captured;
            }
            for (; cursor != null; cursor = cursor.parent) {
                Variable bound = cursor.bindings.get(name);
                if (bound != null) {
                    if (bound instanceof GlobalVariable) {
                        return bound;
                    }
                    return scope.functionScope.capture(name, bound);
                }
            }
            throw parseError(token, "unknown variable '%s'".formatted(name));
        }

        private Variable resolveInfixOperator(String symbol, Token token) {
            String captureKey = "$infix$" + symbol;
            Scope cursor = scope;
            while (cursor != null && cursor.functionScope == scope.functionScope) {
                Variable local = cursor.infixBindings.get(symbol);
                if (local != null) {
                    return local;
                }
                cursor = cursor.parent;
            }
            UpvalueVariable captured = scope.functionScope.capturesByName.get(captureKey);
            if (captured != null) {
                return captured;
            }
            for (; cursor != null; cursor = cursor.parent) {
                Variable bound = cursor.infixBindings.get(symbol);
                if (bound != null) {
                    if (bound instanceof GlobalVariable) {
                        return bound;
                    }
                    return scope.functionScope.capture(captureKey, bound);
                }
            }
            throw parseError(token, "unknown infix '%s'".formatted(symbol));
        }

        private LamaExpressionNode variableLoad(Variable variable) {
            if (variable instanceof LocalVariable local) {
                return new LamaLoadLocalNode(local.slot());
            }
            if (variable instanceof GlobalVariable global) {
                return new LamaLoadGlobalNode(global.name());
            }
            UpvalueVariable upvalue = (UpvalueVariable) variable;
            return new LamaLoadUpvalueNode(upvalue.slot());
        }

        private LamaExpressionNode variableStore(Variable variable, LamaExpressionNode valueNode) {
            if (variable instanceof LocalVariable local) {
                return new LamaStoreLocalNode(local.slot(), valueNode);
            }
            if (variable instanceof GlobalVariable global) {
                return new LamaStoreGlobalNode(global.name(), valueNode);
            }
            UpvalueVariable upvalue = (UpvalueVariable) variable;
            return new LamaStoreUpvalueNode(upvalue.slot(), valueNode);
        }

        private LamaExpressionNode compose(List<LamaExpressionNode> expressions) {
            if (expressions.isEmpty()) {
                return new LamaLongLiteralNode(0);
            }
            if (expressions.size() == 1) {
                return expressions.getFirst();
            }
            return new LamaSequenceNode(expressions.toArray(LamaExpressionNode[]::new));
        }

        private <T extends LamaExpressionNode> T withSection(T node, ParserRuleContext ctx) {
            int start = ctx.start.getStartIndex();
            int end = ctx.stop.getStopIndex();
            node.setSourceSection(start, end - start + 1);
            return node;
        }

        private LamaParseException parseError(Token token, String message) {
            return new LamaParseException("%s:%d:%d: %s".formatted(
                source.getName(),
                token.getLine(),
                token.getCharPositionInLine() + 1,
                message
            ));
        }

        private lama.truffle.parser.antlr.LamaParser.ExpressionNameContext extractAssignedName(
            lama.truffle.parser.antlr.LamaParser.BasicExpressionContext ctx
        ) {
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.ExpressionRelationalOperandContext relationalOperandContext
                && relationalOperandContext.relationalOperand() instanceof lama.truffle.parser.antlr.LamaParser.ExpressionNameContext nameContext) {
                return nameContext;
            }
            return null;
        }

        private lama.truffle.parser.antlr.LamaParser.ExpressionIndexContext extractAssignedIndex(
            lama.truffle.parser.antlr.LamaParser.BasicExpressionContext ctx
        ) {
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.ExpressionRelationalOperandContext relationalOperandContext
                && relationalOperandContext.relationalOperand() instanceof lama.truffle.parser.antlr.LamaParser.ExpressionIndexContext indexContext) {
                return indexContext;
            }
            return null;
        }

        private LamaPattern buildPattern(lama.truffle.parser.antlr.LamaParser.PatternContext ctx) {
            return buildConsPattern(ctx.consPattern());
        }

        private LamaPattern buildConsPattern(lama.truffle.parser.antlr.LamaParser.ConsPatternContext ctx) {
            if (ctx.tail == null) {
                return buildSimplePattern(ctx.head);
            }
            return new LamaSexpPattern(
                "Cons",
                new LamaPattern[]{buildSimplePattern(ctx.head), buildConsPattern(ctx.tail)}
            );
        }

        private LamaPattern buildSimplePattern(lama.truffle.parser.antlr.LamaParser.SimplePatternContext ctx) {
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternParenContext parenContext) {
                return buildPattern(parenContext.inner);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternHashFunContext) {
                return new LamaTypePattern(LamaTypePattern.Kind.FUN);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternHashSexpContext) {
                return new LamaTypePattern(LamaTypePattern.Kind.SEXP);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternHashArrayContext) {
                return new LamaTypePattern(LamaTypePattern.Kind.ARRAY);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternHashStrContext) {
                return new LamaTypePattern(LamaTypePattern.Kind.STR);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternHashValContext) {
                return new LamaTypePattern(LamaTypePattern.Kind.VAL);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternTrueLitContext) {
                return new LamaLiteralLongPattern(1);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternFalseLitContext) {
                return new LamaLiteralLongPattern(0);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternCharLitContext charContext) {
                return new LamaLiteralLongPattern(parseCharLiteral(charContext.getText()));
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternStringLitContext stringContext) {
                return new LamaStringPattern(parseStringLiteral(stringContext.getText()));
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternIntegerLitContext intContext) {
                long value = Long.parseLong(intContext.DECIMAL().getText());
                if (intContext.neg != null) {
                    value = -value;
                }
                return new LamaLiteralLongPattern(value);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternNameContext nameContext) {
                int slot = declareLocal(nameContext.name.getText(), nameContext.name);
                LamaPattern inner = nameContext.inner == null
                    ? new LamaWildcardPattern()
                    : buildPattern(nameContext.inner);
                return new LamaBindingPattern(slot, inner);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternWildcardContext) {
                return new LamaWildcardPattern();
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternSexpContext sexpContext) {
                LamaPattern[] patterns = sexpContext.args.stream().map(this::buildPattern).toArray(LamaPattern[]::new);
                return new LamaSexpPattern(sexpContext.name.getText(), patterns);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternArrayContext arrayContext) {
                LamaPattern[] patterns = arrayContext.elems.stream().map(this::buildPattern).toArray(LamaPattern[]::new);
                return new LamaArrayPattern(patterns);
            }
            if (ctx instanceof lama.truffle.parser.antlr.LamaParser.PatternListContext listContext) {
                LamaPattern node = new LamaSexpPattern("Nil", new LamaPattern[0]);
                for (int i = listContext.elems.size() - 1; i >= 0; i--) {
                    node = new LamaSexpPattern("Cons", new LamaPattern[]{buildPattern(listContext.elems.get(i)), node});
                }
                return node;
            }
            return new LamaWildcardPattern();
        }

        private String parseStringLiteral(String tokenText) {
            String inner = tokenText.substring(1, tokenText.length() - 1);
            return inner.replace("\"\"", "\"");
        }

        private long parseCharLiteral(String tokenText) {
            if ("'\\n'".equals(tokenText)) {
                return '\n';
            }
            if ("'\\t'".equals(tokenText)) {
                return '\t';
            }
            String inner = tokenText.substring(1, tokenText.length() - 1);
            if ("''".equals(inner)) {
                return '\'';
            }
            return inner.charAt(0);
        }

        private <T> T withScope(Scope newScope, ScopeSupplier<T> supplier) {
            Scope previous = scope;
            scope = newScope;
            try {
                return supplier.get();
            } finally {
                scope = previous;
            }
        }

        private boolean isTopLevelScope() {
            return scope.functionScope == rootFunctionScope && scope.parent == rootFunctionScope;
        }
    }

    private record BuiltFunction(LamaFunction function, List<Variable> captureSources) {
    }

    @FunctionalInterface
    private interface ScopeSupplier<T> {
        T get();
    }

    private static final class ThrowingErrorListener extends BaseErrorListener {
        private final Source source;

        private ThrowingErrorListener(Source source) {
            this.source = source;
        }

        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e
        ) {
            throw new LamaParseException("%s:%d:%d: %s".formatted(
                source.getName(),
                line,
                charPositionInLine + 1,
                msg
            ));
        }
    }
}
