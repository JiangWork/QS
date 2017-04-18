package spark.qs;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.HashPartitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;
import tool.StringUtils;

public class WordCount {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length < 2) {
			System.err.println("Usage: inpath_on_hdfs outpath_on_hdfs");
			System.exit(-1);
		}
		String in = args[0];
		String out = args[1];
		
		SparkConf conf = new SparkConf().setAppName("WordCountQS-Jiang");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<String> data = sc.textFile(in);
		JavaRDD<String> words = data.flatMap(new FlatMapFunction<String, String>() {
			@Override
			public Iterator<String> call(String str) throws Exception {
				// TODO Auto-generated method stub
				return Arrays.asList(str.split(" ")).iterator();
			}
			
		});
		JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
			@Override
			public Tuple2<String, Integer> call(String str) throws Exception {
				// TODO Auto-generated method stub
				return new Tuple2<String, Integer>(str, 1);
			}
			
		});
		JavaPairRDD<String, Integer> partones= ones.partitionBy(new HashPartitioner(20));
		System.out.println("Partition number for " + ones  + " is " + ones.getNumPartitions());
		JavaPairRDD<String, Integer> counts = partones.reduceByKey(new Function2<Integer, Integer, Integer>(){

			@Override
			public Integer call(Integer int1, Integer int2) throws Exception {
				// TODO Auto-generated method stub
				return int1 + int2;
			}			
		});
		System.out.println(new Date() + ": start to compute.");
		List<Tuple2<String, Integer>> list = counts.collect();
		for(Tuple2<String, Integer> tuple: list) {
			System.out.println(tuple._1 + ":" + tuple._2);
		}
		
		System.out.println(new Date() + ": finished. " + StringUtils.echo("From dependencies."));
	}

}
