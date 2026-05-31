package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.runtime.LamaClosure;
import lama.truffle.runtime.LamaFunctionTemplate;

public final class LamaLoadUpvalueNode extends LamaExpressionNode {
    private final int slot;

    public LamaLoadUpvalueNode(int slot) {
        this.slot = slot;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        LamaClosure closure = (LamaClosure) frame.getArguments()[0];
        Object value = closure.captures()[slot];
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
