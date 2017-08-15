package qs.hadoop;

import java.io.IOException;

import org.apache.hadoop.fs.shell.PathData;
import org.apache.hadoop.conf.Configuration;

public class PathDataTest {
	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		for(String arg: args) {
			PathData[] items = PathData.expandAsGlob(arg, conf);
			for(PathData item: items) {
				System.out.println(item.stat);
			}
		}
		
	}
}
