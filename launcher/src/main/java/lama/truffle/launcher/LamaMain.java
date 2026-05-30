package lama.truffle.launcher;

import lama.truffle.LamaLanguage;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public final class LamaMain {
    private LamaMain() {
    }

    public static void main(String[] args) throws IOException {
        Source source;

        if (args.length == 0) {
            source = Source.newBuilder(LamaLanguage.ID, new InputStreamReader(System.in), "<stdin>").build();
        } else if (args.length == 1) {
            source = Source.newBuilder(LamaLanguage.ID, new File(args[0])).build();
        } else {
            throw new IllegalArgumentException("expected zero or one file argument");
        }

        try (Context context = Context.newBuilder(LamaLanguage.ID)
            .in(System.in)
            .out(System.out)
            .allowAllAccess(true)
            .build()) {
            context.eval(source);
        } catch (PolyglotException ex) {
            if (ex.isInternalError()) {
                ex.printStackTrace();
            } else {
                System.err.println(ex.getMessage());
            }
            System.exit(1);
        }
    }
}
