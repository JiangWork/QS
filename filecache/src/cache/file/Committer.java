package cache.file;

public interface Committer {

    /**
     * Commit the cache data, non-blocking fashion.
     * @param from the staging path
     * @param to the destination path
     */
    public void submit(Path from, Path to);
}
