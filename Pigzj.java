import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.zip.CRC32;


// Imports
// import java.util.zip.GZIPOutputStream;

// Exception Imports
// import java.io.IOException;
// import java.lang.ArrayIndexOutOfBoundsException;
// import java.lang.Exception;

// import java.io.FileOutputStream;
// import java.io.OutputStream;


class Pigzj extends FilterOutputStream {
    // TODO: Do I need WritableByteChannel?? I THINK NOT.
            // implements WritableByteChannel {
    private final ZipConfiguration config;
    private BlockManager blockManager;
    private ReadTask readTask;
    // private Compressor compressor;
    private WriteTask writeTask;
    private Thread writeThread;
    private ChecksumTask checksumTask;
    private Thread checksumThread;
    private Block previousBlock = null;
    private Block currentBlock = null;
    private boolean closed = false; // If true, stream closed
    
    // TODO: Deal with this later.
    // private Exception lastException = null;

    // public Pigzj(OutputStream out, ZipConfiguration config)
    public Pigzj(InputStream in, OutputStream out, Args args)
                throws IOException {
        super(out);

        config = new ZipConfiguration(args);
        try {
            blockManager = new BlockManager(config);
            readTask = new ReadTask(blockManager);
            readTask.setInput(in);

            // compressor = new Compressor(config);

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
            System.err.println("filter()");
            currentBlock = readTask.getNextBlock();
            while( currentBlock != null ) {
                System.err.println("filter submittting block " + currentBlock.blockNumber);

                CompressTask ctask = new CompressTask(currentBlock, previousBlock, config);
                ctask.run();

                writeTask.submit(currentBlock);
                checksumTask.submit(currentBlock);
                
                previousBlock = currentBlock;
                
                currentBlock = readTask.getNextBlock();
            }
            assert readTask.needsInput();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch( IOException e ){
            throw new RuntimeException(e);
        } catch( RuntimeException e ){
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void close() throws IOException, RuntimeException {
        System.err.println("close()");
        if (closed) {
            System.err.println("closed() closed");
            return;
        }

        readTask.finish();
        filter();

        if(readTask.getUncompressedSize() == 0) {
            System.err.println("close(): readTask.getUncompressedSize() == 0 ");
            try {
                Block empty = blockManager.getBlockFromPool();
                writeTask.submit(empty);
                // write(empty);
            } catch( InterruptedException ignore ) {
            }
        }

        // TODO: Join code belo
        try {
            System.err.println("Pigzj join try close");
            checksumThread.join();


            // System.err.println("Pigzj close setting CRC to " + (int)checksumTask.getChecksumValue() + "(actual value: " + checksumTask.getChecksumValue() + ")");
            writeTask.setCRC((int)checksumTask.getChecksumValue());

            System.err.println("Pigzj close setting uncompressedSize to " + (int)(readTask.getUncompressedSize() & 0xffffffffL));
            writeTask.setUncompressedSize(readTask.getUncompressedSize());

            writeThread.join();
            
            super.close();
            System.err.println("close() readTask.getUncompressedSize() == 0");
        } catch( Exception e ) {
        // } catch( InterruptedException e ) {
            //TODO: Deal with this
        } finally {
            // shutdown compressor
        }


        DebugUtils.debug_m("setting blockManager = null.");
        blockManager = null;
        DebugUtils.debug_m("setting readTask = null.");
        readTask = null;
        writeTask = null;
        writeThread = null;
        checksumTask = null;
        checksumThread = null;
        DebugUtils.debug_m("setting previousBlock = null.");
        previousBlock = null;
        DebugUtils.debug_m("setting currentBlock = null.");
        currentBlock = null;
        DebugUtils. debug_m("setting closed = true.");
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
            // DebugUtils.debug_m("exiting with " + e);
        }



        // makeTestFile();
        // GZIPOutputStream gzout = new GZIPOutputStream(System.out);
        // System.in.transferTo(gzout);
        // gzout.close();
    }
}