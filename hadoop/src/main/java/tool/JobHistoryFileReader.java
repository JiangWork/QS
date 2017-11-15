package tool;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.TypeConverter;
import org.apache.hadoop.mapreduce.jobhistory.JobHistoryParser;
import org.apache.hadoop.mapreduce.jobhistory.JobHistoryParser.JobInfo;
import org.apache.hadoop.mapreduce.v2.api.records.JobId;
import org.apache.hadoop.mapreduce.v2.jobhistory.JobHistoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a jobid and get the job history file.
 * 
 * jiangzhao
 */
public class JobHistoryFileReader {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobHistoryFileReader.class);
	
	private static final Path NULL_PATH = new Path("/");
	private static final String JHISTORY_SUFFIX = ".jhist";
	
	public static void main(String[] args) throws IllegalArgumentException, IOException {
		// TODO Auto-generated method stub
		if (args.length < 1) {
			System.err.println("Missing argument: jobid" );
			return;
		}
		
		String jobId = args[0];
		JobId id = TypeConverter.toYarn(JobID.forName(jobId));
		Configuration conf = new Configuration();
		Path hsPath = scanIntermediateDoneDirectory(conf, id);
		if(hsPath == NULL_PATH) {
			return;
		}
		if(hsPath == null) {
			hsPath = scanDoneDirectory(conf, id);
		}
		if(hsPath == null) {
			LOGGER.error("Can't find history file for " + id);
			return;
		}
		LOGGER.info("Found: " + hsPath);
		JobHistoryParser parser = null;		
	      try {
	        parser = new JobHistoryParser(hsPath.getFileSystem(conf), hsPath);
	        JobInfo jobInfo = parser.parse();
	        jobInfo.printAll();
	      } catch(Exception e) {
	    	  e.printStackTrace();
	      }
	}
	
	private static Path scanIntermediateDoneDirectory(Configuration conf, JobId id) throws IllegalArgumentException, IOException {
		String intermediateDoneDirPrefix = JobHistoryUtils.getConfiguredHistoryIntermediateDoneDirPrefix(conf);
	    Path intermediateDoneDirPath = FileContext.getFileContext(conf).makeQualified(new Path(intermediateDoneDirPrefix));
	    String userName = System.getProperty("user.name");
	    FileSystem fileSystem = intermediateDoneDirPath.getFileSystem(conf);
	    Path pathPattern = new Path(intermediateDoneDirPath, userName + "/" + id + "*");
	    LOGGER.info("Searching " + pathPattern);
	    FileStatus[] fileStatus = fileSystem.globStatus(pathPattern);
	    for(FileStatus fs: fileStatus) {
	    	if(fs.isFile() && fs.getPath().getName().endsWith(JHISTORY_SUFFIX)) {
	    		return fs.getPath();
	    	}
	    }
	    if(fileStatus.length != 0) {
	    	LOGGER.info("History file is aggerating, please wait and retry.");
	    	return NULL_PATH;
	    }
	    return null;
	}
	
	private static Path scanDoneDirectory(Configuration conf, JobId id) throws IllegalArgumentException, IOException {
		String doneDirPrefix = JobHistoryUtils.getConfiguredHistoryServerDoneDirPrefix(conf);
	    Path doneDirPrefixPath = FileContext.getFileContext(conf).makeQualified(new Path(doneDirPrefix));
	    int serialNumberLowDigits = 3;
		  String serialNumberFormat = ("%0"
		        + (JobHistoryUtils.SERIAL_NUMBER_DIRECTORY_DIGITS + serialNumberLowDigits)
		        + "d");
	    String serialPart = JobHistoryUtils.serialNumberDirectoryComponent(id, serialNumberFormat);
	    Path pathPattern = new Path(doneDirPrefixPath, "*/*/*/" + serialPart + "/"+ id + "*" + JHISTORY_SUFFIX);
	    LOGGER.info("Searching " + pathPattern);
	    FileStatus[] fileStatus = doneDirPrefixPath.getFileSystem(conf).globStatus(pathPattern);
	    if(fileStatus.length != 0) {
	    	return fileStatus[0].getPath();
	    }
	    return null;
	}
	
}
