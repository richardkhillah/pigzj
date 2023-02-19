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
    //trailerSync: checksum & uncompressedSize
    private final CountDownLatch trailerSync = new CountDownLatch(2);
    //TODO: handle this.
    // private IOException lasException = null;

    public WriteTask(BlockManager blockManager, OutputStream out, 
        ZipConfiguration config) throws IOException
    {
        super(config); //AbstractSerialExecutor
        this.blockManager = blockManager;
        this.out = out;
        
        long mod_time = Instant.now().getEpochSecond();
        byte[] header = ZipMember.makeHeader(config.getCompressionLevel(), mod_time);
        out.write(header);
        // TODO: Write the WriteHeader method somewhere
        // int headerSize = ZipFileStreamUtil.writeHeader(out, 
        //                                 config.getCompressionLevel());

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
        System.err.println("Writing block " + block.blockNumber);
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
            
            System.err.println("WriteTask run making trailer");
            byte[] trailer = ZipMember.makeTrailer(checksum, uncompressedSize);
            System.err.println("WriteTask run writing trailer");
            out.write(trailer);
            System.err.println("WriteTask run trailer write done.");

            out.flush();
        } catch (InterruptedException ignore){
        } catch (IOException e) {
            // TODO: handle exception
            // lastException = e;
            // throw new RuntimeException(e);
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
        System.err.println("Waiting unil block " + block.blockNumber + " can write");
        block.waitUntilCanWrite(); // block needs to be fully compressed.
        System.err.println("block " + block.blockNumber + " can write");
        
        System.err.println("WriteTask process writeCompressedTo block " + block.blockNumber + " " + block.getUncompressed().length + " bytes");
        block.writeCompressedTo(out);
        System.err.println("WriteTask process writeCompressedTo block " + block.blockNumber + " done");
        
        block.writeDone();
        blockManager.recycleBlockToPool(block);
    }


    
    // TODO: Figure this out.
    // public IOException getLasException() {
    //     return lastException;
    // }
}

