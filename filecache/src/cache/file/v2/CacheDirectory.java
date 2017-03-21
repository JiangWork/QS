package cache.file.v2;

import java.util.Calendar;

/**
 * A CacheDirectory generates the Path
 * 
 *
 * @author jiangzhao
 * @date Mar 14, 2017
 * @version V1.0
 */
public class CacheDirectory {
    public static final String CACHE_DIR_NAME_PREFIX = "cache";
    public static final String CACHE_DIRECTORY = "/kla/klaS";
    public static final String CACHE_STAGING_DIRECTORY = "/kla/klaS/.staging";
 //   private static final int PATH_CACHE_THRESHOLD = 1000;
    private PathGenerator pathGenerator;
    
 //   private Map<String, SoftReference<Path>> pathCache;
    
    
    static {
        
    }
    
    private CacheDirectory() {
        pathGenerator = new DefaultPathGenerator();
  //      pathCache = new ConcurrentHashMap<String, SoftReference<Path>>();
    }
    
    public Path getPath(String identity) {
//        if (pathCache.containsKey(identity)) {
//            
//        }
        Path newPath = pathGenerator.getPath(identity);
//        pathCache.put(identity, new SoftReference<Path>(newPath));
        return newPath;
    }
    
    public String getStagingPath(String identity) {
        return pathGenerator.getStagingPath(identity);
    }
    
    public static String cacheDirName(String identity, String version) {
        return cacheDirNamePrefix(identity) + "_" + version;
    }
    
    public static String cacheDirNamePrefix(String identity) {
        return CACHE_DIR_NAME_PREFIX + "_" +  identity ;
    }
    
    public static String getCacheVersion(String cacheAbsPath) {
        int index = cacheAbsPath.lastIndexOf("/");
        String name = cacheAbsPath;
        if (index != -1) {
            name = cacheAbsPath.substring(index+1); 
        }
        index = name.lastIndexOf("_");
        return index != -1? name.substring(index+1) : "";
    }
    
    /**
     * A Path generator which takes date as a part of its path.
     * 
     */
    private class DefaultPathGenerator implements PathGenerator {


        @Override
        public Path getPath(String identity) {
            // check if it is new identity
            // else re-use the Path
            return newPath(identity);
        }

        private Path newPath(String identity) {
            Calendar calendar = Calendar.getInstance();
            String month = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
            Path path = new Path(CACHE_DIRECTORY + "/" + month, identity);
            return path;
        }
        
        @Override
        public String getStagingPath(String identity) {
            // TODO Auto-generated method stub
            return CACHE_STAGING_DIRECTORY + "/" + identity + "_" + System.currentTimeMillis();
        }

    }
    
}
