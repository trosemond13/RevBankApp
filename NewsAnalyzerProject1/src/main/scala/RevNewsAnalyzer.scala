import org.apache.spark.sql.DataFrame

class RevNewsAnalyzer {
  var users:DataFrame = HiveDBC.getUsers()
  var loggedIn: Boolean = false
  var username: String = ""
  var password: String = ""

  def login(): Unit = {

  }

  def logout(): Unit = {

  }
}
object Main {
  def main(args: Array[String]):Unit = {
    val analyzer = new RevNewsAnalyzer()
    var currCommand = ""

  }
}
