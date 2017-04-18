package tool;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;

public class YarnlogDumper {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		Configuration conf = new YarnConfiguration();
		YarnClient client = create(conf);
		if(args.length < 1) {
			printUsage();
			System.exit(1);
		}
		String applicationId = "",
				containerId = "",
				logDest = "dump-logs";
		boolean listOnly = false;
		applicationId = args[0];
		for (int i = 1; i < args.length; ++i) {
			if ("-l".equalsIgnoreCase(args[i]) || "-listOnly".equalsIgnoreCase(args[i]))  {
				listOnly = true;
			} else if ("-c".equalsIgnoreCase(args[i])) {
				containerId = args[++i];
			} else if ("-d".equalsIgnoreCase(args[i])) {
				logDest = args[++i];
			} else {
				System.err.println("Ignore option: " + args[i]);
			}
		}
		ApplicationId appId = null;
		try {
			appId = ConverterUtils.toApplicationId(applicationId);
		} catch (Exception e) {
			System.err.println("Invalid ApplicationId specified: " + applicationId);
			System.exit(2);
		}

		File dir = new File(logDest);
		if (dir.exists()) {
			System.err.println("Destination directory exists: " + logDest);
			System.exit(3);
		} else {
			dir.mkdirs();
		}

		// verify 
		try {
			int resultCode = verifyApplicationState(conf, appId);
			if (resultCode != 0) {
				System.err.println("Logs are not avaiable right now.");
				System.exit(4);
			}
		} catch (Exception e) {
			//System.err.println(e.getMessage());
			System.err.println("Unable to get ApplicationState, job maybe retired."
			          + " Attempting to fetch logs directly from the filesystem.");
			// maybe we can fetch job information from jobhistory
		}

		UserGroupInformation ugi = UserGroupInformation.getCurrentUser();
		YarnlogDumperHelper helper = new YarnlogDumperHelper();
		helper.setConf(conf);
		System.out.println("\nFinished, exit code: " + helper.dumpAllContainersLogs(appId, ugi.getShortUserName(), 
				containerId, dir.getAbsolutePath(), listOnly));
	}

	private static int verifyApplicationState(Configuration conf, ApplicationId appId) throws IOException,
	YarnException {
		YarnClient yarnClient = create(conf);

		try {
			ApplicationReport appReport = yarnClient.getApplicationReport(appId);
			switch (appReport.getYarnApplicationState()) {
			case NEW:
			case NEW_SAVING:
			case SUBMITTED:
				return -1;
			case ACCEPTED:
			case RUNNING:
			case FAILED:
			case FINISHED:
			case KILLED:
			default:
				// output application info
				printApplicationReport(appReport);
				break;

			}
		} finally {
			yarnClient.close();
		}
		return 0;
	}

	public static void printApplicationReport(ApplicationReport report) {
		System.out.println(String.format("ApplicationId:%s\nType:%s\nStatus:%s\nFinish Time: %s", 
				report.getApplicationId(),
				report.getApplicationType(),
				report.getFinalApplicationStatus(),
				report.getFinishTime()));
	}
	public static YarnClient create(Configuration conf) {
		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(conf);
		yarnClient.start();
		return yarnClient;
	}

	public static void printUsage() {
		System.err.println("\nUsage: applicationId [-l[istOnly]] [-c containerId] [-d logDestitionDir]");
		System.err.println("Dump the logs collected by YARN to local FS. \n"
				+ "The log directory are contructed as following: <logDestitionDir>/<containerId>/* \n\t"
				+ "-l[istOnly]  only list the containers info.\n\t"
				+ "-c containerId  specific certain container.\n\t"
				+ "-d logDestitionDir the directory where to dump the logs.\n");
	}

}
