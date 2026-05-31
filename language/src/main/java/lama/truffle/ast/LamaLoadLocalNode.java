package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.runtime.LamaFunctionTemplate;

public final class LamaLoadLocalNode extends LamaExpressionNode {
    private final int slot;

    public LamaLoadLocalNode(int slot) {
        this.slot = slot;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object value = frame.getObject(slot);
        if (value instanceof LamaFunctionTemplate template) {
            return instantiateTemplate(frame, template);
        }
        return value;
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        return (long) executeGeneric(frame);
    }
}
