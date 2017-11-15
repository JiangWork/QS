package tool;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.logaggregation.AggregatedLogFormat;
import org.apache.hadoop.yarn.logaggregation.AggregatedLogFormat.LogKey;
import org.apache.hadoop.yarn.logaggregation.LogAggregationUtils;
import org.apache.hadoop.yarn.logaggregation.LogCLIHelpers;

public class YarnlogDumperHelper extends LogCLIHelpers {

	public int dumpAllContainersLogs(ApplicationId appId, String appOwner,
			String containerId, String destDir, boolean listOnly) throws IOException {
		Path remoteRootLogDir = new Path(getConf().get(
				YarnConfiguration.NM_REMOTE_APP_LOG_DIR,
				YarnConfiguration.DEFAULT_NM_REMOTE_APP_LOG_DIR));
		String user = appOwner;
		String logDirSuffix = LogAggregationUtils.getRemoteNodeLogDirSuffix(getConf());
		// TODO Change this to get a list of files from the LAS.
		Path remoteAppLogDir = LogAggregationUtils.getRemoteAppLogDir(
				remoteRootLogDir, appId, user, logDirSuffix);
		RemoteIterator<FileStatus> nodeFiles;
		try {
			Path qualifiedLogDir =
					FileContext.getFileContext(getConf()).makeQualified(remoteAppLogDir);
			nodeFiles = FileContext.getFileContext(qualifiedLogDir.toUri(),
					getConf()).listStatus(remoteAppLogDir);
		} catch (FileNotFoundException fnf) {
			logDirNotExist(remoteAppLogDir.toString());
			return -1;
		}
		boolean foundAnyLogs = false;
		while (nodeFiles.hasNext()) {
			FileStatus thisNodeFile = nodeFiles.next();
			if (!thisNodeFile.getPath().getName()
					.endsWith(LogAggregationUtils.TMP_FILE_SUFFIX)) {
				AggregatedLogFormat.LogReader reader =
						new AggregatedLogFormat.LogReader(getConf(), thisNodeFile.getPath());
				try {
					DataInputStream valueStream;
					LogKey key = new LogKey();
					valueStream = reader.next(key);

					while (valueStream != null) {
						String containerString =
								"Container: " + key + " , Host: " + thisNodeFile.getPath().getName();
						if(listOnly)	System.out.println(containerString);
						if (!listOnly && needOutput(containerId, key.toString())) {
							System.out.println(containerString);
							while (true) {
								try {
									System.out.println(">>> To:" + readContainerLogs(valueStream, destDir + "/" + key.toString()));
									foundAnyLogs = true;
								} catch (EOFException eof) {
									break;
								}
							}
							
						}
						if(containerId.trim().length() != 0 && foundAnyLogs) {
							break;
						}
						// Next container
						key = new LogKey();
						valueStream = reader.next(key);
					}
				} finally {
					reader.close();
				}
			}
		}
		if (!listOnly && !foundAnyLogs) {
			emptyLogDir(remoteAppLogDir.toString());
			return -1;
		}
		return 0;
	}

	private String readContainerLogs(DataInputStream valueStream,
			String destDir) throws IOException {
		byte[] buf = new byte[65535];

		String fileType = valueStream.readUTF();

		String fileLengthStr = valueStream.readUTF();
		long fileLength = Long.parseLong(fileLengthStr);
		File parentFile = new File(destDir);
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		File destFile = new File(parentFile, fileType);
		FileOutputStream fos = new FileOutputStream(destFile);

		long curRead = 0;
		long pendingRead = fileLength - curRead;
		int toRead =
				pendingRead > buf.length ? buf.length : (int) pendingRead;
				int len = valueStream.read(buf, 0, toRead);
				while (len != -1 && curRead < fileLength) {
					fos.write(buf, 0, len);
					curRead += len;

					pendingRead = fileLength - curRead;
					toRead =
							pendingRead > buf.length ? buf.length : (int) pendingRead;
							len = valueStream.read(buf, 0, toRead);
				}
				fos.close();
				return destFile.getAbsolutePath();
	}


	private static boolean needOutput(String expectedId, String actualId) {
		if (expectedId.trim().length() == 0) {
			return true;
		}
		return expectedId.equalsIgnoreCase(actualId);
	}
	private static void logDirNotExist(String remoteAppLogDir) {
		System.out.println(remoteAppLogDir + " does not exist.");
		System.out.println("Log aggregation has not completed or is not enabled.");
	}

	private static void emptyLogDir(String remoteAppLogDir) {
		System.out.println(remoteAppLogDir + " does not have any log files.");
	}
}
