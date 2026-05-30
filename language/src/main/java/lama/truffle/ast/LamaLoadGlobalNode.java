package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaContext;
import lama.truffle.runtime.LamaCaptureSpec;
import lama.truffle.runtime.LamaClosure;
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
            return template.instantiate(spec -> resolveCapture(frame, spec));
        }
        return value;
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        return (long) executeGeneric(frame);
    }

    private Object resolveCapture(VirtualFrame frame, LamaCaptureSpec spec) {
        return switch (spec.kind()) {
            case LOCAL -> frame.getObject(spec.slot());
            case UPVALUE -> ((LamaClosure) frame.getArguments()[0]).captures()[spec.slot()];
            case GLOBAL -> LamaContext.get(this).getGlobal(spec.name());
        };
    }
}
