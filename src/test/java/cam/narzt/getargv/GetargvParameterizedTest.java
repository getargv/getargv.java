package cam.narzt.getargv;

import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Parameterized Unit tests for Getargv Class.
 */
public class GetargvParameterizedTest {
    static final long pid = ProcessHandle.current().pid();

    /**
     * Non-Exception Test of asBytes
     */
    @ParameterizedTest
    @MethodSource("asBytesCorrectProvider3")
    void asBytesShouldNotThrowWhenCalledCorrectlyWith3Args(long pid, long skip, boolean nuls) {
        assertDoesNotThrow(() -> Getargv.asBytes(pid, skip, nuls));
    }

    /**
     * Non-Exception Test of asBytes
     */
    @ParameterizedTest
    @MethodSource("asBytesCorrectProvider2")
    void asBytesShouldNotThrowWhenCalledCorrectlyWith2Args(long pid, long skip) {
        assertDoesNotThrow(() -> Getargv.asBytes(pid, skip));
    }

    // The separation of 3 vs 2 arg versions (and 1 arg in non-param test file) is
    // due to https://github.com/junit-team/junit5/issues/2256
    static Stream<Arguments> asBytesCorrectProvider2() {
        int len = ArgsHelper.getArgsAsStrings().length;
        return Stream.of(
                arguments(pid, 0),
                arguments(pid, len));
    }

    static Stream<Arguments> asBytesCorrectProvider3() {
        int len = ArgsHelper.getArgsAsStrings().length;
        return Stream.of(
                arguments(pid, 0, false),
                arguments(pid, 0, true),
                arguments(pid, len, false),
                arguments(pid, len, true));
    }

    /**
     * Exception Test of asBytes
     */
    @ParameterizedTest
    @MethodSource("asBytesIncorrectProvider3")
    public <T extends Throwable> void asBytesShouldThrowWhenCalledIncorrectlyWith3Args(Class<T> exception, long pid,
            long skip) {
        assertThrows(exception, () -> Getargv.asBytes(pid, skip));
    }

    /**
     * Exception Test of asBytes
     */
    @ParameterizedTest
    @MethodSource("asBytesIncorrectProvider2")
    public <T extends Throwable> void asBytesShouldThrowWhenCalledIncorrectlyWith2Args(Class<T> exception, long pid) {
        assertThrows(exception, () -> Getargv.asBytes(pid));
    }

    static Stream<Arguments> asBytesIncorrectProvider3() {
        int len = ArgsHelper.getArgsAsStrings().length;
        return Stream.of(
                arguments(IOException.class, pid, len + 1),
                arguments(IllegalArgumentException.class, pid, -1),
                arguments(IllegalArgumentException.class, pid, Getargv.ARG_MAX + 1));
    }

    static Stream<Arguments> asBytesIncorrectProvider2() {
        return Stream.of(
                arguments(IllegalArgumentException.class, -1),
                arguments(IllegalArgumentException.class, Getargv.PID_MAX + 1));
    }

    /**
     * return value Test of asBytes
     */
    @ParameterizedTest
    @MethodSource("rangeProvider")
    public void asBytesShouldReturnCorrectBytesWithNuls(int i) throws IOException {
        byte[] actual = Getargv.asBytes(pid, i, false);
        byte nul = "\0".getBytes(StandardCharsets.UTF_8)[0];
        byte[] expected = ArgsHelper.concatBytes(nul, i);
        assertArrayEquals(expected, actual);
    }

    static IntStream rangeProvider() {
        int len = ArgsHelper.getArgsAsStrings().length;
        return IntStream.range(0, len);
    }

    /**
     * return value Test of asBytes
     */
    @ParameterizedTest
    @MethodSource("rangeProvider")
    public void asBytesShouldReturnCorrectBytesWithSpaces(int i) throws IOException {
        byte[] actual = Getargv.asBytes(pid, i, true);
        byte space = " ".getBytes(StandardCharsets.UTF_8)[0];
        byte[] expected = ArgsHelper.concatBytes(space, i);
        assertArrayEquals(expected, actual);
    }

    /**
     * Exception Test of asArray
     */
    @ParameterizedTest
    @MethodSource("asArrayIncorrectProvider")
    public <T extends Throwable> void asArrayShouldThrowWhenCalledIncorrectly(Class<T> exception, long pid) {
        assertThrows(exception, () -> Getargv.asArray(pid));
    }

    static Stream<Arguments> asArrayIncorrectProvider() {
        return Stream.of(
                arguments(IOException.class, 0),
                arguments(IOException.class, 1),
                arguments(IllegalArgumentException.class, -1),
                arguments(IllegalArgumentException.class, Getargv.PID_MAX + 1));
    }

    /**
     * Exception Test of asArrayOfStrings
     */
    @ParameterizedTest
    @MethodSource("asArrayIncorrectProvider")
    public <T extends Throwable> void asArrayOfStringsShouldThrowWhenCalledIncorrectly(Class<T> exception, long pid) {
        assertThrows(exception, () -> Getargv.asArrayOfStrings(pid, StandardCharsets.UTF_8));
    }

    /**
     * Non-Exception Test of asString
     */
    @ParameterizedTest
    @MethodSource("asBytesCorrectProvider3")
    public void asStringShouldNotThrowWhenCalledCorrectlyWith3Args(long pid, long skip, boolean nuls) {
        assertDoesNotThrow(() -> Getargv.asString(pid, StandardCharsets.UTF_8, skip, nuls));
    }

    /**
     * Non-Exception Test of asString
     */
    @ParameterizedTest
    @MethodSource("asBytesCorrectProvider2")
    public void asStringShouldNotThrowWhenCalledCorrectlyWith2Args(long pid, long skip) {
        assertDoesNotThrow(() -> Getargv.asString(pid, StandardCharsets.UTF_8, skip));
    }

    /**
     * Exception Test of asString
     */
    @ParameterizedTest
    @MethodSource("asBytesIncorrectProvider3")
    public <T extends Throwable> void asStringShouldThrowWhenCalledIncorrectlyWith3Args(Class<T> exception, long pid,
            long skip) {
        assertThrows(exception, () -> Getargv.asString(pid, StandardCharsets.UTF_8, skip));
    }

    /**
     * Exception Test of asString
     */
    @ParameterizedTest
    @MethodSource("asBytesIncorrectProvider2")
    public <T extends Throwable> void asStringShouldThrowWhenCalledIncorrectlyWith2Args(Class<T> exception, long pid) {
        assertThrows(exception, () -> Getargv.asString(pid, StandardCharsets.UTF_8));
    }

    /**
     * return value Test of asString
     */
    @ParameterizedTest
    @MethodSource("rangeProvider")
    public void asStringShouldReturnCorrectStringWithNuls(int i) throws IOException {
        String actual = Getargv.asString(pid, StandardCharsets.UTF_8, i, false);
        String expected = ArgsHelper.concatStrings('\0', i);
        assertEquals(expected, actual);
    }

    /**
     * return value Test of asString
     */
    @ParameterizedTest
    @MethodSource("rangeProvider")
    public void asStringShouldReturnCorrectStringWithSpaces(int i) throws IOException {
        String actual = Getargv.asString(pid, StandardCharsets.UTF_8, i, true);
        String expected = ArgsHelper.concatStrings(' ', i);
        assertEquals(expected, actual);
    }
}
