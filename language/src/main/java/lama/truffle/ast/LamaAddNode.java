package lama.truffle.ast;

public final class LamaAddNode extends LamaBinaryNode {
    public LamaAddNode(LamaExpressionNode leftNode, LamaExpressionNode rightNode) {
        super(leftNode, rightNode);
    }

    @Override
    protected long executeLongs(long left, long right) {
        return left + right;
    }
}
