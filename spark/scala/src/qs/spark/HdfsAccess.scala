package qs.spark

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Jiang on 8/27/16.
  */
object HdfsAccess {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("SimpleScalaApp").setMaster("local[4]")
    val sc = new SparkContext(conf)

    val data = Array(1,2,3,4,5)

    val distData = sc.parallelize(data)

    println(distData.reduce((a, b) => a + b));

    val distFile = sc.textFile("hdfs://localhost:8020/user/jiazhao/demo-input/hadoop/yarn-site.xml")

    println("HDFS file line length sum:" + distFile.map(_.length).reduce(_ + _))
  }
}
