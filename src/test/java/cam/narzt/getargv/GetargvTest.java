package cam.narzt.getargv;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Unit test for Getargv Class.
 */
public class GetargvTest {
    static final long pid = ProcessHandle.current().pid();

    /**
     * Non-Exception Test of asBytes
     */
    @Test
    void asBytesShouldNotThrowWhenCalledCorrectlyWith1Arg() {
        assertDoesNotThrow(() -> Getargv.asBytes(pid));
    }

    /**
     * Non-Exception Test of asArray
     */
    @Test
    public void asArrayShouldNotThrowWhenCalledCorrectly() {
        assertDoesNotThrow(() -> Getargv.asArray(pid));
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
     * return value Test of asArrayOfStrings
     */
    @Test
    public void asArrayOfStringsShouldReturnCorrectArray() throws IOException {
        String[] actual = Getargv.asArrayOfStrings(pid, StandardCharsets.UTF_8);
        String[] expected = ArgsHelper.getArgsAsStrings();
        assertArrayEquals(expected, actual);
    }
}
