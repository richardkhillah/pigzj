/**
 * Inspiration and Structure from MessAdmin. This is a stripped
 * version the clime.messadmin.utils.compress.impl.ReadTask.java
 */

// Core Imports
import java.io.IOException;
import java.io.InputStream;

// Exception Imports

public class ReadTask {

    private final BlockManager blockManager;
    private InputStream inputStream = null;
    private Block currentBlock = null;
    private long uncompressedSize = 0;
    private boolean finish = false;


    public ReadTask(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    /**
     * Called locally when attempting to read a block. We can only
     * read when we have something to read.
     * 
     * @return true if the inputStream is not set. 
     */
    public boolean needsInput() {
        return inputStream == null;
    }

    /**
     * Called from Pigzj main thread as the main segmenter for 
     * the input stream.
     * 
     * @return The next block read from input or null if there no such block.
     * @throws IOException
     * @throws InterruptedException
     */
    public Block getNextBlock() throws IOException, InterruptedException {
        if ( currentBlock == null || ! currentBlock.isComplete() ) {
            currentBlock = readCurrentOrNewBlock();
        }

        // Set in Pigzj.close() via readTask.finish();
        if ( finish ) {
            Block nextBlock = readCurrentOrNewBlock();
            if ( (nextBlock == null || nextBlock == currentBlock) && currentBlock != null ) {
                currentBlock.setIsLastBlock();
            }
            Block block = currentBlock;
            currentBlock = (nextBlock != currentBlock) ? nextBlock : null;
            return block;
        }

        if( currentBlock == null || ! currentBlock.isComplete() ) {
            return null;
        }

        Block nextBlock = readCurrentOrNewBlock();
        if( nextBlock != null ){
            Block block = currentBlock;
            currentBlock = nextBlock;
            return block;
        }

        return null;
    }

    /**
     * Get a block from the blockpool and read bytes from the
     * inputStream into the blocks uncompressed buffer using the
     * blocks read method.
     * 
     * If the block is partially read, we close the inputstream.
     * More graceful handling might be in order.
     * 
     * @return A blockManaged block read from inputStream.
     * @throws IOException
     * @throws InterruptedException
     */
    private Block readCurrentOrNewBlock() throws IOException, InterruptedException {
        if( needsInput() ){
            return (currentBlock != null && ! currentBlock.isComplete() ) ? currentBlock : null;
        }

        // Get a block to work with
        Block block;
        if( currentBlock != null && !currentBlock.isComplete() ){
            block = currentBlock;
        } else {
            block = blockManager.getBlockFromPool();
        }

        // Fill the block if we have an input to read from
        if( ! needsInput() ){
            int bytesRead = block.read(inputStream);
            if( bytesRead > 0) {
                uncompressedSize += bytesRead;
            }
            if( bytesRead == -1 || bytesRead == 0 || ! block.isComplete() ){ // last block
                inputStream = null;
            }
        }

        if( block.getUncompressedSize() <= 0 ){
            // Something went wrong with this block. Return block to blockpool.
            blockManager.releaseBlockToPool(block);
            return (currentBlock != null && ! currentBlock.isComplete()) ? currentBlock : null;
        }

        return block;
    }

    public void setInput(InputStream input) throws IllegalStateException {
        if( ! needsInput() ) {
            throw new IllegalStateException("An open input stream already exists.");
        }
        inputStream = input;
    }

    /**
     * Called by Pigzj.___.
     * No more input is read, so signal to close the writeStream.
     */
    public void finish() {
        finish = true;
    }

    /**
     * Reset ReadTask for new input
     */
    public void reset() {
        finish = false;
        inputStream = null;
        currentBlock = null;
        uncompressedSize = 0;
    }

    public long getUncompressedSize() {
        return uncompressedSize;
    }
}