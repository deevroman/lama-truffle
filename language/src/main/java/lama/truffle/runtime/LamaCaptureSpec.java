package lama.truffle.runtime;

public record LamaCaptureSpec(Kind kind, int slot, String name) {
    public enum Kind {
        LOCAL,
        UPVALUE,
        GLOBAL
    }

    public static LamaCaptureSpec local(int slot) {
        return new LamaCaptureSpec(Kind.LOCAL, slot, null);
    }

    public static LamaCaptureSpec upvalue(int slot) {
        return new LamaCaptureSpec(Kind.UPVALUE, slot, null);
    }

    public static LamaCaptureSpec global(String name) {
        return new LamaCaptureSpec(Kind.GLOBAL, -1, name);
    }
}
