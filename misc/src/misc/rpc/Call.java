package misc.rpc;

import java.io.Serializable;

public class Call implements Serializable {

    private static final long serialVersionUID = 1L;
    private long id;
    // other parameters like function name, parameters
    private String val;  // the value returned by RPC
    
    private boolean done;

    public synchronized long getId() {
        return id;
    }

    public synchronized void setId(long id) {
        this.id = id;
    }

    public synchronized String getVal() {
        return val;
    }

    public synchronized void setVal(String val) {
        this.val = val;
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized void setDone(boolean done) {
        this.done = done;
    }
    
    
}
