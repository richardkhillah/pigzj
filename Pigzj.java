// Imports
import java.util.zip.GZIPOutputStream;

// import clime.messadmin.utils.compress.zip.GZipConfiguration;

// Exception Imports
import java.io.IOException;
import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.Exception;

import java.io.FileOutputStream;
import java.io.OutputStream;


class Pigzj {
    private GZipConfiguration config;
    private int numProcesses;

    public Pigzj() {
        numProcesses = Runtime.getRuntime().availableProcessors();
    }

    public Pigzj(Args a) {
        numProcesses = Runtime.getRuntime().availableProcessors();
        int temp = a.getThreads();
        if ((4 * numProcesses ) < temp) {
            System.err.println("Max Processors Exceeded: Must have less than " + (4 * numProcesses) + " processors.");
            System.err.println("Setting default processors to " + numProcesses);
        } else if (0 < temp) {
            numProcesses = temp;
        }
        System.err.println("numProcessors initialized to " + numProcesses);
    }

    public Pigzj(GZipConfiguration c) {
        config = c;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception, IOException {
        
        GZipConfiguration config = new GZipConfiguration(new Args(args));

        Pigzj pj = new Pigzj(config);

        // System.err.println(config.toString());





        // makeTestFile();
        // GZIPOutputStream gzout = new GZIPOutputStream(System.out);
        // System.in.transferTo(gzout);
        // gzout.close();
    }
}