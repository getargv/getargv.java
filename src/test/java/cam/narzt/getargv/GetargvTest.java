package cam.narzt.getargv;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Optional;
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
        ArrayList<T> tmp = new ArrayList(Arrays.asList(arr));
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

/**
 * Unit test for Getargv Class.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class GetargvTest {
    static final Getargv getargv = new Getargv();
    static final long pid = ProcessHandle.current().pid();
    static final byte space = " ".getBytes(StandardCharsets.UTF_8)[0];
    static final byte nul = "\0".getBytes(StandardCharsets.UTF_8)[0];

    String[] getArgsAsStrings() {
        String command = ProcessHandle.current().info().command().get();
        String[] expected = ArrayHelper.unshift(ProcessHandle.current().info().arguments().get(),command);
        return expected;
    }

    byte[][] getArgs() {
        String[] expected = getArgsAsStrings();
        return Stream.of(expected).map(s -> s.getBytes()).toArray(byte[][]::new);
    }

    byte[] concatBytes(int c) {
        return concatBytes(c, 0);
    }

    byte[] concatBytes(int c, int skip) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for(byte[] b: getArgs()) {
            if (skip == 0){
                try {
                    outputStream.write(b);
                } catch(IOException e) {
                    fail("error creating byte[] of args");
                }
                outputStream.write(c);
            } else {
                skip--;
            }
        }

        byte[] outArray = outputStream.toByteArray();
        if (outArray.length > 0){
            outArray[outArray.length-1] = nul; // last byte is nul even when converting to spaces
        }
        return outArray;
    }

    String concatStrings(char c) {
        return concatStrings(c, 0);
    }

    String concatStrings(char c, int skip) {
        StringBuilder outputString = new StringBuilder();
        for(String s: getArgsAsStrings()) {
            if (skip == 0){
                outputString.append(s);
                outputString.append(c);
            } else {
                skip--;
            }
        }

        if (outputString.length() > 0){
            outputString.setCharAt(outputString.length()-1, '\0'); // last byte is nul even when converting to spaces
        }
        return outputString.toString();
    }

    /**
     * Non-Exception Test of asBytes
     */
    @Test
    public void asBytesShouldNotThrowWhenCalledCorrectly()
    {
        int len = getArgsAsStrings().length;
        assertDoesNotThrow(() -> getargv.asBytes(pid));
        assertDoesNotThrow(() -> getargv.asBytes(pid,0));
        assertDoesNotThrow(() -> getargv.asBytes(pid,len));
        assertDoesNotThrow(() -> getargv.asBytes(pid,0,false));
        assertDoesNotThrow(() -> getargv.asBytes(pid,0,true));
        assertDoesNotThrow(() -> getargv.asBytes(pid,len,false));
        assertDoesNotThrow(() -> getargv.asBytes(pid,len,true));
    }

    /**
     * Exception Test of asBytes
     */
    @Test
    public void asBytesShouldThrowWhenCalledIncorrectly()
    {
        int len = getArgsAsStrings().length;
        assertThrows(IOException.class, () -> getargv.asBytes(-1));
        assertThrows(IOException.class, () -> getargv.asBytes(pid,-1));
        assertThrows(IOException.class, () -> getargv.asBytes(pid,len+1));
        assertThrows(IOException.class, () -> getargv.asBytes(pid,-1,false));
        assertThrows(IOException.class, () -> getargv.asBytes(pid,-1,true));
        assertThrows(IOException.class, () -> getargv.asBytes(pid,len+1,false));
        assertThrows(IOException.class, () -> getargv.asBytes(pid,len+1,true));
    }

    /**
     * return value Test of asBytes
     */
    @Test
    public void asBytesShouldReturnCorrectBytesWithNuls() throws IOException
    {
        int len = getArgsAsStrings().length;
        for (int i = 0; i < len; i++) {
            byte[] actual = getargv.asBytes(pid, i, false);
            byte[] expected = concatBytes(nul, i);
            assertArrayEquals(expected, actual);
        }
    }

    /**
     * return value Test of asBytes
     */
    @Test
    public void asBytesShouldReturnCorrectBytesWithSpaces() throws IOException
    {
        int len = getArgsAsStrings().length;
        for (int i = 0; i < len; i++) {
            byte[] actual = getargv.asBytes(pid, i, true);
            byte[] expected = concatBytes(space, i);
            assertArrayEquals(expected, actual);
        }
    }

    /**
     * Non-Exception Test of asArray
     */
    @Test
    public void asArrayShouldNotThrowWhenCalledCorrectly()
    {
        assertDoesNotThrow(() -> getargv.asArray(pid));
    }

    /**
     * Exception Test of asArray
     */
    @Test
    public void asArrayShouldThrowWhenCalledIncorrectly()
    {
        assertThrows(IOException.class, () -> getargv.asArray(-1));//Not exist
        assertThrows(IOException.class, () -> getargv.asArray(0));//No Perm
        assertThrows(IOException.class, () -> getargv.asArray(1));//No Perm
    }

    /**
     * return value Test of asArray
     */
    @Test
    public void asArrayShouldReturnCorrectArray() throws IOException
    {
        byte[][] actual = getargv.asArray(pid);
        byte[][] expected = getArgs();
        assertEquals(expected.length,actual.length);
        for(int i = 0; i< expected.length; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    /**
     * Non-Exception Test of asArrayOfStrings
     */
    @Test
    public void asArrayOfStringsShouldNotThrowWhenCalledCorrectly(){
        assertDoesNotThrow(() -> getargv.asArrayOfStrings(pid, StandardCharsets.UTF_8));
    }

    /**
     * Exception Test of asArrayOfStrings
     */
    @Test
    public void asArrayOfStringsShouldThrowWhenCalledIncorrectly(){
        assertThrows(IOException.class, () -> getargv.asArrayOfStrings(-1, StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> getargv.asArrayOfStrings(0, StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> getargv.asArrayOfStrings(1, StandardCharsets.UTF_8));
    }

    /**
     * return value Test of asArrayOfStrings
     */
    @Test
    public void asArrayOfStringsShouldReturnCorrectArray() throws IOException {
        String[] actual = getargv.asArrayOfStrings(pid, StandardCharsets.UTF_8);
        String[] expected = getArgsAsStrings();
        assertArrayEquals(expected, actual);
    }

    /**
     * Non-Exception Test of asString
     */
    @Test
    public void asStringShouldNotThrowWhenCalledCorrectly()
    {
        int len = getArgsAsStrings().length;
        assertDoesNotThrow(() -> getargv.asString(pid, StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> getargv.asString(pid, StandardCharsets.UTF_8, 0));
        assertDoesNotThrow(() -> getargv.asString(pid, StandardCharsets.UTF_8, len));
        assertDoesNotThrow(() -> getargv.asString(pid, StandardCharsets.UTF_8, 0, false));
        assertDoesNotThrow(() -> getargv.asString(pid, StandardCharsets.UTF_8, 0, true));
        assertDoesNotThrow(() -> getargv.asString(pid, StandardCharsets.UTF_8, len, false));
        assertDoesNotThrow(() -> getargv.asString(pid, StandardCharsets.UTF_8, len, true));
    }

    /**
     * Exception Test of asString
     */
    @Test
    public void asStringShouldThrowWhenCalledIncorrectly()
    {
        int len = getArgsAsStrings().length;
        assertThrows(IOException.class, () -> getargv.asString(-1, StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> getargv.asString(pid, StandardCharsets.UTF_8, -1));
        assertThrows(IOException.class, () -> getargv.asString(pid, StandardCharsets.UTF_8, len+1));
        assertThrows(IOException.class, () -> getargv.asString(pid, StandardCharsets.UTF_8, -1, false));
        assertThrows(IOException.class, () -> getargv.asString(pid, StandardCharsets.UTF_8, -1, true));
        assertThrows(IOException.class, () -> getargv.asString(pid, StandardCharsets.UTF_8, len+1, false));
        assertThrows(IOException.class, () -> getargv.asString(pid, StandardCharsets.UTF_8, len+1, true));
    }

    /**
     * return value Test of asString
     */
    @Test
    public void asStringShouldReturnCorrectStringWithNuls() throws IOException
    {
        int len = getArgsAsStrings().length;
        for (int i = 0; i < len; i++) {
            String actual = getargv.asString(pid, StandardCharsets.UTF_8, i, false);
            String expected = concatStrings('\0', i);
            assertEquals(expected, actual);
        }
    }

    /**
     * return value Test of asString
     */
    @Test
    public void asStringShouldReturnCorrectStringWithSpaces() throws IOException
    {
        int len = getArgsAsStrings().length;
        for (int i = 0; i < len; i++) {
            String actual = getargv.asString(pid, StandardCharsets.UTF_8, i, true);
            String expected = concatStrings(' ', i);
            assertEquals(expected, actual);
        }
    }
}
