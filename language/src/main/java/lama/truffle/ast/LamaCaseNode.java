package lama.truffle.ast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import lama.truffle.LamaException;
import lama.truffle.pattern.LamaPattern;

public final class LamaCaseNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode scrutineeNode;

    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final LamaPattern[] patterns;

    @Children
    private final LamaExpressionNode[] bodyNodes;

    public LamaCaseNode(LamaExpressionNode scrutineeNode, LamaPattern[] patterns, LamaExpressionNode[] bodyNodes) {
        this.scrutineeNode = scrutineeNode;
        this.patterns = patterns;
        this.bodyNodes = bodyNodes;
    }

    @Override
    @ExplodeLoop
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
