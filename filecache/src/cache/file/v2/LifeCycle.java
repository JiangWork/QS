package cache.file.v2;

/**
 * A {@LifeCycle} defines service start and stop interfaces.
 * TODO
 *
 * @author jiangzhao
 * @date Mar 21, 2017
 * @version V1.0
 */
public interface LifeCycle {
    
    /**
     * Start the service.
     */
    public void start();
    
    /**
     * Stop the service.
     */
    public void stop();

}
