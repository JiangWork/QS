package tool;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.jobhistory.JobHistoryParser;
import org.apache.hadoop.mapreduce.jobhistory.JobHistoryParser.JobInfo;

public class JobHistoryFileReader {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 1) {
			System.err.println("Missing argument: historyFilePath" );
		}
		
		Configuration conf = new Configuration();
		Path path = new Path(args[0]);
		JobHistoryParser parser = null;		
	      try {
	        parser =
	            new JobHistoryParser(path.getFileSystem(conf),
	            		path);
	        JobInfo jobInfo = parser.parse();
	        jobInfo.printAll();
	      } catch(Exception e) {
	    	  e.printStackTrace();
	      }
	}

}
