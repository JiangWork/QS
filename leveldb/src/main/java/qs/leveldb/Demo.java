package qs.leveldb;

import java.io.File;
import java.io.IOException;

import static org.fusesource.leveldbjni.JniDBFactory.*;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class Demo {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		Options options = new Options();
		options.createIfMissing(true);
		DB db = factory.open(new File("example"), options);
		try {
			for (int i = 0; i < 1 << 20; ++i) {
				db.put(bytes(System.currentTimeMillis() + "" + i),   randomBytes());
			}
			db.put(bytes("Tampa"), bytes("rocks"));
			 db.put(bytes("Tampa"), bytes("green"));
			  db.put(bytes("London"), bytes("red"));
			System.out.println(asString(db.get(bytes("Tampa"))));
			
		  // Use the db in here....
		} finally {
		  // Make sure you close the db to shutdown the 
		  // database and avoid resource leaks.
		  db.close();
		}
	}
	
	public static byte[] randomBytes() {
		byte[] bytes = new byte[128];
		for (int i = 0; i < bytes.length; ++i) {
			bytes[i] = (byte) (Math.random() * 128);
		}
		return bytes;
	}

}
