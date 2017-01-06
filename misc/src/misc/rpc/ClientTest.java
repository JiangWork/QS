package misc.rpc;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientTest {
    
    private static AtomicInteger counter = new AtomicInteger();
    
    static class  CallSender extends Thread {
        private Client client;
        public CallSender(Client client) {
            this.client = client;
        }
        public void run() {
            setName("CallSender" + System.currentTimeMillis());
            System.out.println(this + " send call");
            Call call = new Call();
            call.setId(counter.incrementAndGet());
            Call retCall = client.call(call);
            System.out.println(this + " send returned " + retCall.getVal());
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        // TODO Auto-generated method stub
        Client client = new Client();
        List<CallSender> senders = new ArrayList<CallSender>();
        for(int i = 0 ; i < 200; ++i) {
            CallSender c = new CallSender(client);
            senders.add(c);
            c.start();
        }
        for (CallSender cs: senders) {
            cs.join();
        }
        List<Long> list = client.getCallRet();
        Collections.sort(list);
        System.out.println(list);
//        CallSender c1 = new CallSender(client);
//        CallSender c2 = new CallSender(client);
//        CallSender c3 = new CallSender(client);
//        c1.start();
//        c2.start();
//        c3.start();
        
    }

}
