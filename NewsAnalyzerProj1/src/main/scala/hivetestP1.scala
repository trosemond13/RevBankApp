import org.apache.spark.sql.SparkSession

object hivetestP1 {
  def main(args: Array[String]): Unit = {
    // create a spark session
    // for Windows
    //System.setProperty("hadoop.home.dir", "C:\\winutils")
    System.setProperty("hadoop.home.dir", "C:\\hadoop")
    val spark = SparkSession
      .builder
      .appName("hello hive")
      .config("spark.master", "local")
      .enableHiveSupport()
      .getOrCreate()
    println("created spark session")
    spark.sparkContext.setLogLevel("ERROR")
    spark.sql("DROP table IF EXISTS BevA")
    spark.sql("create table IF NOT EXISTS BevA(Beverage String,BranchID String) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_BranchA.txt' INTO TABLE BevA")
    spark.sql("SELECT Count(*) AS TOTALCOUNT FROM BevA").show()
    spark.sql("SELECT Count(*) AS NumBranch2BevAFile FROM BevA WHERE BevA.BranchID='Branch2'").show()
    //spark.sql("SELECT * FROM BevA").show()

    //spark.sql("DROP table IF EXISTS BevB")
    spark.sql("create table IF NOT EXISTS BevB(Beverage String,BranchID String) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_BranchB.txt' INTO TABLE BevB")
    //spark.sql("SELECT * FROM BevB").show()

    spark.sql("DROP table IF EXISTS BevC")
    spark.sql("create table IF NOT EXISTS BevC(Beverage String,BranchID String) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_BranchC.txt' INTO TABLE BevC")
    //spark.sql("SELECT * FROM BevC").show()

    spark.sql("DROP table IF EXISTS BevAll")
    spark.sql("CREATE TABLE BevAll(Beverage STRING,BranchID STRING)")
    spark.sql("INSERT INTO TABLE BevAll (SELECT * FROM BevA UNION ALL SELECT * FROM BevB UNION ALL SELECT * FROM BevC)")
    //spark.sql("SELECT * FROM BevAll").show()

    spark.sql("DROP table IF EXISTS ConsA")
    spark.sql("create table IF NOT EXISTS ConsA(Beverage STRING, Consumed INT) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_ConscountA.txt' INTO TABLE ConsA")
    //spark.sql("SELECT Count(*) AS TOTALCOUNT FROM ConsA").show()
    //spark.sql("SELECT * FROM ConsA").show()

    spark.sql("DROP table IF EXISTS ConsB")
    spark.sql("create table IF NOT EXISTS ConsB(Beverage STRING, Consumed INT) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_ConscountB.txt' INTO TABLE ConsB")
    //spark.sql("SELECT * FROM ConsB").show()

    spark.sql("DROP table IF EXISTS ConsC")
    spark.sql("create table IF NOT EXISTS ConsC(Beverage STRING, Consumed INT) row format delimited fields terminated by ','");
    spark.sql("LOAD DATA LOCAL INPATH 'Bev_ConscountC.txt' INTO TABLE ConsC")
    spark.sql("SELECT * FROM ConsC").show()

    spark.sql(sqlText="DROP TABLE IF EXISTS ConsAll");
    spark.sql("create table IF NOT EXISTS ConsAll(Beverage STRING, Consumed INT)");
    spark.sql(sqlText="INSERT INTO TABLE ConsAll (SELECT * FROM ConsA UNION ALL SELECT * FROM ConsB UNION ALL SELECT * FROM ConsC)");
    //spark.sql("SELECT * FROM ConsAll").show()

    //Problem 1
    //What is the total number of consumers for Branch1?
    //What is the total number of consumers for the Branch2?
    println("Problem 1 What is the total number of consumers for Branch1? What is the number of consumers for the Branch2? ")
    spark.sql(sqlText="DROP TABLE IF EXISTS TotalCons");
    spark.sql(sqlText="CREATE TABLE IF NOT EXISTS TotalCons(BranchID STRING, TotalConsumers INT) row format delimited fields terminated by ','");
    spark.sql(sqlText="INSERT INTO TABLE TotalCons(SELECT BranchID, sum(ConsAll.Consumed) FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) GROUP BY BevAll.BranchID ORDER BY BevAll.BranchID)");
    //Problem 1
    spark.sql("SELECT * FROM TotalCons").show()

    //Problem 2
    //--What is the most consumed beverage on Branch1
    println("Problem 2 What is the most consumed beverage on Branch1")
    spark.sql(sqlText="SELECT BranchID, sum(ConsAll.Consumed),BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch1' GROUP BY BevAll.BranchID,BevAll.Beverage ORDER BY sum(ConsAll.Consumed) DESC").show()
    //--What is the least consumed beverage on Branch2
    //--What is the Average (median) consumed beverage of Branch2
    //spark.sql(sqlText="SELECT BranchID, ROW_NUMBER() OVER (ORDER BY sum(ConsAll.Consumed)) AS row_num,sum(ConsAll.Consumed),BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch2' GROUP BY BevAll.BranchID,BevAll.Beverage ORDER BY sum(ConsAll.Consumed) DESC")
    println("Problem 2 What is the least consumed beverage on Branch2")
    spark.sql(sqlText="SELECT BranchID,sum(ConsAll.Consumed), BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch2' GROUP BY BevAll.BranchID,BevAll.Beverage ORDER BY sum(ConsAll.Consumed) DESC").show()
    println("least consumed")
    spark.sql(sqlText="SELECT BranchID,sum(ConsAll.Consumed), BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch2' GROUP BY BevAll.BranchID,BevAll.Beverage ORDER BY sum(ConsAll.Consumed) ASC LIMIT 1").show()
    //median shop above is number 26 or Small Latte with 93184
    println("average (median) consumed")
    spark.sql(sqlText="SELECT BranchID,sum(ConsAll.Consumed), BevAll.Beverage FROM BevAll join ConsAll On (ConsAll.Beverage=BevAll.Beverage) WHERE BranchID='Branch2' AND BevAll.Beverage='SMALL_LATTE' GROUP BY BevAll.BranchID,BevAll.Beverage  ORDER BY sum(ConsAll.Consumed) DESC").show()

    spark.close()
  }
}
