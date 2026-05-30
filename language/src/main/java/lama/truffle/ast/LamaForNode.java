package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaForNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode initNode;

    @Child
    private LamaExpressionNode conditionNode;

    @Child
    private LamaExpressionNode postNode;

    @Child
    private LamaExpressionNode bodyNode;

    public LamaForNode(
        LamaExpressionNode initNode,
        LamaExpressionNode conditionNode,
        LamaExpressionNode postNode,
        LamaExpressionNode bodyNode
    ) {
        this.initNode = initNode;
        this.conditionNode = conditionNode;
        this.postNode = postNode;
        this.bodyNode = bodyNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        initNode.executeGeneric(frame);
        while (conditionNode.executeLong(frame) != 0) {
            bodyNode.executeGeneric(frame);
            postNode.executeGeneric(frame);
        }
        return 0L;
    }
}
