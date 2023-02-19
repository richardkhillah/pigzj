import java.util.zip.Deflater;

public class ZipConfiguration {
    private static final int MIN_BLOCK_SIZE = 32*1024;// 32k, size of LZ77 sliding window
	private static final int DEFAULT_BLOCK_SIZE = 128*1024;// 128k

	/**
	 * Limit memory usage to ~8 MB
	 */
    private int maxThreads = -32;
	private int blockSize = DEFAULT_BLOCK_SIZE;

	// TODO: Ensure this is set correctly. Try Deflater.BEST_SPEED
	private int compressionLevel = Deflater.DEFAULT_COMPRESSION;
	private int compressionMethod = Deflater.DEFLATED;
	

	private CharSequence comment = null;

    public ZipConfiguration() {
		super();
	}

	public ZipConfiguration(Args a) {
		super();
		setMaxThreads(a);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(32);
		str.append(getClass().getName()).append('[');
		str.append("maxThreads=").append(getMaxThreads()).append(',');
		str.append("blockSize=").append(getBlockSize()/1024.0).append("k");
		if (getCompressionLevel() != Deflater.DEFAULT_COMPRESSION) {
			str.append(",compressionLevel=").append(getCompressionLevel());
		}
		str.append(']');
		return str.toString();
	}

    public int getBlockPoolSize() {
		return 1 + getMaxThreads() * 2;
	}

	public int getBlockSize() {
		return blockSize;
	}
	public void setBlockSize(int blockSize) {
		if (blockSize < MIN_BLOCK_SIZE) {
			throw new IllegalArgumentException("Block size must be at least " + MIN_BLOCK_SIZE);
		}
		this.blockSize = blockSize;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(Args a){
		final int cpuCores = Runtime.getRuntime().availableProcessors();
        int proposedThreadCount = a.getThreads();
        
        if (proposedThreadCount < 0 || (4 * cpuCores ) < proposedThreadCount) {
            System.err.println("Invalid number of threads: Must use between " 
                + cpuCores + " and " + (4 * cpuCores) + " threads...");
            maxThreads = cpuCores;
        } else {
            maxThreads = proposedThreadCount;
        } 
        System.err.println("maxThreads initialized to " + maxThreads);
	}

	/**
	 * @param maxThreads  set to {@code <= 0} to compute optimal number of CPU cores using system load average,
	 *                       capped to {@code abs(maxThreads)}
	 *                       ({@code 0} == uncapped)
	 */
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public int getCompressionLevel() {
		return compressionLevel;
	}
	public void setCompressionLevel(int compressionLevel) {
		this.compressionLevel = compressionLevel;
	}

	public int getCompressionMethod() {
		return compressionMethod;
	}
	public void setCompressionMethod(int compressionMethod) {
		this.compressionMethod = compressionMethod;
	}

	public CharSequence getComment() {
		return comment;
	}
	public void setComment(CharSequence comment) {
		this.comment = comment;
	}
}