package lama.truffle;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LamaLanguageArithmeticTest {
    @Test
    void evaluatesIntegerLiteral() throws Exception {
        assertEval("42", 42);
    }

    @Test
    void respectsOperatorPrecedence() throws Exception {
        assertEval("2 + 3 * 4", 14);
    }

    @Test
    void respectsParentheses() throws Exception {
        assertEval("(2 + 3) * 4", 20);
    }

    @Test
    void supportsUnaryMinus() throws Exception {
        assertEval("-5 + 2", -3);
    }

    @Test
    void evaluatesLeftAssociativeOperators() throws Exception {
        assertEval("20 - 3 - 4", 13);
        assertEval("20 / 2 / 2", 5);
    }

    @Test
    void executesStatementsAndBuiltins() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Context context = Context.newBuilder(LamaLanguage.ID)
            .in(new ByteArrayInputStream("6 5".getBytes(StandardCharsets.UTF_8)))
            .out(output)
            .build()) {
            Source source = Source.newBuilder(
                LamaLanguage.ID,
                "var x = read (), y = read (), z = x * y * 3; write (z)",
                "test.lama"
            ).build();

            context.eval(source);
        }

        assertEquals(" >  > 90\n", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void executesWhileAndIf() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Context context = Context.newBuilder(LamaLanguage.ID).out(output).build()) {
            Source source = Source.newBuilder(
                LamaLanguage.ID,
                "var x; x := 0; if x then write(1) else write(2) fi",
                "test.lama"
            ).build();

            context.eval(source);
        }

        assertEquals("2\n", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void executesForLoop() throws Exception {
        assertEval("var i, s; s := 0; for i := 1, i <= 3, i := i + 1 do s := s + i od; s", 6);
    }

    @Test
    void executesRecursiveFunctions() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Context context = Context.newBuilder(LamaLanguage.ID)
            .in(new ByteArrayInputStream("0".getBytes(StandardCharsets.UTF_8)))
            .out(output)
            .build()) {
            Source source = Source.newBuilder(
                LamaLanguage.ID,
                """
                fun test1 () {
                  a := 3
                }

                fun test2 (b) {
                  a := b
                }

                var x, a, b;

                x := read ();
                test1 ();
                write (a);
                test2 (8);
                write (a)
                """,
                "test.lama"
            ).build();

            context.eval(source);
        }

        assertEquals(" > 3\n8\n", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void returnsRuntimeObjectsThroughPolyglotEval() throws Exception {
        try (Context context = Context.newBuilder(LamaLanguage.ID).build()) {
            Source source = Source.newBuilder(LamaLanguage.ID, "1 : {}", "test.lama").build();
            Value value = context.eval(source);

            assertTrue(value.toString().startsWith("LamaSexp[tag=Cons, elements="));
        }
    }

    private static void assertEval(String code, long expected) throws Exception {
        try (Context context = Context.newBuilder(LamaLanguage.ID).build()) {
            Source source = Source.newBuilder(LamaLanguage.ID, code, "test.lama").build();
            long actual = context.eval(source).asLong();
            assertEquals(expected, actual);
        }
    }
}
