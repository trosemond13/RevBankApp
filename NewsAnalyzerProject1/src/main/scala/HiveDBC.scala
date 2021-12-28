import org.apache.spark.sql.{DataFrame, SQLContext, SQLImplicits, SparkSession}
import com.roundeights.hasher.Implicits._
import scala.language.postfixOps

object HiveDBC {
  def suppressLogs(params: List[String]): Unit = {
    import org.apache.log4j.{Level, Logger}
    params.foreach(Logger.getLogger(_).setLevel(Level.OFF))
  }

  def connect(): SparkSession = {
    suppressLogs(List("org", "akka"))
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

  def getUsers(): Map[String, (String, Int, Boolean)] = {
    val spark = connect()

    executeDML("create table IF NOT EXISTS users(id Int, username String, password String, admin Boolean) row format delimited fields terminated by ','", spark)
    val users = executeQuery("select * from users", spark)

    if(users.count()==0) {
      val password = "password".sha256.hash
      executeDML("insert into users values (1, 'root', '" + password+ "', true)", spark)
    }

    var usersMap: Map[String, (String, Int, Boolean)] = Map[String, (String, Int, Boolean)]()

    users.collect().foreach(row => {
      val id = row.getInt(0)
      val username = row.getString(1)
      val password = row.getString(2)
      val isAdmin = row.getBoolean(3)
      usersMap += (username -> (password, id, isAdmin))
    })

    disconnect(spark)
    usersMap
  }

}
