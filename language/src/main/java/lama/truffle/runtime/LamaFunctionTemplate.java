package lama.truffle.runtime;

import com.oracle.truffle.api.CompilerDirectives;

import java.util.IdentityHashMap;

public record LamaFunctionTemplate(LamaFunction function, LamaCaptureSpec[] captureSpecs) {
    @FunctionalInterface
    public interface Resolver {
        Object resolve(LamaCaptureSpec spec);
    }

    @CompilerDirectives.TruffleBoundary
    public LamaClosure instantiate(Resolver resolver) {
        return instantiate(this, resolver, new IdentityHashMap<>());
    }

    private static LamaClosure instantiate(
        LamaFunctionTemplate template,
        Resolver resolver,
        IdentityHashMap<LamaFunctionTemplate, LamaClosure> memo
    ) {
        LamaClosure cached = memo.get(template);
        if (cached != null) {
            return cached;
        }

        Object[] captures = new Object[template.captureSpecs.length];
        LamaClosure closure = new LamaClosure(template.function, captures);
        memo.put(template, closure);

        for (int i = 0; i < template.captureSpecs.length; i++) {
            Object value = resolver.resolve(template.captureSpecs[i]);
            if (value instanceof LamaFunctionTemplate nestedTemplate) {
                value = instantiate(nestedTemplate, resolver, memo);
            }
            captures[i] = value;
        }

        return closure;
    }
}
