// General Imports
import java.util.zip.CRC32;
import java.util.zip.Checksum;

// Exception Imports

// public class ChecksumTask extends AbstractSerialExecutor implements Runnable {
public class ChecksumTask extends AbstractSerialExecutor {
    protected CRC32 checksum;

    ChecksumTask(CRC32 checksum, ZipConfiguration config) {
        super(config); // AbstractSerialExecutor
        this.checksum = checksum;
        checksum.reset();
    }

    /**
     * Called in AbstractSerialExecutors run() method.
     * 
     * Update the streams cumulative checksum each task.
     * 
     * @param block The block to check.
     */
    protected void process(Block block) {
        checksum.update(block.getUncompressed());
        block.checksumDone();
    }

    public long getChecksumValue() {
        return checksum.getValue();
    }

    public void reset() {
        checksum.reset();
    }
}
