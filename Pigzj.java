// General Imports
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.zip.CRC32;

// Exception Imports
import java.io.IOException;

class Pigzj extends FilterOutputStream {
    private final ZipConfiguration config;
    private BlockManager blockManager;
    private ReadTask readTask;
    private Compressor compressor;
    private WriteTask writeTask;
    private Thread writeThread;
    private ChecksumTask checksumTask;
    private Thread checksumThread;
    private Block previousBlock = null;
    private Block currentBlock = null;
    private boolean closed = false; // If true, stream closed
    
    // TODO: Deal with this later.
    // private Exception lastException = null;

    public Pigzj(InputStream in, OutputStream out, Args args)
                throws IOException {
        super(out);

        config = new ZipConfiguration(args);
        try {
            blockManager = new BlockManager(config);
            
            readTask = new ReadTask(blockManager);
            readTask.setInput(in);

            compressor = new Compressor(config);

            writeTask = new WriteTask(blockManager, out, config);
            writeThread = new Thread(writeTask, "Pigzj write thread");

            checksumTask = new ChecksumTask(new CRC32(), config);
            checksumThread = new Thread(checksumTask, "Pigzj checksum thread");

            writeThread.start();
            checksumThread.start();
        } catch( RuntimeException e ) {
            // Handle this
        }
    }

    public void filter() 
    throws IOException, RuntimeException
    {
        try {
            currentBlock = readTask.getNextBlock();
            while( currentBlock != null ) {
                compressor.compress(currentBlock, previousBlock);
                writeTask.submit(currentBlock);
                checksumTask.submit(currentBlock);
                
                previousBlock = currentBlock;
                currentBlock = readTask.getNextBlock();
            }
            assert readTask.needsInput();
        } catch( InterruptedException e) {
            throw new RuntimeException(e);
        } catch( IOException e ){
            throw new RuntimeException(e);
        } catch( RuntimeException e ){
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void close() throws IOException, RuntimeException {
        if (closed) {
            return;
        }

        readTask.finish();
        filter();

        if(readTask.getUncompressedSize() == 0) {
            try {
                Block empty = blockManager.getBlockFromPool();
                writeTask.submit(empty);
            } catch( InterruptedException ignore ) {
            }
        }

        // Join threads before shutting down
        try {
            checksumThread.join();
            writeTask.setCRC((int)checksumTask.getChecksumValue());
            writeTask.setUncompressedSize(readTask.getUncompressedSize());
            writeThread.join();
            super.close();
        } catch( Exception e ) {
        // } catch( InterruptedException e ) {
            //TODO: Deal with this
        } finally {
            // shutdown compressor
            if( compressor != null ){
                compressor.shutDownNow();
                compressor = null;
            }
        }

        // This is a bit of overkill at this stage 
        // since we're not reusing this program.
        blockManager = null;
        readTask = null;
        writeTask = null;
        writeThread = null;
        checksumTask = null;
        checksumThread = null;
        previousBlock = null;
        currentBlock = null;
        closed = true;

        // throw new IOException("Task Complete.");
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        try {
            Pigzj pj = new Pigzj(System.in, System.out, new Args(args));
            pj.filter();
            pj.close();
        } catch(Exception e) {
            throw new Exception(e);
        }
    }
}