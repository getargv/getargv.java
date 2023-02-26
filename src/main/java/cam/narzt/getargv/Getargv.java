package cam.narzt.getargv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Stream;
import java.lang.module.ModuleDescriptor.Version;

/**
 * Class to access the various functions of libgetargv, all methods/constants
 * are static, so there's no reason to instantiate an instance of this class.
 */
public final class Getargv {

    /**
     * This is only here so you cannot instantiate this class
     */
    private Getargv() {
        throw new AssertionError();
    }

    /** The maximum size of the arguments to a process on macOS. */
    static final int ARG_MAX = 1024 * 1024;

    /** The maximum pid value on macOS. */
    static final int PID_MAX = Version.parse(System.getProperty("os.version")).compareTo(Version.parse("10.5")) < 0
            ? 30000
            : 99999;

    static {
        try {
            NativeLoader.loadLibrary("cam_narzt_getargv_Getargv");
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.err.println(e.getMessage());
        }
    }

    // --- Native methods
    /**
     * native binding to get_argv_and_argc_of_pid in libgetargv
     *
     * @param pid the pid to target
     * @return array of byte arrays representing the arguments
     * @since 0.1
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     */
    private static native byte[][] get_argv_and_argc_of_pid(long pid) throws IOException;;

    /**
     * native binding to get_argv_of_pid in libgetargv
     *
     * @param pid  the pid to target
     * @param skip number of leading args to skip
     * @param nuls replace nuls with spaces
     * @return byte array of the arguments
     * @since 0.1
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     */
    private static native byte[] get_argv_of_pid(long pid, long skip, boolean nuls) throws IOException;

    // -- Java methods
    /**
     * get arguments of pid as a byte array
     *
     * @param pid the process id (pid) of the process from which you want the
     *            arguments
     * @return the arguments of `pid` as a byte[], suitable for writing to
     *         stdout/stderr/etc.
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     * @since 0.1
     * @see <a href=
     *      "https://docs.oracle.com/en/java/javase/19/docs/specs/jni/types.html#modified-utf-8-strings">Discussion
     *      of Java string encoding</a>
     */
    public static byte[] asBytes(long pid) throws IOException {
        return asBytes(pid, 0);
    }

    /**
     * get arguments of pid as a byte array
     *
     * @param pid  the process id (pid) of the process from which you want the
     *             arguments
     * @param skip the number of leading arguments to skip over (leave off)
     * @return the arguments of `pid` as a byte[], suitable for writing to
     *         stdout/stderr/etc.
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     * @since 0.1
     * @see <a href=
     *      "https://docs.oracle.com/en/java/javase/19/docs/specs/jni/types.html#modified-utf-8-strings">Discussion
     *      of Java string encoding</a>
     */
    public static byte[] asBytes(long pid, long skip) throws IOException {
        return asBytes(pid, skip, false);
    }

    /**
     * get arguments of pid as a byte array
     *
     * @param pid  the process id (pid) of the process from which you want the
     *             arguments
     * @param skip the number of leading arguments to skip over (leave off)
     * @param nuls whether to replace the nul delimiters with spaces for human
     *             reading
     * @return the arguments of `pid` as a byte[], suitable for writing to
     *         stdout/stderr/etc.
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     * @since 0.1
     * @see <a href=
     *      "https://docs.oracle.com/en/java/javase/19/docs/specs/jni/types.html#modified-utf-8-strings">Discussion
     *      of Java string encoding</a>
     */
    public static byte[] asBytes(long pid, long skip, boolean nuls) throws IOException {
        if (skip < 0 || skip > ARG_MAX) {
            throw new IllegalArgumentException("skip outside valid range: 0-" + ARG_MAX);
        }
        if (pid < 0 || pid > PID_MAX) {
            throw new IllegalArgumentException("pid outside valid range: 0-" + PID_MAX);
        }
        return get_argv_of_pid(pid, skip, nuls);
    }

    /**
     * Get the args of `pid` as an array of byte[] that you can inspect.
     *
     * @param pid the process id (pid) of the process from which you want the
     *            arguments
     * @return the arguments of `pid` as an array of byte[], suitable for inspection
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     * @since 0.1
     * @see <a href=
     *      "https://docs.oracle.com/en/java/javase/19/docs/specs/jni/types.html#modified-utf-8-strings">Discussion
     *      of Java string encoding</a>
     */
    public static byte[][] asArray(long pid) throws IOException {
        if (pid < 0 || pid > PID_MAX) {
            throw new IllegalArgumentException("pid outside valid range: 0-" + PID_MAX);
        }
        return get_argv_and_argc_of_pid(pid);
    }

    /**
     * Get the args of `pid` as a String, note the OS doesn't enforce an encoding on
     * arguments to a process, so there is no guarantee that any particular Charset
     * will be valid.
     *
     * @param pid     the process id (pid) of the process from which you want the
     *                arguments
     * @param charset the encoding to attempt to apply to the arguments
     * @return the arguments of `pid` as a String, suitable for writing to
     *         stdout/stderr/etc if the encoding is correct.
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     * @since 0.1
     * @see <a href=
     *      "https://gehrcke.de/2014/02/command-line-argument-binary-data/">Discussion
     *      of process argument encoding</a>
     */
    public static String asString(long pid, Charset charset) throws IOException {
        return asString(pid, charset, 0);
    }

    /**
     * Get the args of `pid` as a String, note the OS doesn't enforce an encoding on
     * arguments to a process, so there is no guarantee that any particular Charset
     * will be valid.
     *
     * @param pid     the process id (pid) of the process from which you want the
     *                arguments
     * @param charset the encoding to attempt to apply to the arguments
     * @param skip    the number of leading arguments to skip over (leave off)
     * @return the arguments of `pid` as a String, suitable for writing to
     *         stdout/stderr/etc if the encoding is correct.
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     * @since 0.1
     * @see <a href=
     *      "https://gehrcke.de/2014/02/command-line-argument-binary-data/">Discussion
     *      of process argument encoding</a>
     */
    public static String asString(long pid, Charset charset, long skip) throws IOException {
        return asString(pid, charset, skip, false);
    }

    /**
     * Get the args of `pid` as a String, note the OS doesn't enforce an encoding on
     * arguments to a process, so there is no guarantee that any particular Charset
     * will be valid.
     *
     * @param pid     the process id (pid) of the process from which you want the
     *                arguments
     * @param charset the encoding to attempt to apply to the arguments
     * @param skip    the number of leading arguments to skip over (leave off)
     * @param nuls    whether to replace the nul delimiters with spaces for human
     * @return the arguments of `pid` as a String, suitable for writing to
     *         stdout/stderr/etc if the encoding is correct.
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     * @since 0.1
     * @see <a href=
     *      "https://gehrcke.de/2014/02/command-line-argument-binary-data/">Discussion
     *      of process argument encoding</a>
     */
    public static String asString(long pid, Charset charset, long skip, boolean nuls) throws IOException {
        if (skip < 0 || skip > ARG_MAX) {
            throw new IllegalArgumentException("skip outside valid range: 0-" + ARG_MAX);
        }
        if (pid < 0 || pid > PID_MAX) {
            throw new IllegalArgumentException("pid outside valid range: 0-" + PID_MAX);
        }
        return new String(asBytes(pid, skip, nuls), charset);
    }

    /**
     * Get the args of `pid` as an array of Strings, note the OS doesn't enforce an
     * encoding on arguments to a process, so there is no guarantee that any
     * particular Charset will be valid.
     *
     * @param pid     the process id (pid) of the process from which you want the
     *                arguments
     * @param charset the encoding to attempt to apply to the arguments
     * @return the arguments of `pid` as an array of Strings, suitable for
     *         inspection if the encoding is correct.
     * @throws IOException when the underlying library call fails
     *                     (permission/nonexistance/malformed).
     * @since 0.1
     * @see <a href=
     *      "https://gehrcke.de/2014/02/command-line-argument-binary-data/">Discussion
     *      of process argument encoding</a>
     */
    public static String[] asArrayOfStrings(long pid, Charset charset) throws IOException {
        if (pid < 0 || pid > PID_MAX) {
            throw new IllegalArgumentException("pid outside valid range: 0-" + PID_MAX);
        }
        return Stream.of(asArray(pid)).map(b -> new String(b, charset)).toArray(String[]::new);
    }
}
