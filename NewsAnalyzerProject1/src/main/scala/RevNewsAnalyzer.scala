import scala.Console._
import com.roundeights.hasher.Implicits._
import org.apache.spark.sql.DataFrame
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
  val SUCCESS: Int = 0
  val FAILURE: Int = -1

  def help(): Unit = {
    if (loggedIn) {
      if(users(username)._3) {
        println(s"$GREEN${BOLD}HELP MENU$RESET> Below are the available commands in alphabetical order for admin users.")
        println("        1. :create -> opens the create menu and allows admins to create analyst accounts.")
        println("        2. :delete -> opens the delete menu and allows admins to delete analyst accounts.")
        println("        3. logout -> logs out of admin view.")
        println("        4. :quit -> exits the program")
      } else {
        println(s"$GREEN${BOLD}HELP MENU$RESET> Below are the available commands in alphabetical order for regular users.")
        println("        1. help -> prints a list of available commands to use.")
        println("        2. logout -> logs the user out of their account.")
        println("        3. :quit -> logs the user out and quits the program.")
        println("        4. saved-words -> prints the user's list of saved words and related information.")
        println("        5. start-analyzer -> Takes the user to the analyzer menu.")
      }
    } else {
      println(s"$GREEN${BOLD}SYSTEM$RESET> Below are the available commands in alphabetical order for unsigned in guests.")
      println ("        1. help -> prints a list of available commands to use.")
      println ("        2. login -> opens the login menu and prompts the user to login.")
      println ("        3. register -> opens the register menu and prompts the user create an account.")
      println ("        4. :quit -> quits the program.")

    }
    print(s"$GREEN${BOLD}HELP MENU$RESET> Enter any input to exit help menu..")
    StdIn.readLine()
  }
  def login(): Int = {
    var tries:Int = 3
    while(!loggedIn) {
      print(s"$GREEN${BOLD}LOGIN MENU$RESET> Enter your username here -> ")
      username = StdIn.readLine().toLowerCase().trim
      print(s"$GREEN${BOLD}LOGIN MENU$RESET> Enter your password here -> ")
      val password: String = StdIn.readLine().trim.sha256.hash
      if(users.contains(username) && users(username)._1 == password) {
        println(s"$GREEN${BOLD}LOGIN MENU$RESET> Logged in as <" + username + ">")
        loggedIn = true
        return SUCCESS
      } else {
        tries = tries - 1
        if(tries>0) {
          println(s"$RED${BOLD}ERROR$RESET> Incorrect username or password. " + tries + " more tries left or the program will exit!")
        } else {
          println(s"$RED${BOLD}ERROR$RESET> You typed in the incorrect username or password too many times. Exiting login page...")
          username = ""
          return FAILURE
        }
      }
    }
    FAILURE
  }
  def logout(): Unit = {
    println(s"$GREEN${BOLD}SYSTEM$RESET> <$username> logged out.")
    username = ""
    loggedIn = false
  }
  def register(user_id: Int, username:String, password:String, makeAdmin: Boolean): Int = {
    if(users.contains(username))
      println(s"$RED${BOLD}ERROR$RESET> A user by the name <$BOLD$username$RESET> already exists. Try another username.")
    else {
      try {
        HiveDBC.addUser(user_id, username, password, makeAdmin)
        println(s"$GREEN${BOLD}REGISTRATION MENU$RESET> <$username>'s account has been created successfully!")
        return SUCCESS
      } catch {
        case _: Throwable =>
          println(s"$RED${BOLD}ERROR$RESET> Error creating an account for with the username <$username>. Try again later.")
          return FAILURE
      }
    }
    FAILURE
  }
}
object Main {
  def main(args: Array[String]):Unit = {
    val analyzer = new RevNewsAnalyzer()
    var currCommand = ""

    do {
      println(s"$GREEN${BOLD}MAIN MENU$RESET> Enter a command, type 'help' for more info or ':quit' to exit.")
      print(s"$BOLD${analyzer.username}$RESET" + "> ")
      currCommand = StdIn.readLine.toLowerCase.trim

      if(currCommand == ":create" && analyzer.loggedIn && analyzer.users(analyzer.username)._3) {
        print(s"$GREEN${BOLD}ACCOUNT CREATION MENU$RESET> Enter the user's desired username: ")
        val username: String = StdIn.readLine().toLowerCase().trim
        print(s"$GREEN${BOLD}ACCOUNT CREATION MENU$RESET> Enter the user's desired password: ")
        val password: String = StdIn.readLine().trim.sha256.hash
        print(s"$GREEN${BOLD}ACCOUNT CREATION MENU$RESET> Would you like to make this user an admin? Type 'y' for yes, any other character for no: ")
        var makeAdmin: Boolean = false
        if(StdIn.readLine() == "y")
          makeAdmin = true
        var user_id = -1
        try {
          user_id = HiveDBC.generateNewUserID()
        } catch {
          case _: Throwable =>
            println(s"$RED${BOLD}ERROR$RESET> Error generating new user id. Try again later.")
        }
        if(analyzer.register(user_id, username, password, makeAdmin) == analyzer.SUCCESS) {
          analyzer.users += username -> (password, user_id, makeAdmin)
        }
      } else if(currCommand == ":delete" && analyzer.loggedIn && analyzer.users(analyzer.username)._3) {
        print(s"$GREEN${BOLD}ACCOUNT DELETION MENU$RESET> Enter the user's desired username: ")
        val username: String = StdIn.readLine().toLowerCase().trim
        if(!analyzer.users.contains(username)) {
          println(s"$RED${BOLD}ERROR$RESET> Account deletion failed. There are no active users with the username <$username>.")
        } else {
          if(username == analyzer.username) {
            println(s"$RED${BOLD}ERROR$RESET> Account deletion failed. You cannot delete an account while logged into it")
          } else {
            try {
              val user = analyzer.users(username)
              HiveDBC.deleteUser(user._2, username, user._1, user._3)
              analyzer.users -= username
            } catch {
              case _: Throwable => println(s"$RED${BOLD}ERROR$RESET> Account deletion failed. There was an issue deleting <$username>'s account.")
            }
          }
        }
      } else if(currCommand == "help") {
        analyzer.help()
      } else if (currCommand == "login" && !analyzer.loggedIn) {
        analyzer.login()
      } else if(currCommand == "logout" && analyzer.loggedIn) {
        analyzer.logout()
      } else if(currCommand == "register" && !analyzer.loggedIn) {
        print(s"$GREEN${BOLD}REGISTRATION MENU$RESET> Enter desired username: ")
        val username: String = StdIn.readLine().toLowerCase().trim
        print(s"$GREEN${BOLD}REGISTRATION MENU$RESET> Enter desired password: ")
        val password: String = StdIn.readLine().trim.sha256.hash
        var user_id = -1
        try {
          user_id = HiveDBC.generateNewUserID()
        } catch {
          case _: Throwable =>
            println(s"$RED${BOLD}ERROR$RESET> Error generating new user id. Try again later.")
        }
        if(analyzer.register(user_id, username, password, makeAdmin = false) == analyzer.SUCCESS) {
          analyzer.users += username -> (password, user_id, false)
        }
      } else if(currCommand == "start-analyzer" && !analyzer.users(analyzer.username)._3) {
        if(analyzer.loggedIn)
          do {
            println(s"$GREEN${BOLD}ANALYZER MENU$RESET> Enter one of the following option to utilize the word analyzer.")
            println(s"$GREEN${BOLD}ANALYZER MENU$RESET> 1. Find total relevancy of a word or phrase.")
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

              if(StdIn.readLine().toLowerCase().trim() == "y") {
                println(s"$GREEN${BOLD}ANALYZER MENU$RESET> Saving $BOLD$input$RESET to user's word list...")
                HiveDBC.addWord(analyzer.users(analyzer.username)._2, input, totalResults)
                println(s"$GREEN${BOLD}ANALYZER MENU$RESET> Saved.")
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
            println()
            println()
            println()
            println()
            println()
            println()
            println()
            println()
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
      } else if(currCommand == "saved-words" && analyzer.loggedIn && !analyzer.users(analyzer.username)._3) {
        val user_id: Int = analyzer.users(analyzer.username)._2
        val words: DataFrame = HiveDBC.getUserWords(user_id)

        if(words.count() == 0) {
          println(s"$GREEN${BOLD}WORD LIST MENU$RESET> You currently do not have any words saved.")
        } else {
          words.show()
        }
      } else {
        println(s"$RED${BOLD}ERROR$RESET> Invalid Command! Please try again.")
      }
      (0 to 30).foreach {
        _ => analyzer.loadingBar.foreach { cc =>
          print(s"$GREEN\r$cc$RESET")
          Thread.sleep(5)
        }
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
