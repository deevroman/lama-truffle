package lama.truffle.ast;

public final class LamaSubNode extends LamaBinaryNode {
    public LamaSubNode(LamaExpressionNode leftNode, LamaExpressionNode rightNode) {
        super(leftNode, rightNode);
    }

    @Override
    protected long executeLongs(long left, long right) {
        return left - right;
    }
}
