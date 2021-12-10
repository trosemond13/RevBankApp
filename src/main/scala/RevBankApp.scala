import scala.Console._
import scala.io.StdIn._

class RevBankApp {
  var users:Map[String, (String, Double)] = JDBC.getUsers()
  var loggedIn:Boolean = false
  var username:String = ""
  var password:String = ""
  var dailyLogins:Int = 0
  val formatter = java.text.NumberFormat.getCurrencyInstance

  val SUCCESS:String = "0"
  val FAILURE:String = "1"

  def login():String = {
    var tries:Int = 3
    if(loggedIn) {
      println(s"${RED}${BOLD}ERROR${RESET}> You are already logged in.")
    } else {
      while(!loggedIn) {
        print(s"${GREEN}${BOLD}SYSTEM${RESET}> Enter your username here -> ")
        username = readLine()
        print(s"${GREEN}${BOLD}SYSTEM${RESET}> Enter your password here -> ")
        password = readLine()
        if(users.contains(username) && users.get(username).last._1==password) {
          println(s"${GREEN}${BOLD}SYSTEM${RESET}> Logged in as <" + username + ">")
          loggedIn = true
          dailyLogins = dailyLogins + 1
        }else {
          tries = tries - 1
          if(tries>0) {
            println(s"${RED}${BOLD}ERROR${RESET}> Incorrect username or password. " + tries + " more tries left or the program will exit!")
          } else {
            println(s"${RED}${BOLD}ERROR${RESET}> You typed in the incorrect username or password too many times. Exiting login page...")
            username = ""
            return FAILURE
          }
        }
      }
    }
    username
  }
  def logout():Unit = {
    if(!loggedIn)
      println(s"${RED}${BOLD}ERROR${RESET}> No user is logged in. Please enter 'login' to sign in or ':help' for more info.")
    else {
      loggedIn = false
      username = ""
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> You have been logged out!")
    }
  }
  def deposit(money: Double):Unit = {
    if(!loggedIn)
      println(s"${RED}${BOLD}ERROR${RESET}> No user is logged in. Please enter 'login' to sign in or ':help' for more info.")
    else {
      users += (username -> Tuple2(password, (users.get(username).get._2 +money)))
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> You have successfully deposited " + formatter.format(money) + " into your bank account.")
    }
  }
  def balance():Unit = {
    if(!loggedIn)
      println(s"${RED}${BOLD}ERROR${RESET}> No user is logged in. Please enter 'login' to sign in or ':help' for more info.")
    else {
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> Your balance is "+ formatter.format(users.get(username).get._2) +".")
    }
  }
}

object Main {
  def main(args:Array[String]): Unit = {

    val bank:RevBankApp = new RevBankApp()
    var currCommand = ""

    println("Welcome to Linux Revature Bank App (LinRBA) version 2.3.5!")
    do{
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> Enter a command, type ':quit' to exit or 'help' for more info.")
      print(s"$BOLD${bank.username}$RESET" + "> ")
      currCommand = readLine().toLowerCase()
      if(currCommand == "login") {
        val login = bank.login
        if(login == "1") {
          println(s"${RED}${BOLD}ERROR${RESET}> Password Lock! You have to wait 10 secs before you can process another command.")
          Thread.sleep(10000)
        }
      } else if(currCommand == "balance") {
        bank.balance
      } else if(currCommand == "deposit") {
        println(s"${GREEN}${BOLD}SYSTEM${RESET}> How much would you like to deposit into your account"+"?")
        print(s"$BOLD${bank.username}$RESET" + "> ")
        bank.deposit(readDouble())

      } else if (currCommand == "withdraw") {

      } else if (currCommand == "transfer") {

      } else if (currCommand == "history") {

      } else if(currCommand == "logout") {
        bank.logout
      } else if(currCommand == ":quit") {
        println(s"${GREEN}${BOLD}SYSTEM${RESET}> Thank you for using LinRBA services. We appreciate your membership.")
        println(s"${GREEN}${BOLD}SYSTEM${RESET}> You are now exiting LinRBA's services!")
        println(".....")
        Thread.sleep(1000)
        println("....")
        Thread.sleep(1000)
        println("...")
        Thread.sleep(2000)
        println(s"${GREEN}${BOLD}SYSTEM${RESET}> Exit complete. Come again soon!")
      } else {
        println(s"${RED}${BOLD}ERROR${RESET}> Invalid Command! Please try again.")
      }
    } while(currCommand != ":quit")
  }
}
