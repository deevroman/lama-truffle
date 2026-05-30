package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaWhileNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode conditionNode;

    @Child
    private LamaExpressionNode bodyNode;

    public LamaWhileNode(LamaExpressionNode conditionNode, LamaExpressionNode bodyNode) {
        this.conditionNode = conditionNode;
        this.bodyNode = bodyNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        while (conditionNode.executeLong(frame) != 0) {
            bodyNode.executeGeneric(frame);
        }
        return 0L;
    }
}
