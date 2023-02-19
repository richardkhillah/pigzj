// Java Imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

// Exceptions
import java.lang.Exception;
import java.io.IOException;

class TestFile {

    private static void makeTestFile() throws Exception {
        File dstDir = new File("/u/cs/ugrad/khillah/Developer/cs131/homework/hw3/test");
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }

        Random rand = new Random();

        File twoMB = new File(dstDir, "2MiB_of_Zeros");
        OutputStream out = new FileOutputStream(twoMB);

        byte[] oneMB = new byte[1024*1024];
        rand.nextBytes(oneMB);
        out.write(oneMB);
        out.close();
        
        File non_2s_comp = new File(dstDir, "2MiB_of_Zeros");
        out = new FileOutputStream(non_2s_comp);

        byte[] non_2s = new byte[3*1027*1025];
        rand.nextBytes(non_2s);
        out.write(non_2s);
        out.close();
    }

    public static void main(String[] args) throws Exception, IOException {
        makeTestFile();
    }
}