package qs.hadoop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;



public class ShellExecutor {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		File temp = File.createTempFile("bash", ".sh");
		BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
		bw.write(shellScript("container_1498722174485_0001_01_000005"));
		bw.close();
		
		 ProcessBuilder builder = new ProcessBuilder("/bin/bash", temp.getAbsolutePath());
		 Process proc = builder.start();
		 BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		 String line = null;
		 while((line = br.readLine()) != null) {
			 System.out.println(line);
		 }
		 br = new BufferedReader(new InputStreamReader( proc.getErrorStream()));
		 line = null;
		 while((line = br.readLine()) != null) {
			 System.out.println(line);
		 }
		 proc.waitFor();
	}

	public static String shellScript(String applicationId) {
		BashBuilder bb = new BashBuilder();
		bb.addLine("#!/bin/bash");
		bb.addLine("show() {");
		bb.addLine(" echo \"#####################################${1} `date`######################################\"");
		bb.addLine("}");
		bb.addLine("show \"DF -ha\"");
		bb.addLine("df -ha 2>&1");
		bb.addLine("show \"TOP -n 1\"");
		bb.addLine("top -b -n 1 2>&1");
		bb.addLine("show \"PS\"");
		bb.addLine("ps -ef 2>&1");
		bb.addLine("");
		bb.addLine(String.format("PIDLIST=`jps -lv | grep %s | awk  '{print $1}'`",  applicationId));
		bb.addLine("for pid in $PIDLIST");
		bb.addLine("do");
		bb.addLine("ps -ef | grep $pid | grep -v grep ");
		bb.addLine("show \"JSTACK $pid\"");
		bb.addLine("timeout 10s jstack $pid 2>&1");
		bb.addLine("show \"JMAP -heap $pid\"");
		bb.addLine("timeout 10s jmap -heap $pid");
		bb.addLine("done");
		return bb.collect();
	}
	
	public static class BashBuilder {
		private StringBuilder sb = new StringBuilder();
		
		public BashBuilder addLine(String line) {
			sb.append(line);
			sb.append("\n");
			return this;
		}
		
		public String collect() {
			return sb.toString();
		}
	}
}
