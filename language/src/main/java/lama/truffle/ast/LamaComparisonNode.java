package lama.truffle.ast;

public final class LamaComparisonNode extends LamaBinaryNode {
    public enum Kind {
        EQ,
        NE,
        LT,
        LE,
        GT,
        GE
    }

    private final Kind kind;

    public LamaComparisonNode(Kind kind, LamaExpressionNode leftNode, LamaExpressionNode rightNode) {
        super(leftNode, rightNode);
        this.kind = kind;
    }

    @Override
    protected long executeLongs(long left, long right) {
        return switch (kind) {
            case EQ -> left == right ? 1 : 0;
            case NE -> left != right ? 1 : 0;
            case LT -> left < right ? 1 : 0;
            case LE -> left <= right ? 1 : 0;
            case GT -> left > right ? 1 : 0;
            case GE -> left >= right ? 1 : 0;
        };
    }
}
