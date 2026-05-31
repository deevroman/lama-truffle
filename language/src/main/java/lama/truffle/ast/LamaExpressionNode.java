package lama.truffle.ast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.source.SourceSection;
import lama.truffle.LamaContext;
import lama.truffle.runtime.LamaClosure;
import lama.truffle.runtime.LamaFunctionTemplate;

public abstract class LamaExpressionNode extends LamaNode {
    private static final int SOURCE_MISSING = -1;
    private static final Object[] NO_UPVALUES = new Object[0];

    private int sourceCharIndex = SOURCE_MISSING;
    private int sourceLength;

    public abstract Object executeGeneric(VirtualFrame frame);

    public long executeLong(VirtualFrame frame) {
        return (long) executeGeneric(frame);
    }

    public LamaClosure executeClosure(VirtualFrame frame) {
        return (LamaClosure) executeGeneric(frame);
    }

    protected final LamaClosure instantiateTemplate(VirtualFrame frame, LamaFunctionTemplate template) {
        return template.instantiate(new FrameSnapshot(snapshotLocals(frame), snapshotUpvalues(frame), LamaContext.get(this)));
    }

    @ExplodeLoop
    private static Object[] snapshotLocals(VirtualFrame frame) {
        int slots = frame.getFrameDescriptor().getNumberOfSlots();
        Object[] locals = new Object[slots];
        for (int i = 0; i < slots; i++) {
            locals[i] = frame.getValue(i);
        }
        return locals;
    }

    private static Object[] snapshotUpvalues(VirtualFrame frame) {
        Object[] arguments = frame.getArguments();
        if (arguments.length == 0 || !(arguments[0] instanceof LamaClosure closure)) {
            return NO_UPVALUES;
        }
        return closure.captures();
    }

    @Override
    @CompilerDirectives.TruffleBoundary
    public SourceSection getSourceSection() {
        if (sourceCharIndex == SOURCE_MISSING) {
            return null;
        }

        if (getRootNode() == null || getRootNode().getSourceSection() == null) {
            return null;
        }

        return getRootNode().getSourceSection().getSource().createSection(sourceCharIndex, sourceLength);
    }

    @CompilerDirectives.TruffleBoundary
    public final void setSourceSection(int charIndex, int length) {
        if (charIndex < 0) {
            throw new IllegalArgumentException("charIndex must be >= 0");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length must be >= 0");
        }

        this.sourceCharIndex = charIndex;
        this.sourceLength = length;
    }

    public final int getSourceStartIndex() {
        return sourceCharIndex;
    }

    public final int getSourceEndIndex() {
        return sourceCharIndex + sourceLength;
    }

    private record FrameSnapshot(Object[] locals, Object[] upvalues, LamaContext context)
        implements LamaFunctionTemplate.Resolver {
        @Override
        public Object resolve(lama.truffle.runtime.LamaCaptureSpec spec) {
            return switch (spec.kind()) {
                case LOCAL -> locals[spec.slot()];
                case UPVALUE -> upvalues[spec.slot()];
                case GLOBAL -> context.getGlobal(spec.name());
            };
        }
    }
}
