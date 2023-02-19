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
        System.err.println("ChecksumTask updating block " + block.blockNumber);
        long cval = checksum.getValue();
        checksum.update(block.getUncompressed());
        System.err.println("ChecksumTask update block " + block.blockNumber + " from cval " + cval + " to " + checksum.getValue() + "; block.getUncompressedSize(): " + block.getUncompressedSize());

        assert block.getUncompressed().length == block.getUncompressedSize() : "NUMBERS DON'T MATCH";
        System.err.println("ChecksumTask block " + block.blockNumber + " update complete.");
        block.checksumDone();
        System.err.println("ChecksumTask.checksumDone for block " + block.blockNumber);

    }

    public long getChecksumValue() {
        return checksum.getValue();
    }

    public void reset() {
        checksum.reset();
    }
}
