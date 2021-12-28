import org.apache.spark.sql.DataFrame
import scala.Console._
import com.roundeights.hasher.Implicits._
import scala.language.postfixOps

class RevNewsAnalyzer {
  var users:Map[String, (String, Int, Boolean)] = HiveDBC.getUsers()
  var loggedIn: Boolean = false
  var username: String = ""
  final val loadingBar = List(s"                      $YELLOW**********$RESET\\$BLUE**********$RESET"
    , s"                      $GREEN**********$RESET-$RED**********$RESET"
    , s"                      $CYAN**********$RESET / $MAGENTA**********$RESET"
    , s"                      $WHITE**********$RESET | $YELLOW**********$RESET")

  def login(): Unit = {
    var tries:Int = 3
    if(loggedIn) {
      println(s"${RED}${BOLD}ERROR${RESET}> You are already logged in.")
    } else {
      while(!loggedIn) {
        print(s"${GREEN}${BOLD}SYSTEM${RESET}> Enter your username here -> ")
        username = readLine()
        print(s"${GREEN}${BOLD}SYSTEM${RESET}> Enter your password here -> ")
        val password = readLine().sha256.hash

        if(users.contains(username) && users.get(username).get._1 == password) {
          println(s"${GREEN}${BOLD}SYSTEM${RESET}> Logged in as <" + username + ">")
          loggedIn = true
        } else {
          tries = tries - 1
          if(tries>0) {
            println(s"${RED}${BOLD}ERROR${RESET}> Incorrect username or password. " + tries + " more tries left or the program will exit! User/pass inputs are case-sensitive for security purposes.")
          } else {
            println(s"${RED}${BOLD}ERROR${RESET}> You typed in the incorrect username or password too many times. Exiting login page...")
            username = ""
          }
        }
      }
    }
  }

  def logout(): Unit = {

  }
}
object Main {
  def main(args: Array[String]):Unit = {
    val analyzer = new RevNewsAnalyzer()
    var currCommand = ""

    do {
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> Enter a command, type 'help' for more info or ':quit' to exit.")
      print(s"$BOLD${analyzer.username}$RESET" + "> ")
      currCommand = readLine().toLowerCase()

      if (currCommand == "login") {
        analyzer.login
      } else if(currCommand == "logout") {
        println("Exiting ADMINVIEW. Reverting back to user mode.")
        analyzer.username = ""
      } else if(currCommand == ":quit") {
          println(s"$GREEN${BOLD}SYSTEM$RESET> Thank you for using the NewsAnalyzer. We appreciate your membership.")
          println(s"$GREEN${BOLD}SYSTEM$RESET> You are now exiting the application!")
          println(".....")
          Thread.sleep(1000)
          println("....")
          Thread.sleep(1000)
          println("...")
          Thread.sleep(2000)
          println(s"$GREEN${BOLD}SYSTEM$RESET> Exit complete. Come again soon!")
      } else {
        println(s"$RED${BOLD}ERROR$RESET> Invalid Command! Please try again.")
      }

    } while(currCommand != ":quit")

  }
}
