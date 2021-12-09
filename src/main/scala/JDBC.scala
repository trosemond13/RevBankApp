import java.io.IOException
import java.sql.{Connection, DriverManager, ResultSet, SQLException, SQLIntegrityConstraintViolationException, Statement}


object JDBC {
  val driver = "com.mysql.cj.jdbc.Driver"
  val url = "jdbc:mysql://localhost:3306/revbankapp"
  val username = "root"
  val password = "Password123!"
  var connection:Connection = null
  var stmt:Statement = null
  var resultSet:ResultSet = null

  protected def connect(): Unit = {
    if (connection == null)
      try {
        Class.forName(driver)
        connection = DriverManager.getConnection(url, username, password)
      } catch {
        case e: SQLException => e.printStackTrace()
      }
    else if (connection.isClosed)
      try {
        Class.forName(driver)
        connection = DriverManager.getConnection(url, username, password)
      } catch {
        case e: SQLException => e.printStackTrace()
      }
  }
  protected def disconnect(): Unit = {
    if (!connection.isClosed)
      connection.close()
    if (resultSet != null)
      if (!resultSet.isClosed)
        resultSet.close()
    if (stmt != null)
      if (!stmt.isClosed)
        stmt.close()
  }

  protected def executeDML(query : String) : Option[Boolean] = {
    try {
      stmt = connection.createStatement()
      Some(stmt.execute(query))
    } catch {
      case e: SQLException => {
        e.printStackTrace()
        None
      }
      case i: IOException => {
        i.printStackTrace()
        None
      }
      case n: SQLIntegrityConstraintViolationException => {
        n.printStackTrace()
        None
      }
    }
  }

  protected def executeQuery(query: String): ResultSet = {
    try {
      stmt = connection.createStatement()
      stmt.executeQuery(query)
    } catch {
      case e: SQLException => {
        e.printStackTrace()
        null
      }
      case i: IOException => {
        i.printStackTrace()
        null
      }
      case n: SQLIntegrityConstraintViolationException => {
        n.printStackTrace()
        null
      }
    }
  }
//  def getX(): Unit = {
//    connect()
//    resultSet = executeQuery("SELECT * FROM users")
//    while(resultSet.next()) {
//      println(resultSet.getString("username") + " " + resultSet.getString("password"))
//    }
//    disconnect()
//  }

  def getUsers(): Map[String, (String, Double)] = {
    connect()
    resultSet = executeQuery("SELECT username, password, bal FROM revbankapp.useraccounts\nINNER JOIN users ON revbankapp.useraccounts._id = revbankapp.users._id\nORDER BY revbankapp.useraccounts._id")
    var username:String = ""
    var password:String = ""
    var bal:Double = 0.0
    var users = Map[String, (String,Double)]()
    while(resultSet.next()) {
      username = resultSet.getString("username")
      password = resultSet.getString("password")
      bal = resultSet.getString("bal").toDouble
      users+=(username -> (password,bal))
      //println("User: " + username + " Password: " + password + " Bal: " + bal)
    }
    disconnect()
    users
  }
}
