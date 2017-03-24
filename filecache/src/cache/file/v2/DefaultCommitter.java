package cache.file.v2;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCommitter implements Committer {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCommitter.class);
    
    private BlockingQueue<Task> queue;
    private volatile boolean shouldRun;
    private Worker worker;
    
    DefaultCommitter() {
        queue = new LinkedBlockingQueue<Task>();
        worker = new Worker();
        shouldRun = true;
    }
    @Override
    public void submit(String from, Path to) {
        Task task = new Task(from, to);
        task.submitTime = System.currentTimeMillis();
        try {
            queue.put(task);
            LOG.info("Submitted task: {}.", task);
        } catch (InterruptedException e) {
            // log
        }        
    }
    
    @Override
    public void start() {
        shouldRun = true;
        worker.start();
    }
    
    @Override
    public void stop() {
        shouldRun = false;
        worker.interrupt();
        try {
            worker.join();
        } catch (InterruptedException e) {
            // who cares?
        }
    }
    
    public int size() {
        return queue.size();
    }
    
    private class Worker extends Thread {
        public Worker() {
            setName("CommitterWorker");
        }
        
        public void run() {
            LOG.info("Starting thread {}.", getName());
            while(shouldRun && !isInterrupted()) {
                Task task = null;
                try {
                    task = queue.take();
                } catch (InterruptedException e) {
                    //log
                    interrupt(); 
                    break;
                }
                execute(task);
            }
            LOG.info("Exiting thread {}: interrupted {} remaining {}.", getName(), isInterrupted(), size());
        }
        
        /**
         * Move the staging cache to destination (1) new cache directory, (2) update symbolic file.
         * 
         * @param task
         */
        public void execute(Task task) {
            LOG.info("Executing task: {}.", task);
            task.startTime = System.currentTimeMillis();
//            final Path path = task.to;
//            path.make();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                interrupt();
                e.printStackTrace();
            }
            task.endTime = System.currentTimeMillis();
        }
    }
    
    private static class Task {
        String from;
        Path to;
        long submitTime;
        long startTime;
        long endTime;
        public Task(String from, Path to) {
            this.from = from;
            this.to = to;
        }
        
        public String toString() {
            return String.format("[%s %s]", from, to);
        }
    }

    public static void main(String[] args) {
        final DefaultCommitter committer = new DefaultCommitter();
        committer.start();
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
                  committer.submit(random.nextInt() + "", new Path(random.nextInt() + "", random.nextInt() + ""));
              }
          }
        };
        submitter.start();
        
    }
}
