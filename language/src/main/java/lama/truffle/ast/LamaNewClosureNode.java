package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.runtime.LamaClosure;
import lama.truffle.runtime.LamaFunction;

public final class LamaNewClosureNode extends LamaExpressionNode {
    private final LamaFunction function;
    @Children
    private final LamaExpressionNode[] captureNodes;

    public LamaNewClosureNode(LamaFunction function, LamaExpressionNode[] captureNodes) {
        this.function = function;
        this.captureNodes = captureNodes;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object[] captures = new Object[captureNodes.length];
        for (int i = 0; i < captureNodes.length; i++) {
            captures[i] = captureNodes[i].executeGeneric(frame);
        }
        return new LamaClosure(function, captures);
    }
}
