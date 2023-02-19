// General Imports
// import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;

// Exception Imports

// TODO: Possibly throw this into each implementing class
public abstract class AbstractSerialExecutor implements Runnable {
    private final BlockingQueue<Block> tasks;
    private volatile boolean finished = false;
    protected ZipConfiguration config;

    public AbstractSerialExecutor(ZipConfiguration config) {
        super();
        this.config = config;
        // tasks = new ArrayBlockingQueue<Block>(config.getBlockPoolSize());
        tasks = new LinkedBlockingQueue<Block>(config.getBlockPoolSize());
    }

    /**
     * Submit a task to the (ordered) array queue
     * @param block the Block object to be submitted to the array queue
     */
    public void submit(Block block) {
        try {
            tasks.put(block);
        } catch (InterruptedException ignore) {
        }
    }

    /**
     * Flush executor queue and set executor to "finished state".
     */
    public void prepareForInterrupt() {
        finished = true;
        tasks.clear();
    }

    /**
     * As required by Runable.
     * 
     * Implmenters of thie interface too, should override this run
     * method however, calling super().run() before executing any
     * Implementer run code.
     */
    public void run() {
        while( ! finished ) {
            try {
                // blocking take
                Block block = tasks.take();
                if( block.isLastBlock() ) {
                    finished = true;
                }
                process(block);
            } catch (InterruptedException ignore) {
            } catch (Exception e) {
                throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
            }

        }
    }

    /**
     * The task that needs to be executed on the block. This should 
     * likely be called execute()
     * @param block The Block object which to operate/execute on.
     * @throws Exception
     */
    protected abstract void process(Block block) throws Exception;
}
