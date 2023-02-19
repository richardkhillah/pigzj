// General Imports
import java.util.zip.Checksum;

// Exception Imports

public class ChecksumTask extends AbstractSerialExecutor {
    protected Checksum checksum;

    ChecksumTask(Checksum checksum, ZipConfiguration config) {
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
        checksum.update(block.getUncompressed(), 0, block.getUncompressedSize());
        block.checksumDone();
    }

    public long getChecksumValue() {
        return checksum.getValue();
    }

    public void reset() {
        checksum.reset();
    }
}
