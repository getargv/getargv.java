package cam.narzt.getargv;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;
import java.nio.charset.StandardCharsets;

class ArgsHelper {
    public static String[] getArgsAsStrings() {
        String command = ProcessHandle.current().info().command().get();
        return ArrayHelper.unshift(ProcessHandle.current().info().arguments().get(), command);
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
