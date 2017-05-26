package tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;

import tool.ApplicationsInQueue.QueueInfo.Counter;

public class ApplicationsInQueue {

	public static void main(String[] args) throws IOException, YarnException {
		// TODO Auto-generated method stub
		Configuration conf = new YarnConfiguration();
		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(conf);
		yarnClient.start();
        EnumSet<YarnApplicationState> appStates = EnumSet
                .noneOf(YarnApplicationState.class);
        appStates.add(YarnApplicationState.SUBMITTED);
        appStates.add(YarnApplicationState.ACCEPTED);
        appStates.add(YarnApplicationState.RUNNING);
        Set<String> appTypes = new HashSet<String>();
        
		List<ApplicationReport> appsReport = yarnClient.getApplications(appTypes,
		        appStates);
		
		Map<String, QueueInfo> queueCounter = new HashMap<String, QueueInfo>();
		for(ApplicationReport report: appsReport) {
			String queueName = report.getQueue();
			QueueInfo info;
			if (!queueCounter.containsKey(queueName)) {
				info = new QueueInfo();
				queueCounter.put(queueName, info);
			} else {
				info = queueCounter.get(queueName);
			}
			info.add(report);
		}
		List<QueueInfo> queueList = new ArrayList<QueueInfo>();
		queueList.addAll(queueCounter.values());
		Collections.sort(queueList);
		Counter total = new Counter();
		for (QueueInfo info: queueList) {
			total.add(info.counter);
		}
		System.out.println("Overview: " + total + " (SUBMITTED/ACCEPTED/RUNNING)." );
		for (QueueInfo info: queueList) {
			info.print();
		}
		yarnClient.stop();
	}
	
	public static class QueueInfo implements Comparable<QueueInfo> {
		private String name;
		private Counter counter = new Counter();
		private Map<String, Counter> userCounter= new HashMap<String, Counter>();
		
		
		public void add(ApplicationReport report) {
			this.name = report.getQueue();
			updateCounter(counter, report.getYarnApplicationState());
			Counter uc;
			if (userCounter.containsKey(report.getUser())) {
				uc = userCounter.get(report.getUser());				
			} else {
				uc = new Counter();
				userCounter.put(report.getUser(), uc);
			}
			updateCounter(uc, report.getYarnApplicationState());
		}
		
		public void updateCounter(Counter counter, YarnApplicationState state) {
			switch(state) {
			case SUBMITTED:
				counter.incSubmitted();
				break;
			case ACCEPTED:
				counter.incAccepted();
				break;
			case RUNNING:
				counter.incRunning();
				break;
			default:
					// ignore
			}
		}
		
		public void print() {
			
			System.out.println(toString());
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder(String.format("Queue: %s, %s.", 
					name, counter));
			Iterator<Entry<String, Counter>> iter = userCounter.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, Counter> item = iter.next();
				String user = item.getKey();
				Counter counter = item.getValue();
				sb.append("\n\t");
				sb.append(user);
				sb.append("\t");
				sb.append(String.format("%s", counter));
			}
			return sb.toString();
		}
		
		public static class Counter {
			private int running;
			private int accepted;
			private int submitted;
			public void add(Counter counter) {
				this.running += counter.running;
				this.accepted += counter.accepted;
				this.submitted += counter.submitted;
			}
			public void incRunning() {
				this.running += 1;
			}
			public void incAccepted() {
				this.accepted += 1;
			}
			public void incSubmitted() {
				this.submitted += 1;
			}
			public int getRunning() {
				return running;
			}
			public void setRunning(int running) {
				this.running = running;
			}
			public int getAccepted() {
				return accepted;
			}
			public void setAccepted(int accepted) {
				this.accepted = accepted;
			}
			public int getSubmitted() {
				return submitted;
			}
			public void setSubmitted(int submitted) {
				this.submitted = submitted;
			}
			public String toString() {
				return String.format("%d/%d/%d", 
						getSubmitted(), getAccepted(), getRunning());
			}
			
		}

		@Override
		public int compareTo(QueueInfo o) {
			// TODO Auto-generated method stub
			return o.name.compareTo(name);
		}
	}

}
