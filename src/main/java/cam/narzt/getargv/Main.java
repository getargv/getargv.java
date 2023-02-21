package cam.narzt.getargv;

import java.io.IOException;

/**
 * This class exists to hold a main method to test the Getargv class,
 * this implementation of a getargv cli tool is really poor.
 */
public final class Main {

    /**
     * This is only here so you cannot instantiate this class
     */
    private Main() {
        throw new AssertionError();
    }

    /**
     * main method
     *
     * @param args the arguments passed in by the jvm
     */
    public static void main(String[] args) {
        long pid = ProcessHandle.current().pid();
        boolean nuls = true;
        int skip = 0;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s":
                    if (i + 1 < args.length) {
                        i++;
                        skip = Integer.parseInt(args[i]);
                    } else {
                        System.err.println("Unknown argument encountered");
                        System.exit(-1);
                    }
                case "-0":
                    nuls = false;
                default:
                    if (args[i].charAt(0) == '-') {
                        System.err.println("Unknown argument encountered");
                        System.exit(-1);
                    } else if (i == args.length - 1) {
                        pid = Integer.parseInt(args[i]);
                    }
            }
        }

        try {
            byte[] text = Getargv.asBytes(pid, skip, nuls);
            System.out.write(text);
            System.out.flush();
        } catch (IOException e) {
        }
    }
}
