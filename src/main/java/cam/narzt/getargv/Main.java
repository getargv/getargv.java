package cam.narzt.getargv;

import java.io.IOException;
import cam.narzt.getargv.Getargv;

public class Main {

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
            // byte[][] array = getargv.asArray(pid);
            // for(byte[] text: array){
            //     System.out.write(text);
            //     System.out.write('\0');
            // }
            byte[] text = getargv.asBytes(pid,skip,nuls);
            System.out.write(text);
            System.out.flush();
        } catch (IOException e) {
            // Oh no! Anyway...
        }
    }
}
