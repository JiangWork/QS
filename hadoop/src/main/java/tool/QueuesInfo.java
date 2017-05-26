package tool;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

public class QueuesInfo {

	private static DecimalFormat df = new DecimalFormat("#.0");
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Print Queue Information.");
		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(new YarnConfiguration());
		yarnClient.start();
		try {
			List<QueueInfo> rootQueues = yarnClient.getRootQueueInfos();
//			for(QueueInfo info: rootQueues) {
//				System.out.println(info.getQueueName() + ":" + info.getCurrentCapacity());
//				System.out.println("subQueue:" + info.getChildQueues());
//			}
			QueueInfo queueInfo = yarnClient.getQueueInfo("root");
//			System.out.println("subQueue:" + queueInfo.getChildQueues());
			queueInfo.setChildQueues(rootQueues);
			// output root usage
			System.out.println(String.format("root: %.1f.", 100*queueInfo.getCurrentCapacity()));
			outputOverCapacityQueue(queueInfo, 100);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			yarnClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void outputOverCapacityQueue(QueueInfo queue, float factor) {
		float used = 100 * queue.getCurrentCapacity();
		if (used > 100) {
			float absUsed = factor * queue.getCapacity() * queue.getCurrentCapacity();
			float absCap = factor * queue.getCapacity();
			float absMaxCap = factor * queue.getMaximumCapacity();
			System.out.println(String.format("queuename=%s\tused=%.1f%%;absUsed=%.1f%%;absCap=%.1f%%;absMaxCap=%.1f%%", 
					queue.getQueueName(), used, absUsed, absCap, absMaxCap));
		}
		for (QueueInfo subQueue: queue.getChildQueues()) {
			outputOverCapacityQueue(subQueue, factor * queue.getCapacity());
		}
	}

}
