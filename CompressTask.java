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
            deflator.setDictionary(
                prevBlock.getUncompressed(), 
                Math.min(DICTIONARY_SIZE, prevBlock.getUncompressedSize()-DICTIONARY_SIZE),
                DICTIONARY_SIZE);
            prevBlock.dictionaryDone(); // trigger countdown latch so block can be recycled
        }
    }

    /**
     * Main task loop for the CompressorExecutor when it runs.
     */
    public void run() {
        int ulen = block.getUncompressedSize();
        deflator.setInput(block.getUncompressed());
        compress(Deflater.NO_FLUSH);
        
        if( ! block.isLastBlock() ){
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

    /**
     * Will be run by Compressor.execute
     * @param flushMode 
     * @throws RuntimeException
     */
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
        try {
            return deflator.deflate(buffer, 0, buffer.length, flushMode);
        } catch( Exception e ) {
            throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
        }
    }
}