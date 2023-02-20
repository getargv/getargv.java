package cam.narzt.getargv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Stream;
import java.lang.module.ModuleDescriptor.Version;

public class Getargv {

    static final int ARG_MAX = 1024 * 1024;

    static final int PID_MAX = Version.parse(System.getProperty("os.version")).compareTo(Version.parse("10.5")) < 0
            ? 30000
            : 99999;

    static {
        try {
            System.loadLibrary("cam_narzt_getargv_Getargv");
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.err.println(e.getMessage());
        }
    }

    // --- Native methods
    private static native byte[][] get_argv_and_argc_of_pid(long pid);

    private static native byte[] get_argv_of_pid(long pid, long skip, boolean nuls);

    // -- Java methods
    public static byte[] asBytes(long pid) throws IOException {
        return asBytes(pid, 0);
    }

    public static byte[] asBytes(long pid, long skip) throws IOException {
        return asBytes(pid, skip, false);
    }

    public static byte[] asBytes(long pid, long skip, boolean nuls) throws IOException {
        if (skip < 0 || skip > ARG_MAX) {
            throw new IllegalArgumentException("skip outside valid range: 0-" + ARG_MAX);
        }
        if (pid < 0 || pid > PID_MAX) {
            throw new IllegalArgumentException("pid outside valid range: 0-" + PID_MAX);
        }
        return get_argv_of_pid(pid, skip, nuls);
    }

    public static byte[][] asArray(long pid) throws IOException {
        if (pid < 0 || pid > PID_MAX) {
            throw new IllegalArgumentException("pid outside valid range: 0-" + PID_MAX);
        }
        return get_argv_and_argc_of_pid(pid);
    }

    // strings are tricky, must deal with encoding:
    // https://docs.oracle.com/en/java/javase/11/docs/specs/jni/types.html
    public static String asString(long pid, Charset charset) throws IOException {
        return asString(pid, charset, 0);
    }

    public static String asString(long pid, Charset charset, long skip) throws IOException {
        return asString(pid, charset, skip, false);
    }

    public static String asString(long pid, Charset charset, long skip, boolean nuls) throws IOException {
        if (skip < 0 || skip > ARG_MAX) {
            throw new IllegalArgumentException("skip outside valid range: 0-" + ARG_MAX);
        }
        if (pid < 0 || pid > PID_MAX) {
            throw new IllegalArgumentException("pid outside valid range: 0-" + PID_MAX);
        }
        return new String(asBytes(pid, skip, nuls), charset);
    }

    public static String[] asArrayOfStrings(long pid, Charset charset) throws IOException {
        if (pid < 0 || pid > PID_MAX) {
            throw new IllegalArgumentException("pid outside valid range: 0-" + PID_MAX);
        }
        return Stream.of(asArray(pid)).map(b -> new String(b, charset)).toArray(String[]::new);
    }
}
