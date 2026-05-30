package lama.truffle.ast;

public final class LamaDivNode extends LamaBinaryNode {
    public LamaDivNode(LamaExpressionNode leftNode, LamaExpressionNode rightNode) {
        super(leftNode, rightNode);
    }

    @Override
    protected long executeLongs(long left, long right) {
        return left / right;
    }
}
