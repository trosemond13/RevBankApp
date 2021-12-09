import scala.Console._
import scala.io.StdIn._

class RevBankApp {
  var users:Map[String, (String, Double)] = JDBC.getUsers()
  var loggedIn:Boolean = false
  var username:String = ""
  var password:String = ""
  var dailyLogins:Int = 0

  val SUCCESS:String = "0"
  val FAILURE:String = "1"

  def login():String = {
    var tries:Int = 3
    if(loggedIn) {
      println("You are already logged in. Type help() for more commands!")
    } else {
      while(!loggedIn) {
        print("Enter your username here -> ");
        username = readLine();
        print("Enter your password here -> ");
        password = readLine();
        if(users.contains(username) && users.get(username).last._1==password) {
          println("Logged in as <" + username + ">")
          loggedIn = true
          dailyLogins = dailyLogins + 1
        }else {
          tries = tries - 1
          if(tries>0) {
            println("Incorrect username or password. " + tries + " more tries left or the program will exit!");
          } else {
            println("You typed in the incorrect username or password too many times. Exiting login page...");
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
      println("No user is logged in. Please enter 'login' to sign in.")
    else {
      loggedIn = false
      username = ""
      println("You have been logged out!")
    }
  }
}

object Main {
  def main(args:Array[String]): Unit = {

    val bank:RevBankApp = new RevBankApp()
    var currCommand = "";

    println("Welcome to Linux Revature Bank App (LinRBA) version 2.3.5!")
    println("Enter your command or type 'help' for more info.")
    do{
      print(s"${BOLD}${bank.username}$RESET" + "> ")
      currCommand = readLine()
      if(currCommand == "login") {
        var login = bank.login
        if(login == "1") {
          println("Password Lock! You have to wait 10 secs before you can process another command.")
          Thread.sleep(10000)
        } else {
          println("You have been logged in as " + bank.username)
        }
      } else if(currCommand == "logout") {
        bank.logout
      }else if(currCommand == ":quit") {
        println("Thank you for using LinRBA services. We appreciate your membership.")
        println("You are now exiting LinRBA's services!")
        println(".....")
        Thread.sleep(1000)
        println("....")
        Thread.sleep(1000)
        println("...")
        Thread.sleep(2000)
        println("Exit complete. Come again soon!")
      } else {
        println("Invalid Command! Please try again. Type ':quit' to exit, or type 'help' for more information.")
      }

    } while(currCommand != ":quit")
  }
}
