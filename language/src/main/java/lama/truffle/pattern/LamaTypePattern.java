package lama.truffle.pattern;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.runtime.LamaArray;
import lama.truffle.runtime.LamaClosure;
import lama.truffle.runtime.LamaSexp;
import lama.truffle.runtime.LamaString;

public final class LamaTypePattern implements LamaPattern {
    public enum Kind {
        FUN,
        SEXP,
        ARRAY,
        STR,
        VAL
    }

    private final Kind kind;

    public LamaTypePattern(Kind kind) {
        this.kind = kind;
    }

    @Override
    public boolean matches(VirtualFrame frame, Object value) {
        return switch (kind) {
            case FUN -> value instanceof LamaClosure;
            case SEXP -> value instanceof LamaSexp;
            case ARRAY -> value instanceof LamaArray;
            case STR -> value instanceof LamaString;
            case VAL -> value instanceof Long;
        };
    }
}
