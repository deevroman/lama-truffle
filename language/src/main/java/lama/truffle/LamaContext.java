package lama.truffle;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class LamaContext {
    private static final TruffleLanguage.ContextReference<LamaContext> REFERENCE =
        TruffleLanguage.ContextReference.create(LamaLanguage.class);

    private final TruffleLanguage.Env env;
    private final Map<String, Object> globals = new HashMap<>();

    public final Scanner input;
    public final PrintWriter output;

    public LamaContext(TruffleLanguage.Env env) {
        this.env = env;
        this.input = new Scanner(new BufferedReader(new InputStreamReader(env.in())));
        this.output = new PrintWriter(env.out(), true);
    }

    public TruffleLanguage.Env env() {
        return env;
    }

    public static LamaContext get(Node node) {
        return REFERENCE.get(node);
    }

    @CompilerDirectives.TruffleBoundary
    public Object getGlobal(String name) {
        return globals.getOrDefault(name, 0L);
    }

    @CompilerDirectives.TruffleBoundary
    public void setGlobal(String name, Object value) {
        globals.put(name, value);
    }
}
