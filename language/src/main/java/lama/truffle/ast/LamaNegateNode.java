package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaNegateNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode valueNode;

    public LamaNegateNode(LamaExpressionNode valueNode) {
        this.valueNode = valueNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return executeLong(frame);
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        return -valueNode.executeLong(frame);
    }
}
