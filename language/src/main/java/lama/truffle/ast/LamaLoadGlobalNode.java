package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaContext;
import lama.truffle.runtime.LamaFunctionTemplate;

public final class LamaLoadGlobalNode extends LamaExpressionNode {
    private final String name;

    public LamaLoadGlobalNode(String name) {
        this.name = name;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object value = LamaContext.get(this).getGlobal(name);
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
