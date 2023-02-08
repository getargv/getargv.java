package cam.narzt.getargv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Stream;

public class Getargv
{

    static {
        System.loadLibrary("cam_narzt_getargv_Getargv");
    }

    // --- Native methods
    private native byte[][] get_argv_and_argc_of_pid(long pid);
    private native byte[] get_argv_of_pid(long pid, long skip, boolean nuls);

    // -- Java methods
    public byte[] asBytes(long pid) throws IOException { return this.asBytes(pid, 0); }
    public byte[] asBytes(long pid, long skip) throws IOException { return this.asBytes(pid, skip, false); }
    public byte[] asBytes(long pid, long skip, boolean nuls) throws IOException { return this.get_argv_of_pid(pid, skip, nuls); }

    public byte[][] asArray(long pid) throws IOException { return this.get_argv_and_argc_of_pid(pid); }

    // strings are tricky, must deal with encoding: https://docs.oracle.com/en/java/javase/11/docs/specs/jni/types.html
    public String asString(long pid, Charset charset) throws IOException { return this.asString(pid, charset, 0); }
    public String asString(long pid, Charset charset, long skip) throws IOException { return this.asString(pid, charset, skip, false); }
    public String asString(long pid, Charset charset, long skip, boolean nuls) throws IOException { return new String(this.asBytes(pid, skip, nuls), charset); }

    public String[] asArrayOfStrings(long pid, Charset charset) throws IOException { return Stream.of(this.asArray(pid)).map(b -> new String(b,charset)).toArray(String[]::new); }

    // -- Test method
    public static void main(String[] args) {
        long pid = ProcessHandle.current().pid();
        boolean nuls = true;
        int skip = 0;
        for (int i = 0; i<args.length; i++){
            switch (args[i]) {
            case "-s":
                if (i+1 < args.length) {
                    i++;
                    skip = Integer.parseInt(args[i]);
                } else {
                    System.err.println("Unknown argument encountered");
                    System.exit(-1);
                }
            case "-0":
                nuls = false;
            default:
                if (args[i].charAt(0)=='-') {
                    System.err.println("Unknown argument encountered");
                    System.exit(-1);
                } else if (i==args.length-1){
                    pid = Integer.parseInt(args[i]);
                }
            }
        }

        Getargv getargv = new Getargv();
        try {
            byte[] text = getargv.asBytes(pid,skip,nuls);
            System.out.write(text);
            System.out.flush();
        } catch (IOException e) {
            // Oh no! Anyway...
        }
    }
}
