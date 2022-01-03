import org.apache.spark.sql.{DataFrame, SparkSession}
import com.roundeights.hasher.Implicits._
import scala.language.postfixOps

object HiveDBC {
  var spark: SparkSession = null

  def suppressLogs(params: List[String]): Unit = {
    import org.apache.log4j.{Level, Logger}
    params.foreach(Logger.getLogger(_).setLevel(Level.OFF))
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

  def getSparkSession(): SparkSession = {
    if(spark == null) {
      suppressLogs(List("org", "akka"))
      System.setProperty("hadoop.home.dir", "C:\\hadoop")
      spark = SparkSession
        .builder()
        .appName("NewsAnalyzerProject1")
        .config("spark.master", "local")
        .enableHiveSupport()
        .getOrCreate()
      spark.conf.set("hive.exec.dynamic.partition.mode", "nonstrict")
      spark.sparkContext.setLogLevel("ERROR")
      spark.sql("CREATE DATABASE IF NOT EXISTS RevNewsAnalyzer")
      spark.sql("USE RevNewsAnalyzer")
      executeDML("CREATE TABLE IF NOT EXISTS users(user_id Int, username String, password String, admin Boolean, deleted Boolean)", spark)
      executeDML("CREATE TABLE IF NOT EXISTS wordsList(word_id Int, user_id Int, word String, numberOfTimesUsed Long)", spark)
    }
    spark
  }

  def getUsers(): Map[String, (String, Int, Boolean)] = {
    val spark = getSparkSession()

    val users = executeQuery("SELECT * FROM users WHERE deleted == false", spark)

    if(users.count()==0) {
      val password = "password".sha256.hash
      executeDML(s"insert into users values (1, 'root', '$password', true, false)", spark)
    }

    var usersMap: Map[String, (String, Int, Boolean)] = Map[String, (String, Int, Boolean)]()

    users.collect().foreach(row => {
      val id = row.getInt(0)
      val username = row.getString(1).trim
      val password = row.getString(2).trim
      val isAdmin = row.getBoolean(3)
      usersMap += (username -> (password, id, isAdmin))
    })
    usersMap
  }

  def getUserWords(user_id: Int): DataFrame = {
    val spark = getSparkSession()
    HiveDBC.executeQuery(s"SELECT word, numberOfTimesUsed FROM wordsList WHERE user_id = $user_id ORDER BY numberOfTimesUsed DESC", spark)
  }

  def getAllWords(): DataFrame = {
    val spark = getSparkSession()
    HiveDBC.executeQuery(s"SELECT word, numberOfTimesUsed FROM wordsList ORDER BY numberOfTimesUsed DESC", spark)
  }

  def generateNewUserID(): Int = {
    val spark = getSparkSession()
    val users = executeQuery("SELECT * FROM users WHERE", spark)
    users.count().toInt + 1
  }

  def addWord(user_id: Int, input: String, results: Long): Unit = {
    val spark = getSparkSession()
    val words = executeQuery("select * from wordslist", spark)
    val new_word_id = (words.count() + 1).toInt
    executeDML(s"insert into wordsList values ($new_word_id, $user_id, '$input', $results)", spark)
  }

  def addUser(user_id:Int, username: String, password: String, isAdmin: Boolean): Unit = {
    val spark = getSparkSession()
    try {
      executeDML(s"insert into users values ($user_id, '$username', '$password', $isAdmin, false)", spark)
    }
  }

  def deleteUser(user_id:Int, username: String, password: String, admin: Boolean): Unit = {
    val spark = getSparkSession()
    val users = executeQuery(s"SELECT * FROM users WHERE user_id != $user_id", spark)
    executeDML("CREATE TABLE tempusers(user_id Int, username String, password String, admin Boolean, deleted Boolean)", spark)

    users.collect().foreach(row => {
      val user_id = row.getInt(0)
      val username = row.getString(1).trim
      val password = row.getString(2).trim
      val admin = row.getBoolean(3)
      executeDML(s"insert into tempusers values ($user_id, '$username', '$password', $admin, false)", spark)
    })
    executeDML(s"insert into tempusers values ($user_id, '$username', '$password', $admin, true)", spark)
    executeDML("DROP TABLE users", spark)
    executeDML("ALTER TABLE tempusers RENAME TO users", spark)
  }

  def updateUserPassword(user_id: Int, username: String, newPass: String, admin: Boolean): Unit = {
    val spark = getSparkSession()
    val users = executeQuery(s"SELECT * FROM users WHERE user_id != $user_id", spark)
    executeDML("CREATE TABLE tempusers(user_id Int, username String, password String, admin Boolean, deleted Boolean)", spark)

    users.collect().foreach(row => {
      val user_id = row.getInt(0)
      val username = row.getString(1).trim
      val password = row.getString(2).trim
      val admin = row.getBoolean(3)
      executeDML(s"insert into tempusers values ($user_id, '$username', '$password', $admin, false)", spark)
    })
    executeDML(s"insert into tempusers values ($user_id, '$username', '$newPass', $admin, false)", spark)
    executeDML("DROP TABLE users", spark)
    executeDML("ALTER TABLE tempusers RENAME TO users", spark)
  }

  def updateUserUsername(user_id: Int, newUsername: String, password: String, admin: Boolean): Unit = {
    val spark = getSparkSession()
    val users = executeQuery(s"SELECT * FROM users WHERE user_id != $user_id", spark)
    executeDML("CREATE TABLE tempusers(user_id Int, username String, password String, admin Boolean, deleted Boolean)", spark)

    users.collect().foreach(row => {
      val user_id = row.getInt(0)
      val username = row.getString(1).trim
      val password = row.getString(2).trim
      val admin = row.getBoolean(3)
      executeDML(s"insert into tempusers values ($user_id, '$username', '$password', $admin, false)", spark)
    })
    executeDML(s"insert into tempusers values ($user_id, '$newUsername', '$password', $admin, false)", spark)
    executeDML("DROP TABLE users", spark)
    executeDML("ALTER TABLE tempusers RENAME TO users", spark)
  }
}
