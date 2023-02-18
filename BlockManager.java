// General Imports
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

// Exception Imports



public class BlockManager {
    private final BlockingDeque<Block> blockPool;
    // TODO: Rename to seq num.
    private final AtomicInteger blockNumber = new AtomicInteger(0);
    private final ZipConfiguration config;

    public BlockManager(ZipConfiguration config) {
        this.config = config;
        int poolSize = config.getBlockPoolSize();
        blockPool = new LinkedBlockingDeque<Block>(poolSize);
        
        // TODO: tune this.
        // Initialize blockpool with recyclable blocks.
        for( int i = 0; i < poolSize; i++ ) {
            blockPool.addFirst(new Block(config));
        }
    }

    /**
     * Called primarily from Read task. 
     * 
     * Get a block from the block pool. takeFirst blocks until 
     * a block becomes available.
     * 
     * @return an initialized Block object ready for fresh use.
     * @throws InterruptedException
     */
    public Block getBlockFromPool() throws InterruptedException {
        Block block = blockPool.takeFirst();
        block.initialize(config); // Allocates block memory not already alloc'd
        block.blockNumber = blockNumber.getAndIncrement();
        return block;
    }

    /**
     * Called from WritePIGZipTask.
     * 
     * This implementation currently blocks the write thread which
     * bottlenecks output.
     * @param block
     * @throws InterruptedException
     */
    public void recycleBlockToPool(Block block) throws InterruptedException {
        // FIXME: Do not wait in caller thread. Dispatch this to another thread.
        block.waitUntilCanRecycle();
        releaseBlockToPool(block);
    }

    /**
     * Called by BlockManager and ReadTask when a read issue occurs
     * and in WritePIGZipTask after the compressed data has been
     * written to the outputStream.
     * 
     * @param block Block object to be returned to the block pool
     * @throws InterruptedException
     */
    public void releaseBlockToPool(Block block) throws InterruptedException {
        assert ! blockPool.contains(block);
        block.reset(); 
        blockPool.putFirst(block);
    }
}