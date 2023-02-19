/**
 * Args.java
 * 
 * Parse the Stdin args passed to main from the cli, searching 
 * for the -p option.
 */
import java.lang.NumberFormatException;

public class Args {
    private int numThreads = -32;

    public Args(String[] args) throws Exception {
        if( args.length % 2 != 0) {
            throw new Exception("Invalid argument: Ensure every flag has corresponding value.");
        }

        // Parse the arguments
        for(int i = 0; i < args.length; i+=2){
            switch (args[i]) {
                case "-p":
                    numThreads = parseThreads(args[i+1]);
                    break;
                default:
                    throw new Exception("Invalid argument " + args[i]);
            }    
        }
    }

    public int parseThreads(String val) throws NumberFormatException {
        try {
            int value = Integer.parseInt(val);
            return value;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Parameter -p <int:processes> must be type int.");
        }
    }

    public int getThreads() { return numThreads; }
}
