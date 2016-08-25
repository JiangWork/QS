package spark.qs;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

public class SimpleSparkApp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String sparkReadMeFile = "/Users/Miller/Work/OpenSource_Framework/spark/spark-2.0.0-src/README.md";
		SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local[4]");
		
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<String> logData = sc.textFile(sparkReadMeFile).cache();
		
		long numAs = logData.filter((String s) -> s.contains("a")).count();
		
//		long numAs = logData.filter(new Function<String, Boolean>() {
//		      public Boolean call(String s) { return s.contains("a"); }
//		    }).count();

		    long numBs = logData.filter(new Function<String, Boolean>() {
		      public Boolean call(String s) { return s.contains("b"); }
		    }).count();

		    System.out.println("JIANGZHAO>> Lines with a: " + numAs + ", lines with b: " + numBs);
		   
		    new Thread(() -> System.out.println("Lambda experssion")).start();
	}

}
