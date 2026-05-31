package lama.truffle.pattern;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import lama.truffle.runtime.LamaSexp;

public final class LamaSexpPattern implements LamaPattern {
    private final String tag;
    @CompilerDirectives.CompilationFinal(dimensions = 1)
    private final LamaPattern[] elementPatterns;

    public LamaSexpPattern(String tag, LamaPattern[] elementPatterns) {
        this.tag = tag;
        this.elementPatterns = elementPatterns;
    }

    @Override
    @ExplodeLoop
    public boolean matches(VirtualFrame frame, Object value) {
        if (!(value instanceof LamaSexp sexp)) {
            return false;
        }
        if (!tag.equals(sexp.tag()) || sexp.elements().length != elementPatterns.length) {
            return false;
        }
        for (int i = 0; i < elementPatterns.length; i++) {
            if (!elementPatterns[i].matches(frame, sexp.elements()[i])) {
                return false;
            }
        }
        return true;
    }
}
