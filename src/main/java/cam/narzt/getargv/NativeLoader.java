package cam.narzt.getargv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.logging.Logger;

/**
 * Class to assist in loading a JNI native lib, tries to load from jar but then
 * falls back to java.library.path if not found.
 */
public final class NativeLoader {

    private static final Logger LOG = Logger.getLogger(NativeLoader.class.toString());

    private NativeLoader() {
        throw new AssertionError();
    }

    /**
     * extended loadLibrary method that looks in the jar as well as the
     * java.library.path
     *
     * @param library the name of the library to load
     * @since 0.1
     * @throws UnsatisfiedLinkError when lib not found or cannot load due to linker
     *                              errors (missing libgetargv on system or bad
     *                              rpath)
     */
    public static void loadLibrary(String library) {
        String libraryName = "lib" + library + ".dylib";
        Class<?> callingClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass();
        LOG.info("called by class: " + callingClass);
        try (InputStream in = callingClass.getClassLoader().getResourceAsStream(libraryName)) {
            if (in != null) {
                LOG.info("trying to load: " + libraryName);
                System.load(saveLibrary(libraryName, in));
            } else {
                throw new IOException("No Resource Found");
            }
        } catch (IOException e) {
            LOG.warning("Could not find library " + library
                    + " as resource, trying fallback lookup through System.loadLibrary");
            System.loadLibrary(library);
        }
    }

    private static String saveLibrary(String libraryName, InputStream in) throws IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }

        File file = File.createTempFile(libraryName + ".", ".tmp", tmpDir);
        file.deleteOnExit();

        try (OutputStream out = new FileOutputStream(file)) {
            LOG.info("trying to save to: " + file.getAbsolutePath());
            in.transferTo(out);
            LOG.info("Saved libfile: " + file.getAbsoluteFile());
            return file.getAbsolutePath();
        }
    }
}
