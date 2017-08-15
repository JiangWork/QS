package qs.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Unzip {

	public static class IdentityMapper extends Mapper<Object, Text, Object, Text> {
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			context.write(NullWritable.get(), value);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length < 2) {
			System.err.println("Usage: unzip <in> [<in>...] <out>");
			System.exit(2);
		}
		conf.set("mapreduce.output.fileoutputformat.compress", "false", "from code");
		Job job = Job.getInstance(conf, "unzip");
		job.setJarByClass(Unzip.class);
		job.setMapperClass(IdentityMapper.class);
		job.setNumReduceTasks(0);
		for (int i = 0; i < otherArgs.length - 1; ++i) {
			FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
		}
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[otherArgs.length - 1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
