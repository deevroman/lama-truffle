#!/usr/bin/env bash

set -u

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
PROJECT_DIR="$SCRIPT_DIR"
TESTS_DIR="$SCRIPT_DIR/tests/regression"
LAMAC="$SCRIPT_DIR/../src/lamac"
RUNTIME_DIR="$SCRIPT_DIR/../runtime"
MAVEN_REPO_DIR=${MAVEN_REPO_DIR:-$SCRIPT_DIR/.m2/repository}
if [ "${JAVA_UNSAFE_MEMORY_ACCESS_ARG+x}" != x ]; then
    JAVA_UNSAFE_MEMORY_ACCESS_ARG=--sun-misc-unsafe-memory-access=allow
fi
CLASSPATH_FILE="$SCRIPT_DIR/.launcher-classpath"

if command -v timeout >/dev/null 2>&1; then
    TIMEOUT_BIN=timeout
elif command -v gtimeout >/dev/null 2>&1; then
    TIMEOUT_BIN=gtimeout
else
    TIMEOUT_BIN=
fi

run_with_timeout() {
    local seconds=$1
    shift

    if [ -n "$TIMEOUT_BIN" ]; then
        "$TIMEOUT_BIN" "$seconds" "$@"
    else
        "$@"
    fi
}

generate_expected_if_missing() {
    local testfile=$1
    local test_input=$2
    local expected_output=$3

    if [ -f "$expected_output" ]; then
        return 0
    fi

    if [ ! -x "$LAMAC" ]; then
        echo "missing expected output for $(basename "$testfile"), and lamac is not available at $LAMAC" >&2
        return 1
    fi

    run_with_timeout 30 "$LAMAC" -I "$RUNTIME_DIR" -i "$testfile" <"$test_input" >"$expected_output" 2>&1
}

run_graal_test() {
    local testfile=$1
    local test_input=$2
    local actual_output=$3

    : >"$actual_output"

    run_with_timeout 30 \
        java \
        "-Xss8m" \
        ${JAVA_UNSAFE_MEMORY_ACCESS_ARG:+"$JAVA_UNSAFE_MEMORY_ACCESS_ARG"} \
        -cp "$GRAAL_LAUNCHER_CLASSPATH" \
        --enable-native-access=ALL-UNNAMED \
        lama.truffle.launcher.LamaMain \
        "$testfile" \
        <"$test_input" \
        >"$actual_output" 2>&1
}

echo "Building Truffle launcher..."
mkdir -p "$MAVEN_REPO_DIR"
if ! mvn -q -Dmaven.repo.local="$MAVEN_REPO_DIR" -DskipTests install -f "$PROJECT_DIR/pom.xml"; then
    echo "failed to build Truffle project" >&2
    exit 1
fi

echo "Resolving launcher classpath..."
if ! mvn \
    -q \
    -Dmaven.repo.local="$MAVEN_REPO_DIR" \
    -f "$PROJECT_DIR/launcher/pom.xml" \
    -DincludeScope=runtime \
    -Dmdep.outputFile="$CLASSPATH_FILE" \
    dependency:build-classpath; then
    echo "failed to resolve Truffle launcher classpath" >&2
    exit 1
fi

GRAAL_LAUNCHER_CLASSPATH="$PROJECT_DIR/launcher/target/classes:$PROJECT_DIR/language/target/classes:$(cat "$CLASSPATH_FILE")"

if [ "$#" -gt 0 ]; then
    testfiles=("$@")
else
    testfiles=("$TESTS_DIR"/test*.lama)
fi

for testfile in "${testfiles[@]}"; do
    if [[ "$testfile" != /* ]]; then
        testfile="$TESTS_DIR/$testfile"
    fi

    if [ ! -f "$testfile" ]; then
        echo "missing test file: $testfile" >&2
        exit 1
    fi

    test_name=$(basename "$testfile")
    test_input=${testfile%.lama}.input
    expected_output=${testfile%.lama}.expected
    actual_output=${testfile%.lama}.graal.actual

    if [ ! -f "$test_input" ]; then
        test_input=/dev/null
    fi

    if ! generate_expected_if_missing "$testfile" "$test_input" "$expected_output"; then
        echo "$test_name: expected output is unavailable"
        exit 1
    fi

    if ! run_graal_test "$testfile" "$test_input" "$actual_output"; then
        echo "$test_name: failed"
        git diff --no-index -- "$expected_output" "$actual_output" || true
        exit 1
    fi

    if cmp -s "$actual_output" "$expected_output"; then
        echo "$test_name: passed"
    else
        echo "$test_name: failed"
        git diff --no-index -- "$expected_output" "$actual_output" || true
        exit 1
    fi
done
