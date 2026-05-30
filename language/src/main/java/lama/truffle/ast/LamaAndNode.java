package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaAndNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode leftNode;

    @Child
    private LamaExpressionNode rightNode;

    public LamaAndNode(LamaExpressionNode leftNode, LamaExpressionNode rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return executeLong(frame);
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        if (leftNode.executeLong(frame) == 0) {
            return 0;
        }
        return rightNode.executeLong(frame) != 0 ? 1 : 0;
    }
}
