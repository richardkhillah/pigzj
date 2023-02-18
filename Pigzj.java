// Imports
// import java.util.zip.GZIPOutputStream;

// Exception Imports
// import java.io.IOException;
// import java.lang.ArrayIndexOutOfBoundsException;
// import java.lang.Exception;

// import java.io.FileOutputStream;
// import java.io.OutputStream;


class Pigzj {
    private ZipConfiguration config;
    private int numProcesses;

    // TODO: DO I NEED THIS??? I THINK NOT!!!
    public Pigzj() {
        numProcesses = Runtime.getRuntime().availableProcessors();
    }

    public Pigzj(ZipConfiguration c) {
        config = c;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        ZipConfiguration config = new ZipConfiguration(new Args(args));

        Pigzj pj = new Pigzj(config);

        BlockManager blockManager = new BlockManager(config);

        // System.err.println(config.toString());





        // makeTestFile();
        // GZIPOutputStream gzout = new GZIPOutputStream(System.out);
        // System.in.transferTo(gzout);
        // gzout.close();
    }
}