package lama.truffle.pattern;

import com.oracle.truffle.api.frame.VirtualFrame;

public interface LamaPattern {
    boolean matches(VirtualFrame frame, Object value);
}
