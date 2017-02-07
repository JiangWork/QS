package misc.rpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server extends Thread {

    private ServerSocket serverSocket;
    public Server() throws IOException {
        serverSocket = new ServerSocket(32100);
    }
    
    public void run() {
        setName("Server");
        while(!Thread.interrupted()) {
            try {
                System.out.println(this + " listening...");
                Socket socket = serverSocket.accept();
                SocketHandler handler = new SocketHandler(socket);
                handler.start();
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
    }
    
    class SocketHandler extends Thread {
        private Socket socket;
        public SocketHandler(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            setName("SocketHandler");
            List<Call> calls = new ArrayList<Call>();
            ObjectInputStream ois = null;
            ObjectOutputStream oos = null;
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            while(true) {
            try {
                Call call = (Call)ois.readObject();
                //System.out.println(this + "  handling.." + this.hashCode());
                calls.add(call);
//                Thread.sleep(Math.abs(new Random().nextInt()) % 2000 + 1000);
//                call.setVal("Handled by " + this);
//                oos.writeObject(call);
//                System.out.println(this + "  handle done.." + this.hashCode() + " " + call.getVal());
                 if (calls.size() >= 5) {
                   
                   for (Call callTmp: calls) {
                       callTmp.setVal("Handled by " + this + " " + System.currentTimeMillis());
                       oos.writeObject(callTmp);
                   }
                   calls.clear();
                 }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }

}
