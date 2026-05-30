package lama.truffle.ast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import lama.truffle.runtime.LamaClosure;

public abstract class LamaExpressionNode extends LamaNode {
    private static final int SOURCE_MISSING = -1;

    private int sourceCharIndex = SOURCE_MISSING;
    private int sourceLength;

    public abstract Object executeGeneric(VirtualFrame frame);

    public long executeLong(VirtualFrame frame) {
        return (long) executeGeneric(frame);
    }

    public LamaClosure executeClosure(VirtualFrame frame) {
        return (LamaClosure) executeGeneric(frame);
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
}
