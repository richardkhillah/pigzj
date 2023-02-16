// Imports

// Exception Imports
import java.io.IOException;
import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.Exception;


class Pigzj {
    private int numProcesses;

    public Pigzj() {
        numProcesses = Runtime.getRuntime().availableProcessors();
    }
    public Pigzj(Args a){
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

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception, IOException {
        Pigzj pj = new Pigzj(new Args(args));


    }
}