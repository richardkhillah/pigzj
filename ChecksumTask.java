// General Imports
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.Checksum;

// Exception Imports

public class ChecksumTask implements Runnable {
    protected ZipConfiguration config;
    private final BlockingQueue<Block> tasks;
    protected Checksum checksum;
    private volatile boolean finished = false;


    ChecksumTask(Checksum checksum, ZipConfiguration config) {
        super(); // Runnable
        this.config = config;
        tasks = new LinkedBlockingQueue<Block>(config.getBlockPoolSize());
        this.checksum = checksum;
        checksum.reset();
    }

     /**
     * Submit a task to the (ordered) array queue
     * @param block the Block object to be submitted to the array queue
     */
    public void submit(Block block) {
        try {
            tasks.put(block);
        } catch (InterruptedException ignore) {
        }
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

    /**
     * As required by Runable.
     * 
     * Implmenters of thie interface too, should override this run
     * method however, calling super().run() before executing any
     * Implementer run code.
     */
    public void run() {
        while( ! finished ) {
            try {
                // blocking take
                Block block = tasks.take();
                if( block.isLastBlock() ) {
                    finished = true;
                }
                // process(block);
                checksum.update(block.getUncompressed(), 0, block.getUncompressedSize());
                block.checksumDone();
            } catch (InterruptedException ignore) {
            } catch (Exception e) {
                throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
            }
        }
    }
}
