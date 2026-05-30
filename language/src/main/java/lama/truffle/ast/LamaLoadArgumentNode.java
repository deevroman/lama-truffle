package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaLoadArgumentNode extends LamaExpressionNode {
    private final int index;

    public LamaLoadArgumentNode(int index) {
        this.index = index;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return frame.getArguments()[index];
    }
}
