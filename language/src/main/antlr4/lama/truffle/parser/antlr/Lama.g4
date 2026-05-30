grammar Lama;

options {
    tokenVocab = LamaLexer;
}

file : imports+=importDecl* body=scopeExpression EOF;
importDecl : IMPORT what=UIDENT SEMI;

scopeExpression : definitions+=definition* body=expression;

definition
    : variableDefinition # DefinitionVariable
    | functionDefinition # DefinitionFunction
    | infixDefinition # DefinitionInfix
    ;

variableDefinition
    : kind=(VAR | PUBLIC)
      vars+=variableDefinitionItem (COMMA vars+=variableDefinitionItem)*
      SEMI
    ;

variableDefinitionItem
    : name=LIDENT
      (EQ init=basicExpression)?
    ;

functionDefinition
    : public=PUBLIC?
      FUN name=LIDENT
      PAREN_L functionParameters PAREN_R
      body=functionBody
    ;

infixDefinition
    : kind=(INFIX | INFIXL | INFIXR)
      name=customOperatorSymbol
      ((AT_KW precedence=operatorSymbol) | (BEFORE precedence=operatorSymbol) | (AFTER precedence=operatorSymbol))?
      PAREN_L functionParameters PAREN_R
      body=functionBody
    ;

functionParameters : (params+=pattern (COMMA params+=pattern)*)?;

functionBody : BRACE_L body=scopeExpression BRACE_R;

expression : exprs+=basicExpression (SEMI exprs+=basicExpression)*;

basicExpression
    : relationalOperand # ExpressionRelationalOperand
    | lhs=relationalOperand op=relationalOp rhs=relationalOperand # ExpressionRelational
    | lhs=basicExpression AND_AND rhs=basicExpression # ExpressionAnd
    | lhs=basicExpression BANG_BANG rhs=basicExpression # ExpressionOr
    |<assoc=right> lhs=basicExpression COLON rhs=basicExpression # ExpressionCons
    |<assoc=right> lhs=basicExpression COLON_EQ rhs=basicExpression # ExpressionAssign
    ;

relationalOperand
    : DECIMAL # ExpressionIntegerLit
    | STRING # ExpressionStringLit
    | CHAR # ExpressionCharLit
    | LIDENT # ExpressionName
    | TRUE # ExpressionTrueLit
    | FALSE # ExpressionFalseLit
    | ETA name=LIDENT # ExpressionEta
    | INFIX name=customOperatorSymbol PAREN_L (args+=expression (COMMA args+=expression)*)? PAREN_R # ExpressionInfixCall
    | FUN PAREN_L params=functionParameters PAREN_R body=functionBody # ExpressionFunLit
    | SKIP_KW # ExpressionSkip
    | PAREN_L inner=scopeExpression PAREN_R # ExpressionParen
    | BRACE_L (elems+=expression (COMMA elems+=expression)*)? BRACE_R # ExpressionListLit
    | BRACKET_L (elems+=expression (COMMA elems+=expression)*)? BRACKET_R # ExpressionArrayLit
    | name=UIDENT (PAREN_L args+=expression (COMMA args+=expression)* PAREN_R)? # ExpressionS
    | ifExpression # ExpressionIf
    | whileDoExpression # ExpressionWhileDo
    | doWhileExpression # ExpressionDoWhile
    | forExpression # ExpressionFor
    | caseExpression # ExpressionCase
    | LET pat=pattern EQ init=expression IN body=expression # ExpressionLet
    | lhs=relationalOperand BRACKET_L idx=expression BRACKET_R # ExpressionIndex
    | lhs=relationalOperand PAREN_L (args+=expression (COMMA args+=expression)*)? PAREN_R # ExpressionCall
    | recv=relationalOperand DOT callee=LIDENT (PAREN_L (args+=expression (COMMA args+=expression)*)? PAREN_R)? # ExpressionMethodCall
    | MINUS rhs=relationalOperand # ExpressionNeg
    | lhs=relationalOperand op=customOperatorSymbol rhs=relationalOperand # ExpressionUserInfix
    | lhs=relationalOperand op=multiplicativeOp rhs=relationalOperand # ExpressionMultiplicative
    | lhs=relationalOperand op=additiveOp rhs=relationalOperand # ExpressionAdditive
    ;

multiplicativeOp
    : STAR # OpMul
    | SLASH # OpDiv
    | PERCENT # OpMod
    ;

additiveOp
    : PLUS # OpAdd
    | MINUS # OpSub
    ;

relationalOp
    : EQ_EQ # OpEq
    | BANG_EQ # OpNe
    | LT_EQ # OpLe
    | LT # OpLt
    | GT_EQ # OpGe
    | GT # OpGt
    ;

ifExpression
    : IF cond=expression
      THEN thenBranch=scopeExpression
      (elifBranches+=elifBranch)*
      (ELSE elseBranch=scopeExpression)?
      FI
    ;

elifBranch
    : ELIF cond=expression
      THEN body=scopeExpression
    ;

whileDoExpression
    : WHILE cond=expression
      DO body=scopeExpression
      OD
    ;

doWhileExpression
    : DO body=scopeExpression
      WHILE cond=expression
      OD
    ;

forExpression
    : FOR
      init=scopeExpression
      COMMA cond=expression
      COMMA post=expression
      DO body=scopeExpression
      OD
    ;

caseExpression
    : CASE scrutinee=expression
      OF branches+=caseBranch (PIPE branches+=caseBranch)*
      ESAC
    ;

caseBranch : pat=pattern ARROW body=scopeExpression;

pattern : consPattern;

operatorSymbol
    : parts+=operatorToken+
    ;

customOperatorSymbol
    : parts+=operatorToken parts+=operatorToken (parts+=operatorToken)*
    ;

operatorToken
    : EQ
    | EQ_EQ
    | BANG_EQ
    | LT_EQ
    | LT
    | GT_EQ
    | GT
    | PLUS
    | MINUS
    | STAR
    | SLASH
    | PERCENT
    ;

consPattern
    : head=simplePattern
      (COLON tail=consPattern)?
    ;

simplePattern
    : PAREN_L inner=pattern PAREN_R # PatternParen
    | HASH FUN # PatternHashFun
    | HASH SEXP # PatternHashSexp
    | HASH ARRAY # PatternHashArray
    | HASH STR # PatternHashStr
    | HASH VAL # PatternHashVal
    | HASH BOX # PatternHashBox
    | TRUE # PatternTrueLit
    | FALSE # PatternFalseLit
    | CHAR # PatternCharLit
    | STRING # PatternStringLit
    | neg=MINUS? DECIMAL # PatternIntegerLit
    | name=LIDENT (AT inner=pattern)? # PatternName
    | UNDER # PatternWildcard
    | name=UIDENT (PAREN_L args+=pattern (COMMA args+=pattern)* PAREN_R)? # PatternSexp
    | BRACKET_L (elems+=pattern (COMMA elems+=pattern)*)? BRACKET_R # PatternArray
    | BRACE_L (elems+=pattern (COMMA elems+=pattern)*)? BRACE_R # PatternList
    ;
