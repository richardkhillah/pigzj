// General imports
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

// Exception imports
import java.io.IOException;

// Similar format to MessAdmin
public class Block implements Comparable<Block> {
    private volatile byte[] uncompressed;
    private volatile int uncompressedSize;
    private volatile ByteArrayOutputStream compressed;
    protected volatile int blockNumber;
    private volatile boolean lastBlock;
    private volatile boolean compressionDone;
    private volatile boolean checksumDone;
    private volatile boolean dictionaryDone;
    private volatile boolean writeDone;
    private volatile CountDownLatch writeSync;
    private volatile CountDownLatch recycleSync;

    Block(ZipConfiguration config) {
        compressed = new ByteArrayOutputStream(config.getBlockSize());
        initialize();
    }

    protected void initialize() {
        uncompressedSize = 0;
        blockNumber = 0;
        lastBlock = false;
        compressionDone = false;
        checksumDone = false;
        dictionaryDone = false;
        writeDone = false;
        writeSync = new CountDownLatch(1); // Compression needs ot be done
        recycleSync = new CountDownLatch(4); // compression, checksum, write, dictionary all need to be done
    }

    /**
     * Call from BlockManager
     * Allocate blocks on first use in BlockManager.
     * @param config ZipConfiguration object containing blocksize to malloc.
     */
    public void initialize(ZipConfiguration config) {
        if( uncompressed == null ){
            uncompressed = new byte[config.getBlockSize()];
        }
    }

    // Call from WritePIGZipTask
    public void waitUntilCanWrite() throws InterruptedException {
        writeSync.await();
        assert compressionDone;
    }

    // Call from BlockManager
    public void waitUntilCanRecycle() throws InterruptedException {
        recycleSync.await();
        assert compressionDone && checksumDone && writeDone && dictionaryDone;
    }

    /**
     * Called from CompressTask to update block flags and latches
     * 
     * @return size of compressed ByteStreamBuffer
     */
    public int compressionDone() {
        compressionDone = true;
        int compressedSize = compressed.size();
        writeSync.countDown();
        recycleSync.countDown();
        return compressedSize;
    }

    /**
     * Called from ChecksumTask. When checksum on this block has been
     * completed, update flag and countdown latch.
     */
    public void checksumDone() {
        checksumDone = true;
        recycleSync.countDown();
    }

    /**
     * Called from WritePIGZipTask. When compressed block is written
     * to output, flag and countdown latch are updated.
     */
    public void writeDone() {
        writeDone = true;
        recycleSync.countDown();
    }

    /**
     * Called from CompressTask. After last 32KiB of this block has
     * been used for the next blocks compression dictionary,
     * update dictionaryDone and countdown latch so BlockManager can
     * recycle this block.
     */
    public void dictionaryDone() {
        dictionaryDone = true;
        recycleSync.countDown();
    }

    /**
     * Called from BlockManager when recycling a block.
     * Block is reinitialized and entered back into the block pool.
     */
    public void reset() {
        initialize();
        compressed.reset();
    }

    /**
     * Inherited from Comparable
     * @return 
     */
    public int compareTo(Block o) {
        return o.blockNumber - blockNumber;
    }

    /**
     * Called from ReadTask. Read uncompressed data from an
     * Input stream and store in a byte array to be compressed later.
     * 
     * @param input InputStream with input data
     * @return number of bytes read from input
     * @throws IOException
     */
    public int read(InputStream input) throws IOException {
        int totalRead = 0;
        int lastRead = 0;
        while( lastRead != -1 && uncompressedSize < uncompressed.length ) {
            lastRead = input.read(uncompressed, uncompressedSize, 
                                uncompressed.length - uncompressedSize);
            if ( lastRead > 0 ) {
                uncompressedSize += lastRead;
                totalRead += lastRead;
            }
        }
        // if( lastRead == -1 ){
        //     throw new IOException("-1");
        // }
        return totalRead;
    }

    /**
     * Called from ReadTask.
     * 
     * @return true is block is full to capacity.
     */
    public boolean isComplete() {
        return uncompressedSize == uncompressed.length;
    }

    /**
     * Called from CompressTask. Fill the ByteArrayOutputStream buffer.
     * 
     * @param b compressed byte array filled by a deflator.
     * @param offset integer offset into b. Should be 0.
     * @param len number of bytes in b to be written.
     */
    public void writeCompressed(byte b[], int offset, int len) {
        assert ! compressionDone; // Should be in the middle of compression.
        compressed.write(b, offset, len);
    }

    /**
     * Called by WritePIGZipTask.
     * @param out Designated output stream to send compressed data to.
     * @throws IOException
     */
    public void writeCompressedTo(OutputStream out) throws IOException {
        assert compressionDone; // Do not write block is still being compressed.
        compressed.writeTo(out);
    }

    /**
     * Called by CompressTask
     * 
     * @return A blocks uncompressed byte array.
     */
    public byte[] getUncompressed() {
        return uncompressed;
    }

    /**
     * Called in CompressTask and ChecksumTask.
     * 
     * @return The number of uncompressed bytes in uncompressed byte[].
     */
    public int getUncompressedSize() {
        return uncompressedSize;
    }

    /**
     * Called by CompressTask and AbstractSerializer.
     * 
     * @return true if this is the last block read from inputstream.
     */
    public boolean isLastBlock() {
        return lastBlock;
    }

    /**
     * Called by ReadTask upon reading the final block from input, and
     * by Pigzj close when creating an empty block.
     */
    public void setIsLastBlock() {
        if( ! lastBlock ) {
            lastBlock = true;
            dictionaryDone();
        }
    }

}