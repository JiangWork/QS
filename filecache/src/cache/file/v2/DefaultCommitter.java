package cache.file.v2;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultCommitter implements Committer {

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
            System.out.println("Submitted task: " + task);
        } catch (InterruptedException e) {
            // log
        }        
    }
    
    public void start() {
        shouldRun = true;
        worker.start();
    }
    
    /**
     * Stop the committer and return the number of unfinished tasks.
     */
    public int stop() {
        shouldRun = false;
        worker.interrupt();
        try {
            worker.join();
        } catch (InterruptedException e) {
            // who cares?
        }
        return size();
    }
    
    public int size() {
        return queue.size();
    }
    
    private class Worker extends Thread {
        public Worker() {
            setName("CommitterWorker");
        }
        
        public void run() {
            System.out.println("Starting thread " + this.getName());
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
            System.out.println("Exiting thread " + this.getName() + " interrupted " + isInterrupted() + " remaining " + size());
        }
        
        /**
         * Move the staging cache to destination (1) new cache directory, (2) update symbolic file.
         * 
         * @param task
         */
        public void execute(Task task) {
            System.out.println("Executing task: " + task);
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
        //submitter.start();
        
    }
}
