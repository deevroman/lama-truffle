package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaOrNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode leftNode;

    @Child
    private LamaExpressionNode rightNode;

    public LamaOrNode(LamaExpressionNode leftNode, LamaExpressionNode rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return executeLong(frame);
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        if (leftNode.executeLong(frame) != 0) {
            return 1;
        }
        return rightNode.executeLong(frame) != 0 ? 1 : 0;
    }
}
