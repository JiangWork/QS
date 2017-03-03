package cache.file;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Path represents file layout on the disk. 
 *
 * @author jiangzhao
 * @date Feb 28, 2017
 * @version V1.0
 */
public class Path {

	private static final Logger LOG = LoggerFactory.getLogger(Path.class);
	private static final String LOCK_PREFIX = ".LOCK";
	private static final String VERSION = ".VERSION";
	
	public static final String SEPARATOR = File.separator;
	public static final LockFileFilter fileFilter = new LockFileFilter();
	
	/**
	 * A thread can lock the cache to prevent the CacheManager to update the cache.
	 * If it needs to lock, it will create a empty file with current time stamp under the cache directory.
	 * 
	 * For other process (written by third party), it can use Version information to judge the cache is expired or not.
	 */
	/** Is this path lock by current Thread or not **/
	private boolean locked;  
	private String lockFileName;  /** The lock file name created by current thread: full path**/
	private String path;   /**the cache path**/
	private int version;  /**The version of the cache data**/
	private boolean versionChanged;
	
	
	private Path(String path) {
		this.path = path;
		this.locked = false;
		this.lockFileName = "";
		this.version = 0;
		this.versionChanged = false;
	}
	
	/**
	 * Create the lock file under cache directory.
	 * If the file already has been created, nothing is done. 
	 * @return true if lock file created or already exists, otherwise false.
	 */
	public boolean lock() {
		if (locked)	return true;
		// create the lock file name
		String randomName = String.format("%s_%d_%d", LOCK_PREFIX, System.currentTimeMillis(), 
				System.identityHashCode(this));
		this.lockFileName = path + SEPARATOR + randomName;
		File file = new File(this.lockFileName);
		try {
			locked = file.createNewFile();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		LOG.debug("lock result: {} file: {}", locked, randomName);
		return locked;
	}
	
	/**
	 * Delete the lock file if this Thread lock the cache data.
	 * Must be invoked after disposing this Path Object.
	 * @return
	 */
	public boolean unlock() {
		if (!locked) return false;
		File file = new File(this.lockFileName);
		locked = !file.delete();
		return !locked;
	}
	
	public void updateVersion() {
		if (!versionChanged) return;
		DataOutputStream dos = null;
		try {
		    dos = new DataOutputStream(new FileOutputStream(new File(path + SEPARATOR + VERSION)));
		    dos.writeInt(getVersion());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (dos != null)
				try {
				    dos.close();
				} catch (IOException e) {
					// ignored
				}
		}
	}
	
	public void resolve() {		
		String versionFile = path + SEPARATOR + VERSION;
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(new File(versionFile)));
			this.version = dis.readInt();
			LOG.debug("Resolving path {} with version {} ", path, version);
		} catch (IOException e) {
			LOG.error("Can't read version info.", e);
		} finally {
		    if (dis != null) {
		        try {
                    dis.close();
                } catch (IOException e) {
                    // ignored
                }
		    }
		}
	}	
	
	/**
	 * Check if this cache data is locked by other Threads or not
	 * @return
	 */
	public boolean hasLocks() {
		File directory = new File(path);
		File[] files = directory.listFiles(fileFilter);
		return files != null && files.length != 0;
	}
	
	public void clearAllLocks() {
		File directory = new File(path);
		File[] files = directory.listFiles(fileFilter);
		if (files != null) {
			for (File file: files) {
				file.delete();
			}
		}
		locked = false;
	}
	
	public void makePath() {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		} else {
			resolve();
		}
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		versionChanged = (this.version != version);
		this.version = version;
	}

	public boolean isLock() {
		return locked;
	}

	public String getPath() {
		return path;
	}

	/**
	 * Make a path, if such path exists, then resolve the version information.
	 * If not, then create the directory.
	 * @param directory
	 * @return
	 */
	public static Path make(String directory) {
		Path path = new Path(directory);
		path.makePath();
		return path;
	}
	
	/**
	 * Parse the exist directory.
	 * @param directory
	 * @return
	 */
	public static Path parse(String directory) {
		Path path = new Path(directory);
		path.resolve();
		return path;
	}

	public static class LockFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			// TODO Auto-generated method stub
			return pathname.getName().startsWith(LOCK_PREFIX);
		}
		
	}
	
	public static void main(String[] args) {
	    
//	    Path path = Path.make("D:\\demo\\cache1");
//	    path.lock();
//	    System.out.println(path.isLock());
//	    path.lock();
//	    path.lock();
//	    System.out.println(path.isLock());
//	    path.unlock();
//	    System.out.println(path.isLock());
//	    path.unlock();
//	    System.out.println(path.isLock());
//	    path.setVersion(4);
//	    path.updateVersion();
	    
	    Path rpath = Path.parse("D:\\demo\\cache1");
	    System.out.println(rpath.getVersion());
	    System.out.println(rpath.hasLocks());
	    rpath.lock();
	    rpath.setVersion(rpath.getVersion() + 10);
	    rpath.updateVersion();
	    System.out.println(rpath.hasLocks());
	    rpath.clearAllLocks();
	}
	
}
