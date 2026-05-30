package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaException;
import lama.truffle.pattern.LamaPattern;

public final class LamaCaseNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode scrutineeNode;

    private final LamaPattern[] patterns;

    @Children
    private final LamaExpressionNode[] bodyNodes;

    public LamaCaseNode(LamaExpressionNode scrutineeNode, LamaPattern[] patterns, LamaExpressionNode[] bodyNodes) {
        this.scrutineeNode = scrutineeNode;
        this.patterns = patterns;
        this.bodyNodes = bodyNodes;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object scrutinee = scrutineeNode.executeGeneric(frame);
        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i].matches(frame, scrutinee)) {
                return bodyNodes[i].executeGeneric(frame);
            }
        }
        throw LamaException.matchFailure(this);
    }
}
