package lama.truffle.ast;

public final class LamaMulNode extends LamaBinaryNode {
    public LamaMulNode(LamaExpressionNode leftNode, LamaExpressionNode rightNode) {
        super(leftNode, rightNode);
    }

    @Override
    protected long executeLongs(long left, long right) {
        return left * right;
    }
}
