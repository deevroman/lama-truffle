package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class LamaBinaryNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode leftNode;

    @Child
    private LamaExpressionNode rightNode;

    protected LamaBinaryNode(LamaExpressionNode leftNode, LamaExpressionNode rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    @Override
    public final Object executeGeneric(VirtualFrame frame) {
        return executeLong(frame);
    }

    @Override
    public final long executeLong(VirtualFrame frame) {
        return executeLongs(leftNode.executeLong(frame), rightNode.executeLong(frame));
    }

    protected abstract long executeLongs(long left, long right);
}
