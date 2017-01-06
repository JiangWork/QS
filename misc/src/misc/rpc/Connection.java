package misc.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Connection {
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private List<Listener> listeners;
    private BlockingQueue<Call> pendingCalls;
    private SendingWorker sender;
    private ReadingWorker reader;
    public Connection(Socket socket) {
        this.socket = socket;
        try {
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        listeners = new ArrayList<Listener>();
        pendingCalls = new ArrayBlockingQueue<Call>(100);
        sender = new SendingWorker();
        reader = new ReadingWorker();
        sender.start();
        reader.start();
    }
    
    
    public void stop() {
        sender.interrupt();
        reader.interrupt();
    }
    public void remove(Listener listener) {
        listeners.remove(listener);
    }
    
    public void add(Listener listener) {
        listeners.add(listener);
    }
    
    public void sendRequestCall(Call call) {
        try {
            pendingCalls.put(call);
            //startWorkersIfNecessary();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
//    public synchronized void startWorkersIfNecessary() {
//        if (!sender.isRunning) sender.start();
//        if (!reader.isRunning) reader.start();
//    }
    
    public static Connection newConnection() throws UnknownHostException, IOException {
        Socket socket = new Socket("localhost", 32100);
        Connection con = new Connection(socket);
        return con;
    }
    class SendingWorker extends Thread {
        private boolean isRunning = false;
        
        public void run() {
            setName("SendingWorker");
            System.out.println("Start to send ");
            isRunning = true;
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(os);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while(!Thread.interrupted()) {
                try {
                    Call call = pendingCalls.take();
                    System.out.println(this + " sending call " + call.getId());
                    oos.writeObject(call);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                
            }
        }
    }
    
    class ReadingWorker extends Thread {
        private boolean isRunning = false;
        public void run() {
            setName("ReadingWorker");
            System.out.println("Start to read ");
            isRunning = true;
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(is);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
            
            while(!Thread.interrupted()) {
                try {
                    Object callObj = ois.readObject();
                    Call call = (Call)callObj;
                    System.out.println(this + " read call " + call.getId());
                    for (Listener listener: listeners) {
                        listener.onComplete(call);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                
                
            }
        }
    }
    
    public interface Listener{
        public void onComplete(Call call);
    }
    
    
}
