package qs.zookeeper.example01;

/**
 * https://zookeeper.apache.org/doc/trunk/javaExample.html
 * 
 * Requirements 
 * 
 * The client has four requirements:
 * 
 * It takes as parameters: the address of the ZooKeeper service the name of a
 * znode - the one to be watched the name of a file to write the output to an
 * executable with arguments. 
 * 
 * It fetches the data associated with the znode and starts the executable. 
 * If the znode changes, the client refetches the contents and restarts the executable. 
 * If the znode disappears, the client kills the executable.
 *
 * @author jiangzhao
 * @date Dec 19, 2016
 * @version V1.0
 */
public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err
                    .println("USAGE: Executor hostPort znode filename program [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String znode = args[1];
        String filename = args[2];
        String exec[] = new String[args.length - 3];
        System.arraycopy(args, 3, exec, 0, exec.length);
        try {
            new Executor(hostPort, znode, filename, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
