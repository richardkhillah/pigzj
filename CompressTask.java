// General Imports
import java.util.zip.Deflater;

// Exception Imports

class CompressTask implements Runnable {
    private static final int DICTIONARY_SIZE = 32*1024; // 32KiB 
    protected static final int STRIDE = 8*1024; // 8KiB output buffer size
    protected final ZipConfiguration config;
    protected final byte[] buffer = new byte[STRIDE];
    protected final Block block;
    protected final Deflater deflator; // Compression Deflator

    CompressTask(Block block, Block prevBlock, ZipConfiguration config) {
        this.config = config;
        this.block = block;
        deflator = new Deflater(config.getCompressionLevel(), true);

        if( prevBlock != null ){
             // prime compression dictionary with last 32KiB of previous block
            System.err.println("CompressTask.setDictionary: block " + block.blockNumber + " size: " + block.getUncompressedSize() + "; prevBlock: " + prevBlock.blockNumber + " size " + prevBlock.getUncompressedSize());
            deflator.setDictionary(
                prevBlock.getUncompressed(), 
                Math.min(DICTIONARY_SIZE, prevBlock.getUncompressedSize()-DICTIONARY_SIZE),
                // prevBlock.getUncompressedSize()-DICTIONARY_SIZE,
                DICTIONARY_SIZE);
            System.err.println("CompressTask.prevBlock " + prevBlock.blockNumber + " .dictionaryDone()");
            prevBlock.dictionaryDone(); // trigger countdown latch so block can be recycled
        }
    }

    /**
     * Main task loop for the CompressorExecutor when it runs.
     */
    public void run() {
        int ulen = block.getUncompressedSize();
        System.err.println("CompressTask setInput block " + block.blockNumber + " uncompressed length " + ulen);
        deflator.setInput(block.getUncompressed());
        System.err.println("CompressTask input set block " + block.blockNumber + " uncompressed length " + ulen);
        System.err.println("CompressTask block deflate block " + block.blockNumber);
        compress(Deflater.NO_FLUSH);
        System.err.println("CompressTask block " + block.blockNumber + " deflated");
        
        if( ! block.isLastBlock() ){
            System.err.println("CompressTask block " + block.blockNumber + " ! isLastBlock");
            compress(Deflater.SYNC_FLUSH);
            assert deflator.needsInput() : "Deflater synced by still has input";
        } else {
            // last block, wrap things up
            deflator.finish();
            while( ! deflator.finished() ){
                compress(Deflater.NO_FLUSH);
            }
        }
        assert deflator.getTotalIn() == block.getUncompressedSize() : "Expected "+block.getUncompressedSize();
        deflator.end();
        block.compressionDone();
    }

    protected void compress( int flushMode ) throws RuntimeException {
        int len;
        do {
            len = deflate(flushMode);
            if( 0 < len ) {
                block.writeCompressed(buffer,0, len);
            }
        } while( len != 0 );
    }

    private int deflate(int flushMode) {
        if( buffer == null ){
            throw new NullPointerException();
        }
        // if( buffer.length < 0 ){
        //     System.err.println("deflate block " + block.blockNumber + " compressed size: " + block.getUncompressed().length);
        //     System.err.println("deflate 0 < buffer.length => 0 < " + buffer.length);
        //     System.exit(-1);
        //     throw new ArrayIndexOutOfBoundsException();
        // }
        try {
            System.err.println("deflate buffer.length: " + buffer.length);
            return deflator.deflate(buffer, 0, buffer.length, flushMode);
        } catch( Exception e ) {
            throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
        }
    }
}