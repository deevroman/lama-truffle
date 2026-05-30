package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaIfNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode conditionNode;

    @Child
    private LamaExpressionNode thenNode;

    @Child
    private LamaExpressionNode elseNode;

    public LamaIfNode(LamaExpressionNode conditionNode, LamaExpressionNode thenNode, LamaExpressionNode elseNode) {
        this.conditionNode = conditionNode;
        this.thenNode = thenNode;
        this.elseNode = elseNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        if (conditionNode.executeLong(frame) != 0) {
            return thenNode.executeGeneric(frame);
        }
        return elseNode.executeGeneric(frame);
    }
}
