package cam.narzt.getargv;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import java.nio.charset.StandardCharsets;

class ArrayHelper {
    // These are inefficient and badly named, so only use in tests
    public static <T> T[] push(T[] arr, T item) {
        T[] tmp = Arrays.copyOf(arr, arr.length + 1);
        tmp[tmp.length - 1] = item;
        return tmp;
    }

    public static <T> T[] unshift(T[] arr, T item) {
        ArrayList<T> tmp = new ArrayList<T>(Arrays.asList(arr));
        tmp.add(0, item);
        return tmp.toArray(arr);
    }

    public static <T> T[] pop(T[] arr) {
        T[] tmp = Arrays.copyOf(arr, arr.length - 1);
        return tmp;
    }

    public static <T> T[] shift(T[] arr) {
        T[] tmp = Arrays.copyOfRange(arr, 1, arr.length);
        return tmp;
    }
}

class ArgsHelper {
    public static String[] getArgsAsStrings() {
        String command = ProcessHandle.current().info().command().get();
        String[] expected = ArrayHelper.unshift(ProcessHandle.current().info().arguments().get(), command);
        return expected;
    }

    public static byte[][] getArgs() {
        String[] expected = getArgsAsStrings();
        return Stream.of(expected).map(s -> s.getBytes()).toArray(byte[][]::new);
    }

    public static byte[] concatBytes(int c) {
        return concatBytes(c, 0);
    }

    public static byte[] concatBytes(int c, int skip) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] b : getArgs()) {
            if (skip == 0) {
                try {
                    outputStream.write(b);
                } catch (IOException e) {
                    fail("error creating byte[] of args");
                }
                outputStream.write(c);
            } else {
                skip--;
            }
        }

        byte[] outArray = outputStream.toByteArray();
        if (outArray.length > 0) {
            byte nul = "\0".getBytes(StandardCharsets.UTF_8)[0];
            outArray[outArray.length - 1] = nul; // last byte is nul even when converting to spaces
        }
        return outArray;
    }

    public static String concatStrings(char c) {
        return concatStrings(c, 0);
    }

    public static String concatStrings(char c, int skip) {
        StringBuilder outputString = new StringBuilder();
        for (String s : getArgsAsStrings()) {
            if (skip == 0) {
                outputString.append(s);
                outputString.append(c);
            } else {
                skip--;
            }
        }

        if (outputString.length() > 0) {
            outputString.setCharAt(outputString.length() - 1, '\0'); // last byte is nul even when converting to spaces
        }
        return outputString.toString();
    }
}

/**
 * Unit test for Getargv Class.
 */
public class GetargvTest {
    static final long pid = ProcessHandle.current().pid();
    static final byte space = " ".getBytes(StandardCharsets.UTF_8)[0];
    static final byte nul = "\0".getBytes(StandardCharsets.UTF_8)[0];

    /**
     * Non-Exception Test of asBytes
     */
    @Test
    public void asBytesShouldNotThrowWhenCalledCorrectly() {
        int len = ArgsHelper.getArgsAsStrings().length;
        assertDoesNotThrow(() -> Getargv.asBytes(pid));
        assertDoesNotThrow(() -> Getargv.asBytes(pid, 0));
        assertDoesNotThrow(() -> Getargv.asBytes(pid, len));
        assertDoesNotThrow(() -> Getargv.asBytes(pid, 0, false));
        assertDoesNotThrow(() -> Getargv.asBytes(pid, 0, true));
        assertDoesNotThrow(() -> Getargv.asBytes(pid, len, false));
        assertDoesNotThrow(() -> Getargv.asBytes(pid, len, true));
    }

    /**
     * Exception Test of asBytes
     */
    @Test
    public void asBytesShouldThrowWhenCalledIncorrectly() {
        int len = ArgsHelper.getArgsAsStrings().length;
        assertThrows(IOException.class, () -> Getargv.asBytes(pid, len + 1), "too many skipped for pid");

        assertThrows(IllegalArgumentException.class, () -> Getargv.asBytes(-1), "pid too low");
        assertThrows(IllegalArgumentException.class, () -> Getargv.asBytes(Getargv.PID_MAX + 1), "pid too high");

        assertThrows(IllegalArgumentException.class, () -> Getargv.asBytes(pid, -1), "skip too low");
        assertThrows(IllegalArgumentException.class, () -> Getargv.asBytes(pid, Getargv.ARG_MAX + 1), "skip too high");
    }

    /**
     * return value Test of asBytes
     */
    @Test
    public void asBytesShouldReturnCorrectBytesWithNuls() throws IOException {
        int len = ArgsHelper.getArgsAsStrings().length;
        for (int i = 0; i < len; i++) {
            byte[] actual = Getargv.asBytes(pid, i, false);
            byte[] expected = ArgsHelper.concatBytes(nul, i);
            assertArrayEquals(expected, actual);
        }
    }

    /**
     * return value Test of asBytes
     */
    @Test
    public void asBytesShouldReturnCorrectBytesWithSpaces() throws IOException {
        int len = ArgsHelper.getArgsAsStrings().length;
        for (int i = 0; i < len; i++) {
            byte[] actual = Getargv.asBytes(pid, i, true);
            byte[] expected = ArgsHelper.concatBytes(space, i);
            assertArrayEquals(expected, actual);
        }
    }

    /**
     * Non-Exception Test of asArray
     */
    @Test
    public void asArrayShouldNotThrowWhenCalledCorrectly() {
        assertDoesNotThrow(() -> Getargv.asArray(pid));
    }

    /**
     * Exception Test of asArray
     */
    @Test
    public void asArrayShouldThrowWhenCalledIncorrectly() {
        assertThrows(IllegalArgumentException.class, () -> Getargv.asArray(-1), "pid too low");
        assertThrows(IllegalArgumentException.class, () -> Getargv.asArray(Getargv.PID_MAX + 1), "pid too high");
        assertThrows(IOException.class, () -> Getargv.asArray(0), "pid no permission");
        assertThrows(IOException.class, () -> Getargv.asArray(1), "pid no permission");
    }

    /**
     * return value Test of asArray
     */
    @Test
    public void asArrayShouldReturnCorrectArray() throws IOException {
        byte[][] actual = Getargv.asArray(pid);
        byte[][] expected = ArgsHelper.getArgs();
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    /**
     * Non-Exception Test of asArrayOfStrings
     */
    @Test
    public void asArrayOfStringsShouldNotThrowWhenCalledCorrectly() {
        assertDoesNotThrow(() -> Getargv.asArrayOfStrings(pid, StandardCharsets.UTF_8));
    }

    /**
     * Exception Test of asArrayOfStrings
     */
    @Test
    public void asArrayOfStringsShouldThrowWhenCalledIncorrectly() {
        assertThrows(IllegalArgumentException.class, () -> Getargv.asArrayOfStrings(-1, StandardCharsets.UTF_8),
                "pid too low");
        assertThrows(IllegalArgumentException.class,
                () -> Getargv.asArrayOfStrings(Getargv.PID_MAX + 1, StandardCharsets.UTF_8), "pid too high");
        assertThrows(IOException.class, () -> Getargv.asArrayOfStrings(0, StandardCharsets.UTF_8), "pid no permission");
        assertThrows(IOException.class, () -> Getargv.asArrayOfStrings(1, StandardCharsets.UTF_8), "pid no permission");
    }

    /**
     * return value Test of asArrayOfStrings
     */
    @Test
    public void asArrayOfStringsShouldReturnCorrectArray() throws IOException {
        String[] actual = Getargv.asArrayOfStrings(pid, StandardCharsets.UTF_8);
        String[] expected = ArgsHelper.getArgsAsStrings();
        assertArrayEquals(expected, actual);
    }

    /**
     * Non-Exception Test of asString
     */
    @Test
    public void asStringShouldNotThrowWhenCalledCorrectly() {
        int len = ArgsHelper.getArgsAsStrings().length;
        assertDoesNotThrow(() -> Getargv.asString(pid, StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> Getargv.asString(pid, StandardCharsets.UTF_8, 0));
        assertDoesNotThrow(() -> Getargv.asString(pid, StandardCharsets.UTF_8, len));
        assertDoesNotThrow(() -> Getargv.asString(pid, StandardCharsets.UTF_8, 0, false));
        assertDoesNotThrow(() -> Getargv.asString(pid, StandardCharsets.UTF_8, 0, true));
        assertDoesNotThrow(() -> Getargv.asString(pid, StandardCharsets.UTF_8, len, false));
        assertDoesNotThrow(() -> Getargv.asString(pid, StandardCharsets.UTF_8, len, true));
    }

    /**
     * Exception Test of asString
     */
    @Test
    public void asStringShouldThrowWhenCalledIncorrectly() {
        int len = ArgsHelper.getArgsAsStrings().length;
        assertThrows(IllegalArgumentException.class, () -> Getargv.asString(-1, StandardCharsets.UTF_8), "pid too low");
        assertThrows(IllegalArgumentException.class,
                () -> Getargv.asString(Getargv.PID_MAX + 1, StandardCharsets.UTF_8), "pid too high");

        assertThrows(IllegalArgumentException.class,
                () -> Getargv.asString(pid, StandardCharsets.UTF_8, Getargv.ARG_MAX + 1), "skip too high");
        assertThrows(IllegalArgumentException.class, () -> Getargv.asString(pid, StandardCharsets.UTF_8, -1),
                "skip too low");

        assertThrows(IOException.class, () -> Getargv.asString(pid, StandardCharsets.UTF_8, len + 1),
                "skip too many for pid");
    }

    /**
     * return value Test of asString
     */
    @Test
    public void asStringShouldReturnCorrectStringWithNuls() throws IOException {
        int len = ArgsHelper.getArgsAsStrings().length;
        for (int i = 0; i < len; i++) {
            String actual = Getargv.asString(pid, StandardCharsets.UTF_8, i, false);
            String expected = ArgsHelper.concatStrings('\0', i);
            assertEquals(expected, actual);
        }
    }

    /**
     * return value Test of asString
     */
    @Test
    public void asStringShouldReturnCorrectStringWithSpaces() throws IOException {
        int len = ArgsHelper.getArgsAsStrings().length;
        for (int i = 0; i < len; i++) {
            String actual = Getargv.asString(pid, StandardCharsets.UTF_8, i, true);
            String expected = ArgsHelper.concatStrings(' ', i);
            assertEquals(expected, actual);
        }
    }
}
