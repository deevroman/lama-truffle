package lama.truffle.ast;

public final class LamaModuloNode extends LamaBinaryNode {
    public LamaModuloNode(LamaExpressionNode leftNode, LamaExpressionNode rightNode) {
        super(leftNode, rightNode);
    }

    @Override
    protected long executeLongs(long left, long right) {
        return left % right;
    }
}
