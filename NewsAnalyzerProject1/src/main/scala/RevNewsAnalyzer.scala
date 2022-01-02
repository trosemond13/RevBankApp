import scala.Console._
import com.roundeights.hasher.Implicits._
import scalaj.http.Http

import scala.io.StdIn
import scala.language.postfixOps

class RevNewsAnalyzer {
  var users: Map[String, (String, Int, Boolean)] = HiveDBC.getUsers()
  var loggedIn: Boolean = false
  var username: String = ""
  final val loadingBar = List(s"                      $YELLOW**********$RESET\\$BLUE**********$RESET"
    , s"                      $GREEN**********$RESET-$RED**********$RESET"
    , s"                      $CYAN**********$RESET / $MAGENTA**********$RESET"
    , s"                      $WHITE**********$RESET | $YELLOW**********$RESET")
  val SUCCESS = 0
  val FAILURE = 1

  def login(): Int = {
    var tries:Int = 3
    if(loggedIn) {
      println(s"$RED${BOLD}ERROR$RESET> You are already logged in.")
      return SUCCESS
    } else {
      while(!loggedIn) {
        print(s"$GREEN${BOLD}SYSTEM$RESET> Enter your username here -> ")
        username = StdIn.readLine()
        print(s"$GREEN${BOLD}SYSTEM$RESET> Enter your password here -> ")
        val password: String = StdIn.readLine().trim.sha256.hash

        if(users.contains(username) && users(username)._1 == password) {
          println(s"$GREEN${BOLD}SYSTEM$RESET> Logged in as <" + username + ">")
          loggedIn = true
          return SUCCESS
        } else {
          tries = tries - 1
          if(tries>0) {
            println(s"$RED${BOLD}ERROR$RESET> Incorrect username or password. " + tries + " more tries left or the program will exit! User/pass inputs are case-sensitive for security purposes.")
          } else {
            println(s"$RED${BOLD}ERROR$RESET> You typed in the incorrect username or password too many times. Exiting login page...")
            username = ""
          }
        }
      }
    }
    FAILURE
  }
  def logout(): Unit = {

  }
  def help(): Unit = {
    if (loggedIn) {
      if(!users(username)._3) {
        println(s"${GREEN}${BOLD}SYSTEM${RESET}> Below are the available commands in alphabetical order for admin users.")
        println("        1. :create -> opens the create menu and allows admins to create bank accounts.")
        println("        2. :delete -> opens the delete menu and allows admins to delete bank accounts.")
        println("        3. logout -> logs out of admin view.")
        println("        4. :quit -> exits the program")
      } else {
        println(s"$GREEN${BOLD}HELP MENU$RESET> Below are the available commands in alphabetical order for regular users.")
        println ("        1. help -> prints a list of available commands to use.")
        println ("        2. logout -> logs the user out of their account.")
        println ("        3. :quit -> logs the user out and quits the program.")
        println ("        4. start-analyzer -> Takes the user to the analyzer menu.")
        print(s"$GREEN${BOLD}SYSTEM$RESET> Enter any input to exit help menu..")
        StdIn.readLine()
      }
    } else {
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> Below are the available commands in alphabetical order for unsigned in guests.")
      println ("        1. help -> prints a list of available commands to use.")
      println ("        2. login -> opens the login menu and prompts the user to login.")
      println ("        3. register -> opens the register menu and prompts the user create an account.")
      println ("        4. :quit -> quits the program.")

    }
    print(s"$GREEN${BOLD}SYSTEM$RESET> Enter any input to exit help menu..")
    StdIn.readLine()
  }
}
object Main {
  def main(args: Array[String]):Unit = {
    val analyzer = new RevNewsAnalyzer()
    var currCommand = ""

    do {
      println(s"$GREEN${BOLD}MAIN MENU$RESET> Enter a command, type 'help' for more info or ':quit' to exit.")
      print(s"$BOLD${analyzer.username}$RESET" + "> ")
      currCommand = StdIn.readLine().toLowerCase()

      if (currCommand == "login") {
        analyzer.login
      } else if(currCommand == "logout") {
        println("Logged out.")
        analyzer.username = ""
      } else if(currCommand == "help") {
        analyzer.help
      } else  if(currCommand == "start-analyzer") {
        if(analyzer.loggedIn)
          do {
            println(s"$GREEN${BOLD}ANALYZER MENU$RESET> Enter one of the following option to utilize the word analyzer.")
            println(s"$GREEN${BOLD}ANALYZER MENU$RESET> 1. Find total relevancy (everything) of a word or phrase.")
            println(s"$GREEN${BOLD}ANALYZER MENU$RESET> 2. Find monthly relevancy of a word or phrase.")
            println(s"$GREEN${BOLD}ANALYZER MENU$RESET> 3. Find yearly relevancy of a word or phrase.")
            println(s"$GREEN${BOLD}ANALYZER MENU$RESET> 4. Return back to the main menu.")
            print(s"$BOLD${analyzer.username}$RESET" + s"> $UNDERLINED")
            currCommand = StdIn.readLine()
            if (currCommand == "1") {
              print(s"$RESET${BOLD}Enter word or phrase$RESET" + s"> ")
              val input = StdIn.readLine()
              val totalResults: Long = Http("http://newsapi.org/v2/everything")
                .params(Map[String, String]("q"-> input, "apiKey" -> "3b7cd19b5fbd4c6d8fabe6b302cac48c"))
                .asString.body.split(",")(1).split(":")(1).toLong
              println(s"$GREEN${BOLD}ANALYZER MENU$RESET> $BOLD$input$RESET has $BOLD$totalResults$RESET total results.")
              print(s"$GREEN${BOLD}ANALYZER MENU$RESET> Would you like to save this word? Press 'y' to save or any other key to skip: ")
              val response = StdIn.readLine().toLowerCase().trim
              while(response != "y" || response != "n") {
                if(StdIn.readLine() == "y") {
                  println(s"$GREEN${BOLD}ANALYZER MENU$RESET> Saving $BOLD$input$RESET to user's word list...")
                  HiveDBC.addWord(analyzer.users(analyzer.username)._2, input, totalResults)
                  println(s"$GREEN${BOLD}ANALYZER MENU$RESET> Saved.")
                }
              }
            } else if (currCommand == "2") {
              println(s"$GREEN${BOLD}ANALYZER ERROR$RESET> Please upgrade API plan from 'basic' to 'premium' to have access to this feature")
            } else if (currCommand == "3") {
              println(s"$GREEN${BOLD}ANALYZER ERROR$RESET> Please upgrade API plan from 'basic' to 'premium' to have access to this feature")
            } else if (currCommand == "4") {
              currCommand = "stop"
            } else {
              println(s"$RED${BOLD}ERROR$RESET> Invalid Option! Please try again.")
            }
          } while(currCommand != "stop")
        else
          println(s"$RED${BOLD}ERROR$RESET> You must be logged in to analyze words or phrases. Please login and try again.")
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

      println()
      println()
      println()
      println()
      println()
      println()
      println()
      println()
    } while(currCommand != ":quit")
    HiveDBC.disconnect(HiveDBC.getSparkSession())
  }
}
