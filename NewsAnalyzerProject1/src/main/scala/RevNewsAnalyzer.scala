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
        println("        5. saved-words -> prints the user's list of saved words and related information or all.")
        println("        6. trend-words -> prints out a trend of common queries between users.")
        println("        7. update -> opens the update menu and prompts to user to update account info.")
      } else {
        println(s"$GREEN${BOLD}HELP MENU$RESET> Below are the available commands in alphabetical order for regular users.")
        println("        1. help -> prints a list of available commands to use.")
        println("        2. logout -> logs the user out of their account.")
        println("        3. :quit -> logs the user out and quits the program.")
        println("        4. saved-words -> prints the user's list of saved words and related information.")
        println("        5. start-analyzer -> Takes the user to the analyzer menu.")
        println("        6. update -> opens the update menu and prompts to user to update account info.")
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
  def updatePassword(user_id: Int, oldPass: String, newPass: String, admin: Boolean): Unit = {
    if(oldPass == users(username)._1) {
      try {
        HiveDBC.updateUserPassword(user_id, username, newPass, admin)
        users += username -> (newPass, user_id, admin)
        println(s"$GREEN${BOLD}UPDATE MENU$RESET> <$username>'s password successfully updated.")
      } catch {
        case _: Throwable =>  println(s"$RED${BOLD}ERROR$RESET> There has been an error updating <${this.username}>'s password. Try again later.")
      }
    } else {
      println(s"$RED${BOLD}ERROR$RESET> Update failed.Input does not match current password. Try again later.")
    }
  }
  def updateUsername(user_id: Int, newUsername: String, password: String, admin: Boolean): Unit = {
    if(!users.contains(newUsername))
      try {
        HiveDBC.updateUserUsername(user_id, newUsername, password, admin)
        users -= username
        users += newUsername -> (password, user_id, admin)
        println(s"$GREEN${BOLD}UPDATE MENU$RESET> <$username>'s username successfully updated to <$newUsername>.")
        this.username = newUsername
      } catch {
        case _: Throwable =>  println(s"$RED${BOLD}ERROR$RESET> There has been an error updating <${this.username}>'s username to $username. Try again later.")
      }
    else
      println(s"$RED${BOLD}ERROR$RESET> Username cannot be updated. Username already exists.")
  }
}
object Main {
  def main(args: Array[String]):Unit = {
    val analyzer = new RevNewsAnalyzer()
    var currCommand: String = ""
    var apikey: String = "90462b241c624cf68860e583d23155c9"

    do {
      println(s"$GREEN${BOLD}MAIN MENU$RESET> Enter a command, type 'help' for more info or ':quit' to exit.")
      print(s"$BOLD${analyzer.username}$RESET" + "> ")
      currCommand = StdIn.readLine.toLowerCase.trim
      if (currCommand == "apikey") {
        apikey = StdIn.readLine()
      } else if(currCommand == ":create" && analyzer.loggedIn && analyzer.users(analyzer.username)._3) {
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
      } else if(currCommand == "start-analyzer" && analyzer.loggedIn && !analyzer.users(analyzer.username)._3) {
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
              .params(Map[String, String]("q"-> input, "apiKey" -> s"$apikey"))
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
      } else if(currCommand == "saved-words" && analyzer.loggedIn) {
        if (!analyzer.users(analyzer.username)._3) {
          val user_id: Int = analyzer.users(analyzer.username)._2
          val words: DataFrame = HiveDBC.getUserWords(user_id)

          if (words.count() == 0) {
            println(s"$GREEN${BOLD}WORD LIST MENU$RESET> You currently do not have any words saved.")
          } else {
            words.show()
          }
        } else {
          print(s"$GREEN${BOLD}WORD LIST MENU$RESET> Enter a user id to print a user's saved words or 'all' to list all saved words: ")
          val response = StdIn.readLine()
          if (response == "all") {
            HiveDBC.getAllWords().show()
          } else {
            val words = HiveDBC.getUserWords(response.toInt)
            if (words.count() == 0) {
              println(s"$GREEN${BOLD}WORD LIST MENU$RESET> There are not any saved words for the specified user.")
            } else {
              words.distinct().show()
            }
          }
        }
      } else if (currCommand == "trend-words" && analyzer.loggedIn && analyzer.users(analyzer.username)._3) {
        val words = HiveDBC.getTrend()
        words.show()
      } else if (currCommand == "update" && analyzer.loggedIn) {
        print(s"$GREEN${BOLD}UPDATE MENU$RESET> What would you like to update (username/password): ")
        val response: String = StdIn.readLine()
        val user_id: Int = analyzer.users(analyzer.username)._2
        val admin: Boolean = analyzer.users(analyzer.username)._3
        if(response == "username") {
          print(s"$GREEN${BOLD}UPDATE MENU$RESET> Enter desired username: ")
          val newUsername:String = StdIn.readLine()
          print(s"$GREEN${BOLD}UPDATE MENU$RESET> Enter current password: ")
          val password: String = StdIn.readLine().sha256.hash
          analyzer.updateUsername(user_id, newUsername, password, admin)
        } else if (response == "password") {
          print(s"$GREEN${BOLD}UPDATE MENU$RESET> Enter current password: ")
          val currentPassword: String = StdIn.readLine().sha256.hash
          print(s"$GREEN${BOLD}UPDATE MENU$RESET> Enter new password: ")
          val newPassword: String = StdIn.readLine().sha256.hash
          analyzer.updatePassword(user_id, currentPassword, newPassword, admin)
        } else {
          println(s"$RED${BOLD}ERROR$RESET> Invalid input! Try again later. Reverting to main menu.")
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
