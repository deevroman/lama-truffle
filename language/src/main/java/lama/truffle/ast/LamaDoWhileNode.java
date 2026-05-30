package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaDoWhileNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode bodyNode;

    @Child
    private LamaExpressionNode conditionNode;

    public LamaDoWhileNode(LamaExpressionNode bodyNode, LamaExpressionNode conditionNode) {
        this.bodyNode = bodyNode;
        this.conditionNode = conditionNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        do {
            bodyNode.executeGeneric(frame);
        } while (conditionNode.executeLong(frame) != 0);
        return 0L;
    }
}
