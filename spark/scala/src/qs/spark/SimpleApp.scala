package qs.spark

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

/**
  * Created by Jiang on 8/24/16.
  */
object SimpleApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("SimpleScalaApp").setMaster("local[4]")
    val sc = new SparkContext(conf)
    val logData = sc.textFile("/Users/Miller/Work/OpenSource_Framework/spark/spark-2.0.0-src/README.md").cache()
    val numOfbLine = logData.filter(_.contains("b")).count()
    val numOfaLine = logData.filter(_.contains("a")).count()
    String.format("JIANGZHAO >> Lines with a: %d, Lines with b: %d", new java.lang.Long(numOfaLine), new java.lang.Long(numOfbLine))
    println("JIANGZHAO >> Lines with a: %d, Lines with b: %d".format(numOfaLine, numOfbLine))
    //Thread.sleep(Integer.MAX_VALUE)
  }
}
