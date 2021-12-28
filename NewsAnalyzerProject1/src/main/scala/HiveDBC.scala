import org.apache.spark.sql.{DataFrame, SparkSession}

object HiveDBC {
  def connect(): SparkSession = {
    val spark = SparkSession
      .builder()
      .appName("NewsAnalyzerProject1")
      .config("spark.master", "local")
      .enableHiveSupport()
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    spark
  }

  def disconnect(spark: SparkSession): Unit = {
    spark.close
  }

  def executeDML(query : String, spark: SparkSession): Unit = {
    spark.sql(query).queryExecution
  }

  def executeQuery(query: String, spark: SparkSession): DataFrame = {
    spark.sql(query)
  }

  def getUsers(): DataFrame = {
    val spark = connect()
    executeDML("create table IF NOT EXISTS users(id Int, username String, password String, admin Boolean) row format delimited fields terminated by ','", spark)
    val users = executeQuery("select * from users", spark)
    if(users.count()==0) {
      executeDML("insert into users values (1, 'root', 'password', true)",spark)
    }

    val result: Map[String, (String, Int, Boolean)] = users.map(record => record.toList.take(5).mkString("_"))
    users.show()

    println(users.)

    disconnect(spark)
    users
  }

}
