package cache.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockSpinCommitter implements Committer, Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(LockSpinCommitter.class);
    private BlockingQueue<Task> queue = new LinkedBlockingQueue<Task>();
    private List<Task> delayedQueue = new ArrayList<Task>();
    private volatile boolean shouldRun = true; 
    private Thread worker;
    private DelayedWorker delayedWorker;
    private Mover mover;
    
    public LockSpinCommitter() {
        shouldRun = true;
        mover = new Mover();
        delayedWorker = new DelayedWorker();
        worker = new Thread(this);
    }    

    @Override
    public void submit(Path from, Path to) {
        Task task = new Task(from, to);
        queue.offer(task);
    }    
       
    public void start() {
        worker.setName("MainCommitter");
        this.shouldRun = true;
        worker.start();
        delayedWorker.start();
    }
    
    public void stop() {
        LOG.info("Stopping the committer...");
        this.shouldRun = false;
        worker.interrupt();
        delayedWorker.interrupt();
        try {
            worker.join();
            delayedWorker.join();
        } catch (InterruptedException e) {
            //ignored
        }
    }
    
    @Override
    public void run() {
        LOG.debug("Starting the commiter...");
        
        while(shouldRun) {
            try {
                Task task = queue.take();
                Path toPath = task.to;
                if (toPath.hasLocks()) {  // needs wait
                    synchronized(delayedQueue) {
                        delayedQueue.add(task);
                    }
                } else {
                    int exitCode = mover.move(task.from, task.to);
                    LOG.info("commit from {} to {}, exit code {}", task.from.getPath(), task.to.getPath(), exitCode);
                }
            } catch (InterruptedException e) {
               LOG.info("{} is interupted, remaining {} tasks.", Thread.currentThread().getName(), queue.size());
               Thread.currentThread().interrupt(); // re-assign the flag status
            }
        }
    }
    
    private class DelayedWorker extends Thread {
        
        public DelayedWorker() {
            setName("DelayedCommitter");
        }
        
        public void run() {
            while(!isInterrupted() && shouldRun) {
                synchronized(delayedQueue) {
                Iterator<Task> iter = delayedQueue.iterator();
                while(iter.hasNext()) {
                    Task task = iter.next();
                    Path toPath = task.to;
                    System.out.println("Checking " + toPath.getPath());
                    if (toPath.hasLocks()) {  // needs wait
                        task.increaseTry();
                    } else {
                        int exitCode = mover.move(task.from, task.to);
                        iter.remove();
                        LOG.info("commit from {} to {}, exit code {}", task.from.getPath(), task.to.getPath(), exitCode);
                    }
                }
                }
                
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    LOG.error("{} is interupted, remaining {} tasks.", getName(), delayedQueue.size());
                    interrupt();
                }
            }
        }
    }
    public static class Task {
        private Path from;
        private Path to;
        private int tryNum;
        
        public Task(Path from, Path to) {
            this.from = from;
            this.to = to;
            this.tryNum = 0;
        }
        public void increaseTry() {
            ++tryNum;
        }
        public int getTryNum() {
            return tryNum;
        }

    }
    
    public static class Mover {
        private static final boolean IS_LINUX = isLinux();
        private static final boolean IS_WINDOWS = isWindows();
        
        public int move(Path from, Path to) {
            List<String> commands = new ArrayList<String>();
            if(IS_LINUX) {
                commands.add("/bin/bash");
                commands.add("-c");
                commands.add(String.format("rm -rf %s; mv %s %s", to.getPath(), from.getPath(), to.getPath()));
            } else if (IS_WINDOWS) {
                commands.add("CMD");
                commands.add("/C");
                commands.add(String.format("rmdir %s /s /q && move %s %s", to.getPath(), from.getPath(), to.getPath()));
            } else {
                LOG.error("Unsupported OS: " + System.getProperty("os.name"));
                throw new IllegalStateException("Unsupported OS: " + System.getProperty("os.name"));
            }
            String[] cmdArr = commands.toArray(new String[0]);
            LOG.info("Executing commands: " + commands);
            return doMove(cmdArr);
        }
        
        private int doMove(String[] commands) {
            int exitCode = 0;
            ProcessBuilder pb = new ProcessBuilder(commands);
            try {
                Process process = pb.start();
                exitCode = process.waitFor();
                ConsolePrinter stdout = new ConsolePrinter(process.getInputStream());
                ConsolePrinter stderr = new ConsolePrinter(process.getErrorStream());
                stdout.start();
                stderr.start();
                LOG.debug("Exit code {}", exitCode);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }   
            return exitCode;            
        }
        
        private static boolean isLinux() {
            String OS = System.getProperty("os.name").toLowerCase();
            return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
        }
        
        private static boolean isWindows() {
            String OS = System.getProperty("os.name").toLowerCase();
            return (OS.indexOf("win") >= 0);
        }
    }
    
    private static class ConsolePrinter extends Thread {
        private InputStream is;
        public ConsolePrinter(InputStream is) {
            this.is = is;
        }
        
        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inLine = null;
            try {
                while((inLine = br.readLine())!=null) {
                    System.out.println(inLine);
                }
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        LockSpinCommitter committer = new LockSpinCommitter();
        committer.start();
        Path fromPath = Path.make("D:\\demo\\staging\\cache1");
        fromPath.setVersion(2);
        fromPath.updateVersion();
        Path toPath = Path.make("D:\\demo\\cache1");
        toPath.setVersion(1);
        toPath.updateVersion();
        //toPath.lock();
        committer.submit(fromPath, toPath);
        committer.stop();
    }

}
