// Java Imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

// Exceptions
import java.lang.Exception;
import java.io.IOException;

class TestFile {

    private static void makeTestFile() throws Exception {
        File dstDir = new File("/u/cs/ugrad/khillah/Developer/cs131/homework/hw3/test");
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        File twoMB = new File(dstDir, "2MiB_of_Zeros");
        OutputStream out = new FileOutputStream(twoMB);

        byte[] oneMB = new byte[1024*1024];
        for(int i = 0; i < 2; i++) {
            out.write(oneMB);
        }
        out.close();
    }

    public static void main(String[] args) throws Exception, IOException {
        makeTestFile();
    }
}