package misc.rpc;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements Connection.Listener {

    private Connection conn;
    private Map<Long, Call> callMap;
    private Lock lock;
    private Condition recived;
    
    private List<Long> recivedCallRet = new ArrayList<Long>();
    
    public Client() throws UnknownHostException, IOException {
        callMap = new HashMap<Long, Call>();
        lock = new ReentrantLock();
        recived = lock.newCondition();
        conn = Connection.newConnection();
        conn.add(this);
    }
    
    public  synchronized void addReciveId(long id) {
        recivedCallRet.add(id);
    }
    
    public  synchronized List<Long> getCallRet() {
        return recivedCallRet;
    }
    
    public Call call(Call call) {
        System.out.println(Thread.currentThread() + ": send call " +  call.getId());
        lock.lock();
        try {
            callMap.put(call.getId(), call);
            conn.sendRequestCall(call);
            while(true) {
                try {
                    recived.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Call newCall = callMap.get(call.getId());
                if(newCall.isDone()) {
                    System.out.println(Thread.currentThread() + ": Call " +  call.getId() + 
                            " is done. value " + newCall.getVal() + " returning...");
                    addReciveId(call.getId());
                    return callMap.remove(call.getId());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onComplete(Call call) {
        call.setDone(true);
        lock.lock();
        try {
            Call newCall = callMap.remove(call.getId());
            if (newCall == null) {
                System.out.println(Thread.currentThread() +  ": no such call " + call.getId() );
                return;
            }
            callMap.put(call.getId(), call);
            recived.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    
}
