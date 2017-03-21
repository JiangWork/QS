package cache.file.v2;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * A Path represents cache layout on the disk. 
 * <p>It usually consists of following content:
 * <p> <b>symbolicFile:</b> the symbolic file linked to latest cache directory
 * <p> <b>cacheDir1:</b> cache data version 1
 * <p> <b>cacheDir2:</b> cache data version 2
 * <p> <b>...</b>
 * <p> <b>cacheDirN:</b> cache data version N
 *  
 * @author jiangzhao
 * @date Mar 14, 2017
 * @version V1.0
 */
public class Path {
 
    private String parent;
    private String cacheDir;
    /**the identity of the cache content this path contains, e.g., FMID**/
    private String identity;
    private String symbolic;
    
    public Path(String parent, String identity) {
        this.parent = parent;
        this.identity = identity;
        this.symbolic = identity;
    }
    
    /**
     * Get the directory for holding latest cache data.
     * @return
     */
    public String nextCacheDir() {
        if (cacheDir != null)   return cacheDir;
        // assign cacheDir
        int nVer = nextVersion();
        cacheDir = CacheDirectory.cacheDirName(identity, nVer+"");
        return cacheDir;
    }
    
    /**
     * Get the next version of cache data.
     * @return
     */
    public int nextVersion() {
        int version = 1;
        File symLink = new File(parent + "/" + symbolic);
        if (!symLink.exists()) {
            String absPath = null;
            try {
                absPath = symLink.getCanonicalPath();
            } catch (IOException e) {
                //log
                absPath = symLink.getAbsolutePath();
            }
            String vStr = CacheDirectory.getCacheVersion(absPath);
            try {
                version = Integer.parseInt(vStr) + 1;
            } catch(NumberFormatException e) {
                // log
            }            
        }
        return version;
    }
    
    /**
     * Get the symbolic file path for this Path.
     * @return
     */
    public String symbolic() {
        return parent + "/" + symbolic;
    }
    
    /**
     * List all the cache directories for this Path.
     * @return
     */
    public List<File> listCacheDirs() {
        List<File> cacheDirs = new ArrayList<File>();
        File file = new File(parent);
        if(file.exists()) {
            File[] files = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    // TODO Auto-generated method stub
                    return pathname.isDirectory() && 
                            pathname.getName().startsWith(CacheDirectory.cacheDirNamePrefix(identity));
                }
                
            });
            cacheDirs = Arrays.asList(files);
        }
        return cacheDirs;
    }

    public String getIdentity() {
        return identity;
    }
    
    public String getCacheDir() {
        return nextCacheDir();
    }
    
    public String getParent() {
        return parent;
    }
    
    /**
     * Create the directory if not exist
     */
    public boolean make() {
        File file = new File(parent);
        if(!file.exists())
            return file.mkdirs();
        return true;
    }

    public boolean exists() {
        File file = new File(parent);
        return file.exists();
    }
    
    public String toString() {
        return parent + ":" + identity;
    }
   }
