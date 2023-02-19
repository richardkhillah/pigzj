import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class Compressor {

    protected ZipConfiguration config;
    protected ExecutorService compressExecutor;
    
    Compressor(ZipConfiguration config) {
        this.config = config;
        int nThreads = config.getMaxThreads();
        compressExecutor = new ThreadPoolExecutor(nThreads, // core Pool Size
                                                nThreads,   // max Pool Size
                                                60L, TimeUnit.SECONDS, // Keep alive
                                                new LinkedBlockingQueue<Runnable>());
    }

    /**
     * Called in Pigzj.filter upon reading a block
     * @param block
     * @param prevBlock
     */
    public void compress(Block block, Block prevBlock) {
        compressExecutor.execute(new CompressTask(block, prevBlock, config));
    }

    /**
     * Called in Pigzj when compression is complete and program is shutting down.
     * 
     * @see ExecutorSerivce#shutdown()
     */
    public void shutdown() {
        compressExecutor.shutdown();
    }

    /**
     * Called in Pigzj. Return the list of runnables that were waiting to be
     * compressed.
     */
    public List<Runnable> shutDownNow(){
        return compressExecutor.shutdownNow();
    }
}
