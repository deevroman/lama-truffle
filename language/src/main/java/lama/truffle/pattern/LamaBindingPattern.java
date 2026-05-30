package lama.truffle.pattern;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaBindingPattern implements LamaPattern {
    private final int slot;
    private final LamaPattern innerPattern;

    public LamaBindingPattern(int slot, LamaPattern innerPattern) {
        this.slot = slot;
        this.innerPattern = innerPattern;
    }

    @Override
    public boolean matches(VirtualFrame frame, Object value) {
        if (!innerPattern.matches(frame, value)) {
            return false;
        }
        frame.setObject(slot, value);
        return true;
    }
}
