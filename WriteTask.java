// General Imports
import java.io.OutputStream;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

// Exception Imports
import java.io.IOException;

class WriteTask implements Runnable {
    protected ZipConfiguration config;
    private final OutputStream out;
    private final BlockManager blockManager;
    private final BlockingQueue<Block> tasks;
    private volatile boolean finished = false;

    private volatile int checksum;
    private volatile int uncompressedSize = 0;
    private final CountDownLatch trailerSync = new CountDownLatch(2); //trailerSync: checksum & uncompressedSize

    public WriteTask(BlockManager blockManager, OutputStream out, 
        ZipConfiguration config) throws IOException
    {
        super(); // Runnable
        this.config = config;
        this.out = out;
        this.blockManager = blockManager;
        tasks = new LinkedBlockingQueue<Block>(config.getBlockPoolSize());
        
        // Write the compression header
        long mod_time = Instant.now().getEpochSecond();
        byte[] header = ZipMember.makeHeader(config.getCompressionLevel(), mod_time);
        try {
            out.write(header);   
        } catch( IOException e ) {
            throw new IOException(e);
        }
    }

    /**
     * Add a task to the queue
     */
    public void submit(Block block) {
        try {
            tasks.put(block);
        } catch (InterruptedException ignore) {
        }
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
        // Run main writing loop
        while( ! finished ) {
            try {
                // blocking take
                Block block = tasks.take();
                if( block.isLastBlock() ) {
                    finished = true;
                }
                block.waitUntilCanWrite(); // block needs to be fully compressed.
                out.write(block.getCompressed());
                block.writeDone();
        
                blockManager.recycleBlockToPool(block);
            } catch (InterruptedException ignore) {
            } catch (Exception e) {
                throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
            }

        }

        // before finishing things up.
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
}

