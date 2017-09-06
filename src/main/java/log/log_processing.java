package log;

import java.util.*;

import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;
import org.apache.spark.util.LongAccumulator;

import scala.Tuple2;



public class log_processing {
	public static void main(String[] args) {
		//set path for input and output
		String inputPath = args[0];
		String outputPath = args[1];
		String logPath = args[2];
		//conf
		SparkConf conf = new SparkConf().setAppName("log");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		//doc file domain, log
		JavaRDD<String> domainList = sc.textFile(logPath);
		JavaRDD<String> logFile = sc.textFile(inputPath, 10);
		//kiem tra truong log du dl valid 
		//lay truong dl can
		JavaPairRDD<String, String> checkedLog = logFile
				.filter(l -> l.split("\t").length>=24)
				.mapToPair(line->new Tuple2<String, String>(line.split("\t")[8], line.split("\t")[13]));
		//set domain 
		Set<String> domains = new HashSet<String>();
		domainList.collect().forEach(domain->domains.add(domain));
		//broadcast domainList
		//the larger rdd not need to be shuffled
		Broadcast<Set<String>> broadcastedDomains = sc.broadcast(domains);
		JavaPairRDD<String, String> toLogRow = checkedLog.filter(log->broadcastedDomains.value().contains(log._1));


		//count guid
		JavaPairRDD<Tuple2<String, String>, Integer> countguid = toLogRow
				.mapToPair(line-> new Tuple2<Tuple2<String, String>, Integer>(line,1))
				.reduceByKey((count1, count2)->count1+count2);
		//		//create row
		JavaRDD<Row> rowRDD = countguid.map(f->{
			return RowFactory.create(f._1._1, f._1._2, f._2);
		});
		//define schema
		List<StructField> listFields = new ArrayList<>();
		String schemaString = "domain guid count";
		for (String fieldName : schemaString.split(" ")){
			if ("count".equals(fieldName.trim()))
				listFields.add(DataTypes.createStructField(fieldName, DataTypes.IntegerType, true));			
			else
				listFields.add(DataTypes.createStructField(fieldName, DataTypes.StringType, true));
		}
		StructType schema = DataTypes.createStructType(listFields);
		Dataset<Row> resultDataFrame = sqlContext.createDataFrame(rowRDD, schema);
		resultDataFrame.repartition(500).write().csv(outputPath);
		sc.close();
//		//map to pair rdd domain
//		LongAccumulator c = sc.sc().longAccumulator();
//		JavaPairRDD<String, Long> domains = domainList.mapToPair(f->{
//			c.add(1);
//			Tuple2<String, Long> a = new Tuple2<String, Long>(f, c.value());
//			return a;
//		});
		
//		JavaPairRDD<String, String> filterFields = fields.leftOuterJoin(domains)
//				.filter(f->{
//					if (f._2._2.isPresent())
//						return true;
//					return false;
//				})
//				.mapToPair(a-> new Tuple2<String, String>(a._1, a._2._1));
//		System.out.println(filterFields.count());
	}
}
