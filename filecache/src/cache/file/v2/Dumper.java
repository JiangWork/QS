package cache.file.v2;

/**
 * 
 * A dumper dumps cache to staging directory.
 *
 * @author jiangzhao
 * @date Mar 14, 2017
 * @version V1.0
 */
public interface Dumper {
    
    /**
     * Submit the dump work to dumper.
     * A concrete class may de-duplicate the work according to {@code identity}. 
     * @param file the file needs to dump the cache.
     * @param identity the identity of this work, usually a primary key.
     */
    public void submit(String file, String identity);
}
