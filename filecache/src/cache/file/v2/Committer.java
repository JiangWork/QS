package cache.file.v2;


/**
 * 
 * A committer to move the staging cache directory to destination directory. 
 * Implementation can remove old caches.
 * 
 * @author jiangzhao
 * @date Mar 14, 2017
 * @version V1.0
 */
public interface Committer {

    /**
     * Commit the cache data, non-blocking fashion.
     * @param from the staging path
     * @param to the destination path
     */
    public void submit(String from, Path to);
}
