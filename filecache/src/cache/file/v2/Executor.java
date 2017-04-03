package cache.file.v2;

/**
 * A Executor do the actual cache logic. 
 *
 * @author jiangzhao
 * @date Mar 21, 2017
 * @version V1.0
 */
public interface Executor {

    /**
     * Do the cache logic.
     * @param context the parameters needed by this Executor
     * 
     */
    public void execute(Context context);
    
    public static class Context {
        protected String file;
        protected String identity;
        protected String stagingDirectory;
        public Context() {}
        public Context(String file, String identity, String staging) {
            this.file = file;
            this.identity = identity;
            this.stagingDirectory = staging;
        }
    }
}
