package cache.file.v2;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DefaultDumper is an concrete implementation of {@link Dumper}.
 * It has unlimited capacity to accept tasks.
 * It de-duplicates the tasks according to the {@code identity}.
 *
 * By default, it uses {@link DefaultCommitter} and {@link DefaultExecutor}.
 * @author jiangzhao
 * @date Mar 20, 2017
 * @version V1.0
 */
public class DefaultDumper implements Dumper {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDumper.class);

    private List<Task> queue;
    private volatile boolean shouldRun;
    private final Worker worker;
    private final Committer committer;
    private final Executor executor;
    
    // lock utilities
    private final Lock lock;
    private final Condition notEmpty;

    
    public DefaultDumper() {
        this(new DefaultCommitter(), new DefaultExecutor());
    }
    
    public DefaultDumper(Committer committer, Executor executor) {
        queue = new LinkedList<Task>();
        shouldRun = true;
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        worker = new Worker();
        this.committer = committer;
        this.executor = executor;
    }
    
    
    @Override
    public void submit(String file, String identity) {
        // TODO Auto-generated method stub
        Task task = new Task(file, identity);
        task.submitTime = System.currentTimeMillis();
        lock.lock();
        try {
            // de-duplicate
            boolean found = false;
            for (Task qt: queue) {
                if(qt.identity.equals(identity)) {
                    LOG.info("Queued task found (id:{}), changed {} to {}.", identity,  qt.file, file);
                    found = true;
                    qt.file = file;
                }
            }
            if (!found) {
                LOG.info("Adding task to queue:{}.", task);
                queue.add(task);
            }
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void start() {
        shouldRun = true;
        worker.start();
        committer.start();
    }
    
    @Override
    public void stop() {
        shouldRun = false;
        worker.interrupt();
        try {
            worker.join(1000);
        } catch (InterruptedException e) {
            //log
        }
        committer.stop();
    }
    
    
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
    
    class Worker extends Thread {
        public Worker() {
            setName("DumpWorker");
        }
        
        // execute task from queue
        public void run() {
            LOG.info("Starting thread {}.", getName());
            while(shouldRun && !isInterrupted()) {
                Task obtainedTask = null;
                try {
                    obtainedTask = getTask();
                } catch (InterruptedException e) {
                    interrupt();
                    break;
                }
                if (obtainedTask == null) {
                    LOG.debug("Obtained task is null. Continuing ...");
                }
                final String id = obtainedTask.identity;
                String stagingDir = CacheDirectory.getInstance().getStagingPath(id);
                execute(obtainedTask, stagingDir); 
                // commit the task
                Path toPath = CacheDirectory.getInstance().getPath(id);
                committer.submit(stagingDir, toPath);
            }
            LOG.info("Exiting thread {}: interrupted {} remaining {}.", getName(), isInterrupted(), size());
        }
        
        
        public Task getTask() throws InterruptedException {
            Task task = null;            
            lock.lockInterruptibly();
            try {
                while(queue.isEmpty()) {
                    notEmpty.await();
                }
                task = queue.remove(0);
            } finally {
                lock.unlock();
            }
            return task;
        }
        
        public void execute(Task task, String stagingDir) {
            LOG.info("Executing task: {}, staging {}.", task, stagingDir);
            task.startTime = System.currentTimeMillis();
            Executor.Context ctx = new Executor.Context(
                    task.file, task.identity, stagingDir
                    );
            executor.execute(ctx);
            task.endTime = System.currentTimeMillis();
        }
    }
    
    private static class Task {
        String file;
        String identity;
        long submitTime;
        long startTime;
        long endTime;
        public Task(String file, String identity) {
            this.file = file;
            this.identity = identity;
        }
        
        public String toString() {
            return "[" + identity + " " + file + "]";
        }
        
    }
    
    public static void main(String[] args) throws InterruptedException {
        final DefaultDumper dumper = new DefaultDumper();
        dumper.start();
        Thread submitter = new Thread() {
            public void run() {
                Random random = new Random();
                while(true) {
                    try {
                        Thread.sleep(Math.abs(random.nextInt() % 1000));
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    dumper.submit(random.nextInt()+"", random.nextInt() + "");
                }
            }
        };
        submitter.start();
        System.out.println(dumper.size());
        submitter.join();
        
//        Thread.sleep(4500);
//        dumper.stop();
    }
    
}
