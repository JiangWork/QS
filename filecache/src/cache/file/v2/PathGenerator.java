package cache.file.v2;

/**
 * Generate the {@link Path}.
 * 
 *
 * @author jiangzhao
 * @date Mar 14, 2017
 * @version V1.0
 */
public interface PathGenerator {

    /**
     * Get the {@link Path} given by identity (e.g., FMID).
     * Sub-class can implement different path generation approaches.
     * <p><b>Note:</b>
     * (1) for newly identity, it can generate new Path. 
     * (2) for old identity, it should re-use the existing path.
     * 
     * @param identity
     * @return
     */
    public Path getPath(String identity);
    
    /**
     * Get the staging path for identity. (e.g., FMID)
     * @param identity
     * @return
     */
    public String getStagingPath(String identity);
}
