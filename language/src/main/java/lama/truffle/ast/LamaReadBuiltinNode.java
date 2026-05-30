package lama.truffle.ast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaContext;

import java.util.NoSuchElementException;

public final class LamaReadBuiltinNode extends LamaExpressionNode {
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return executeLong(frame);
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        return read(LamaContext.get(this));
    }

    @CompilerDirectives.TruffleBoundary
    private static long read(LamaContext context) {
        context.output.print(" > ");
        try {
            return context.input.nextLong(10);
        } catch (NoSuchElementException ignored) {
            return 0;
        }
    }
}
