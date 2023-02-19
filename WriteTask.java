// General Imports
import java.io.OutputStream;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

// Exception Imports
import java.io.IOException;

// User Defined Imports

class WriteTask extends AbstractSerialExecutor implements Runnable {
    private final BlockManager blockManager;
    private final OutputStream out;
    private volatile int checksum;
    private volatile int uncompressedSize = 0;
    private final CountDownLatch trailerSync = new CountDownLatch(2); //trailerSync: checksum & uncompressedSize

    public WriteTask(BlockManager blockManager, OutputStream out, 
        ZipConfiguration config) throws IOException
    {
        super(config); //AbstractSerialExecutor
        this.blockManager = blockManager;
        this.out = out;
        
        long mod_time = Instant.now().getEpochSecond();
        byte[] header = ZipMember.makeHeader(config.getCompressionLevel(), mod_time);
        out.write(header);
    }

    /**
     * Called from Pigzj when joining thread. Checksum is a final
     * issue dealt with during compression. Checksum is completed
     * once the checksumThread has finshed checking.
     * 
     * Once this has been set, along with uncompressedSize, writeTask
     * can write the compressed footer/trailer.
     * 
     * @param checksum The CRC 32 value of all bytes compressed on stream.
     */
    public void setCRC(int checksum) {
        this.checksum = checksum;
        trailerSync.countDown();
    }

    /**
     * Called from Pigzj during close() after all input blocks have
     * been read and the readTask is no longer running, i.e., after readTask
     * has fully updated the total uncompressedSize of bytes read.
     * 
     * Once this has been set, along with the CRC32 checksum, writeTask
     * can write the compressed footer/trailer.
     * 
     * @param uncompressedSize Total number of input bytes read by readtask.
     */
    public void setUncompressedSize(long uncompressedSize) {
        // rfc1952; ISIZE is the input size modulo 2^32
        int uncompressedSizeInt = (int)(uncompressedSize & 0xffffffffL);
        this.uncompressedSize = uncompressedSizeInt;
        trailerSync.countDown();
    }


    /**
     * Write a single block's uncompressed data to output stream.
     * @param block The block with uncompressed data to write
     * @throws IOException
     * @throws InterruptedException
     */
    public void write(Block block) throws IOException, InterruptedException {
        byte[] data = block.getUncompressed();
        int nBytes = block.getUncompressedSize();
        out.write(data, 0, nBytes);
    }

    /**
     * Run the processing of the write task in super.run() and upon
     * completion of writing the compressed data, sync on the trailer
     * and write the trailer to the output stream before flushing and
     * closing the stream.
     */
    @Override
    public void run() {
        // Run this threads "main loop".
        super.run();

        // Before finishing things up.
        try {
            // the last block was written, now wait for checksum before 
            // and uncompressedSize to be set before writing trailer.
            trailerSync.await();
            
            byte[] trailer = ZipMember.makeTrailer(checksum, uncompressedSize);
            out.write(trailer);
            out.flush();
        } catch (InterruptedException ignore){
        } catch (IOException e) {
            // TODO: handle exception
        }
    }

    /**
     * The task run in the super.run() main loop. Write each block
     * to the output stream then release the block used back to the 
     * block manager to be recycled.
     * 
     * @param block The block containing compressed data to be written.
     */
    @Override
    protected void process(Block block) throws InterruptedException, IOException {
        block.waitUntilCanWrite(); // block needs to be fully compressed.
        block.writeCompressedTo(out);
        block.writeDone();

        blockManager.recycleBlockToPool(block);
    }
}

