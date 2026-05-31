package lama.truffle.pattern;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import lama.truffle.runtime.LamaArray;

public final class LamaArrayPattern implements LamaPattern {
    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final LamaPattern[] elementPatterns;

    public LamaArrayPattern(LamaPattern[] elementPatterns) {
        this.elementPatterns = elementPatterns;
    }

    @Override
    @ExplodeLoop
    public boolean matches(VirtualFrame frame, Object value) {
        if (!(value instanceof LamaArray array) || array.elements().length != elementPatterns.length) {
            return false;
        }
        for (int i = 0; i < elementPatterns.length; i++) {
            if (!elementPatterns[i].matches(frame, array.elements()[i])) {
                return false;
            }
        }
        return true;
    }
}
