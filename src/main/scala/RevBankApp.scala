import java.text.NumberFormat
import scala.Console._
import scala.io.StdIn._

class RevBankApp {
  var users: Map[String, (String, Double, Int)] = JDBC.getUsers()
  var loggedIn: Boolean = false
  var username: String = ""
  var password: String = ""
  var dailyLogins: Int = 0
  val formatter:NumberFormat = java.text.NumberFormat.getCurrencyInstance
  final val startupScreen:String = "  _      _       _____  ____            ____              _    _             " +
    "\n | |    (_)     |  __ \\|  _ \\   /\\     |  _ \\            | |  (_)            " +
    "\n | |     _ _ __ | |__) | |_) | /  \\    | |_) | __ _ _ __ | | ___ _ __   __ _ " +
    "\n | |    | | '_ \\|  _  /|  _ < / /\\ \\   |  _ < / _` | '_ \\| |/ / | '_ \\ / _` |" +
    "\n | |____| | | | | | \\ \\| |_) / ____ \\  | |_) | (_| | | | |   <| | | | | (_| |" +
    "\n |______|_|_| |_|_|  \\_\\____/_/    \\_\\ |____/ \\__,_|_| |_|_|\\_\\_|_| |_|\\__, |" +
    "\n                                                                        __/ |" +
    "\n                       Loading Bank Services                           |___/ "
  final val loadingBar = List(s"                      $YELLOW**********$RESET\\$BLUE**********$RESET"
    , s"                      $GREEN**********$RESET-$RED**********$RESET"
    , s"                      $CYAN**********$RESET / $MAGENTA**********$RESET"
    , s"                      $WHITE**********$RESET | $YELLOW**********$RESET")
  final val helpMenu:String = "  _    _ ______ _      _____    __  __ ______ _   _ _    _ " +
    "\n | |  | |  ____| |    |  __ \\  |  \\/  |  ____| \\ | | |  | |" +
    "\n | |__| | |__  | |    | |__) | | \\  / | |__  |  \\| | |  | |" +
    "\n |  __  |  __| | |    |  ___/  | |\\/| |  __| | . ` | |  | |" +
    "\n | |  | | |____| |____| |      | |  | | |____| |\\  | |__| |" +
    "\n |_|  |_|______|______|_|      |_|  |_|______|_| \\_|\\____/ " +
    "\n                                                           "
  final val mainMenu:String = "  __  __          _____ _   _   __  __ ______ _   _ _    _ " +
    "\n |  \\/  |   /\\   |_   _| \\ | | |  \\/  |  ____| \\ | | |  | |" +
    "\n | \\  / |  /  \\    | | |  \\| | | \\  / | |__  |  \\| | |  | |" +
    "\n | |\\/| | / /\\ \\   | | | . ` | | |\\/| |  __| | . ` | |  | |" +
    "\n | |  | |/ ____ \\ _| |_| |\\  | | |  | | |____| |\\  | |__| |" +
    "\n |_|  |_/_/    \\_\\_____|_| \\_| |_|  |_|______|_| \\_|\\____/ " +
    "\n                                                           "
  final val depoMenu:String = "  _____  ______ _____   ____    __  __ ______ _   _ _    _ " +
    "\n |  __ \\|  ____|  __ \\ / __ \\  |  \\/  |  ____| \\ | | |  | |" +
    "\n | |  | | |__  | |__) | |  | | | \\  / | |__  |  \\| | |  | |" +
    "\n | |  | |  __| |  ___/| |  | | | |\\/| |  __| | . ` | |  | |" +
    "\n | |__| | |____| |    | |__| | | |  | | |____| |\\  | |__| |" +
    "\n |_____/|______|_|     \\____/  |_|  |_|______|_| \\_|\\____/ " +
    "\n                                                           "
  final val withdMenu:String = " __          _______ _______ _    _ _____    __  __ ______ _   _ _    _ " +
    "\n \\ \\        / /_   _|__   __| |  | |  __ \\  |  \\/  |  ____| \\ | | |  | |" +
    "\n  \\ \\  /\\  / /  | |    | |  | |__| | |  | | | \\  / | |__  |  \\| | |  | |" +
    "\n   \\ \\/  \\/ /   | |    | |  |  __  | |  | | | |\\/| |  __| | . ` | |  | |" +
    "\n    \\  /\\  /   _| |_   | |  | |  | | |__| | | |  | | |____| |\\  | |__| |" +
    "\n     \\/  \\/   |_____|  |_|  |_|  |_|_____/  |_|  |_|______|_| \\_|\\____/ " +
    "\n                                                                        "
  final val transMenu:String = "  _______ _____            _   _  _____   __  __ ______ _   _ _    _ " +
    "\n |__   __|  __ \\     /\\   | \\ | |/ ____| |  \\/  |  ____| \\ | | |  | |" +
    "\n    | |  | |__) |   /  \\  |  \\| | (___   | \\  / | |__  |  \\| | |  | |" +
    "\n    | |  |  _  /   / /\\ \\ | . ` |\\___ \\  | |\\/| |  __| | . ` | |  | |" +
    "\n    | |  | | \\ \\  / ____ \\| |\\  |____) | | |  | | |____| |\\  | |__| |" +
    "\n    |_|  |_|  \\_\\/_/    \\_\\_| \\_|_____/  |_|  |_|______|_| \\_|\\____/ " +
    "\n                                                                     "
  final val balMenu:String = "  ____          _        __  __ ______ _   _ _    _ " +
    "\n |  _ \\   /\\   | |      |  \\/  |  ____| \\ | | |  | |" +
    "\n | |_) | /  \\  | |      | \\  / | |__  |  \\| | |  | |" +
    "\n |  _ < / /\\ \\ | |      | |\\/| |  __| | . ` | |  | |" +
    "\n | |_) / ____ \\| |____  | |  | | |____| |\\  | |__| |" +
    "\n |____/_/    \\_\\______| |_|  |_|______|_| \\_|\\____/ " +
    "\n                                                    "
  final val loginMenu:String = "  _      ____   _____ _____ _   _   __  __ ______ _   _ _    _ " +
    "\n | |    / __ \\ / ____|_   _| \\ | | |  \\/  |  ____| \\ | | |  | |" +
    "\n | |   | |  | | |  __  | | |  \\| | | \\  / | |__  |  \\| | |  | |" +
    "\n | |   | |  | | | |_ | | | | . ` | | |\\/| |  __| | . ` | |  | |" +
    "\n | |___| |__| | |__| |_| |_| |\\  | | |  | | |____| |\\  | |__| |" +
    "\n |______\\____/ \\_____|_____|_| \\_| |_|  |_|______|_| \\_|\\____/ " +
    "\n                                                               "
  final val historyMenu:String = "  _    _ _____  _____ _______ ____  _______     __  __  __ ______ _   _ _    _ " +
    "\n | |  | |_   _|/ ____|__   __/ __ \\|  __ \\ \\   / / |  \\/  |  ____| \\ | | |  | |" +
    "\n | |__| | | | | (___    | | | |  | | |__) \\ \\_/ /  | \\  / | |__  |  \\| | |  | |'" +
    "\n |  __  | | |  \\___ \\   | | | |  | |  _  / \\   /   | |\\/| |  __| | . ` | |  | |" +
    "\n | |  | |_| |_ ____) |  | | | |__| | | \\ \\  | |    | |  | | |____| |\\  | |__| |" +
    "\n |_|  |_|_____|_____/   |_|  \\____/|_|  \\_\\ |_|    |_|  |_|______|_| \\_|\\____/ " +
    "\n                                                                               "

  val SUCCESS: String = "0"
  val FAILURE: String = "1"

  def balance(): Unit = {
    if(!loggedIn)
      println(s"${RED}${BOLD}ERROR${RESET}> No user is logged in. Please enter 'login' to sign in or ':help' for more info.")
    else {
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> Your balance is "+ formatter.format(users.get(username).get._2) +".")
    }
  }
  def deposit(money: Double): Unit = {
    if(!loggedIn)
      println(s"$RED}${BOLD}ERROR$RESET> No user is logged in. Please enter 'login' to sign in or ':help' for more info.")
    else {
      users += (username -> (password, users.get(username).get._2 + money, users.get(username).get._3))
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> You have successfully deposited " + formatter.format(money) + " into your bank account.")
      print(s"${GREEN}${BOLD}SYSTEM${RESET}> Would you like to check your balance? " + "(Y/N): ")
      val res = readLine()
      if(res.toLowerCase == "y")
        println(s"$GREEN${BOLD}SYSTEM$RESET> Your current balance is " + formatter.format(users.get(username).get._2) + ".")
    }
  }
  def adminHelp(): Unit = {
    println(s"${GREEN}${BOLD}SYSTEM${RESET}> Below are the available commands in alphabetical order.")
    println("        1. :create -> opens the create menu and allows admins to create bank accounts.")
    println("        2. :delete -> opens the delete menu and allows admins to delete bank accounts.")
    println("        3. logout -> logs out of admin view.")
    println("        4. :quit -> exits the program")

  }
  def userHelp():Unit = {
    println(s"${GREEN}${BOLD}SYSTEM${RESET}> Below are the available commands in alphabetical order.")
    println ("        1. balance -> returns your current balance.")
    println ("        2. deposit -> opens up the deposit menu so the user can deposit money into their account.")
    println ("        3. help -> prints a list of available commands to use.")
    println ("        4. login -> opens the login menu and prompts the user to login.")
    println ("        4. logout -> logs the user out of their account.")
    println ("        5. :quit -> logs the user out and quits the program.")
    println ("        6. transfer -> opens up the transfer menu so the user can send money to another user's account.")
    println ("        7. withdraw -> opens up the withdraw menu so the user can withdraw money from their account")
  }
  def login(): String = {
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
            println(s"${RED}${BOLD}ERROR${RESET}> Incorrect username or password. " + tries + " more tries left or the program will exit! User/pass inputs are case-sensitive for security purposes.")

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
  def logout(): Unit = {
    if(!loggedIn)
      println(s"${RED}${BOLD}ERROR${RESET}> No user is logged in. Please enter 'login' to sign in or ':help' for more info.")
    else {
      loggedIn = false
      username = ""
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> You have been logged out!")
    }
  }
  def transfer(money:Double, receiver:String): Int = {
    if(!users.contains(receiver)) {
      return 1
    } else {
      val moneyValidator = users.get(username).get._2 - money
      if(moneyValidator >= 0) {
        users += (receiver -> (users.get(receiver).get._1, users.get(receiver).get._2 + money, users.get(receiver).get._3))
        users += (username -> (password, users.get(username).get._2 - money, users.get(username).get._3))
        println(s"${GREEN}${BOLD}SYSTEM${RESET}> Transfer is pending approval... Waiting on success signal.")
        print(s"${GREEN}${BOLD}SYSTEM${RESET}> Would you like to check your balance? " + "(Y/N): ")
        val res = readLine()
        if(res.toLowerCase == "y")
          println(s"${GREEN}${BOLD}SYSTEM${RESET}> Your current balance is " + formatter.format(moneyValidator) + ".")
        0
      } else {
        println(s"${RED}${BOLD}ERROR${RESET}> Insufficient funds. Check your balance.")
        1
      }
    }
  }
  def withdraw(money: Double): Unit = {
    if(!loggedIn)
      println(s"${RED}${BOLD}ERROR${RESET}> No user is logged in. Please enter 'login' to sign in or ':help' for more info.")
    else {
      val moneyValidator:Double = users.get(username).get._2 - money
      if(moneyValidator >= 0) {
        users += (username -> (password, users.get(username).get._2 - money, users.get(username).get._3))
        println(s"${GREEN}${BOLD}SYSTEM${RESET}> You have successfully withdrew " + formatter.format(money) + " from your bank account.")
        print(s"${GREEN}${BOLD}SYSTEM${RESET}> Would you like to check your balance? " + "(Y/N): ")
        val res = readLine()
        if(res.toLowerCase == "y")
          println(s"${GREEN}${BOLD}SYSTEM${RESET}> Your current balance is " + formatter.format(moneyValidator) + ".")
      } else
        println(s"${RED}${BOLD}ERROR${RESET}> Insufficient funds. Check your balance.")
    }
  }
}

object Main {
  def main(args:Array[String]): Unit = {
    val bank:RevBankApp = new RevBankApp()
    var currCommand = ""
    var adminView: Boolean = false;

    print(bank.startupScreen)
    println()
    (0 to 30).foreach {
      _ => bank.loadingBar.foreach { cc =>
        print(s"${GREEN}\r$cc${RESET}")
        Thread.sleep(20)
      }
    }
    println()

    println("Welcome to Linux Revature Banking App (LinRBA) version 2.3.5!")
    do{
      println(bank.mainMenu)
      println(s"${GREEN}${BOLD}SYSTEM${RESET}> Enter a command, type 'help' for more info or ':quit' to exit.")
      print(s"$BOLD${bank.username}$RESET" + "> ")
      currCommand = readLine().toLowerCase()
      if(currCommand == "login" && !adminView) {
        println(bank.loginMenu)
        val login = bank.login
        if(login == "1") {
          println(s"${RED}${BOLD}ERROR${RESET}> Password Lock! You have to wait 10 secs before you can process another command.")
          Thread.sleep(10000)
        }
      } else if(currCommand == "adminview" && !bank.loggedIn){
        print("ADMINVIEW> Join the AdminVPN and type in the root password here -> ")
        if(readLine() == "demonstration") {
          bank.username = "ADMINVIEW"
          adminView = true
          println("ADMINVIEW> You are now in AdminView!")
        } else {
          println("ADMINVIEW> Exitting ADMINVIEW! After too many attempts VPN-IP will be blocked. Contact IT for more info.")
        }
      } else if (currCommand == "accountnumber" && !adminView) {
        var account_number: Int = JDBC.findAccountNumberById(bank.users.get(bank.username).get._3)
        if(account_number == 0)
          println(s"${RED}${BOLD}ERROR${RESET}> Error locating account number. Please contact IT!")
        else {
          println(s"${GREEN}${BOLD}SYSTEM${RESET}> Your account number is [${BOLD}" + account_number + s"${RESET}].")
        }
      } else if(currCommand == "balance" && !adminView) {
        println(bank.balMenu)
        bank.balance
      } else if(currCommand == "deposit" && !adminView) {
        println(bank.depoMenu)
        var error = false
        println(s"$GREEN${BOLD}SYSTEM$RESET> How much would you like to deposit into your account"+"?")
        print(s"$BOLD${bank.username}$RESET" + "> $")
        do {
          try {
            if(error)
              print(s"$BOLD${bank.username}$RESET" + "> ")
            bank.deposit(readDouble())
            error = true
          } catch {
            case e => print("Please enter a valid amount of money. In forms $X, $X.X, $X.XX.\n" +
              s"$BOLD${bank.username}$RESET" + "> $")
          }
          JDBC.updateWithdrawDepositBalances(bank.users.get(bank.username).get._2, bank.users.get(bank.username).get._3)
        } while (!error)
      } else if (currCommand == "withdraw" && !adminView) {
        println(bank.withdMenu)
        var error = false
        println(s"$GREEN${BOLD}SYSTEM$RESET> How much would you like to withdraw from your account"+"?")
        print(s"$BOLD${bank.username}$RESET" + "> $")
        do {
          try {
            if(error)
              print(s"$BOLD${bank.username}$RESET" + "> ")
            bank.withdraw(readDouble())
            error = true
          } catch {
            case e => print("Please enter a valid amount of money. In forms $X, $X.X, $X.XX.\n" +
              s"$BOLD${bank.username}$RESET" + "> ")
          }
          JDBC.updateWithdrawDepositBalances(bank.users.get(bank.username).get._2, bank.users.get(bank.username).get._3)
        } while (!error)
      } else if (currCommand == "transfer" && !adminView) {
        println(bank.transMenu)
        print(s"$GREEN${BOLD}SYSTEM$RESET> Which account would you like to transfer to?")
        print(s"\n$GREEN${BOLD}SYSTEM$RESET> account_number (XXX-XXX): ")
        var account_number_s = readLine()
        account_number_s = account_number_s.replace("-", "")
        val account_number = Integer.parseInt(account_number_s)
        print(s"$GREEN${BOLD}SYSTEM$RESET> How much money would you like to transfer to account [" +account_number_s+"]?")
        print(s"\n$GREEN${BOLD}SYSTEM$RESET> "+"$")
        val amount = Math.abs(readDouble())
        if (bank.transfer(amount, JDBC.findUsernameByAccountNumber(account_number)) == 0) {
          JDBC.updateTransferBalances(amount, bank.users.get(bank.username).get._3, account_number)
          println(s"$GREEN${BOLD}SYSTEM$RESET> You have successfully transferred " + bank.formatter.format(amount) + " into account [" + account_number_s +"].")
        } else {
          println(s"$RED${BOLD}ERROR$RESET> Transfer to account [" + account_number_s +"] failed!")
        }
      } else if (currCommand == "history" && !adminView) {
        println(bank.historyMenu)

      } else if(currCommand == "help") {
        println(bank.helpMenu)
        if (adminView) {
          bank.adminHelp()
        } else {
          bank.userHelp()
        }
      } else if(currCommand == "logout") {
        if(!adminView)
          bank.logout
        else {
          println("Exiting ADMINVIEW. Reverting back to user mode.")
          bank.username = ""
          adminView = false
        }
      } else if(currCommand == ":quit") {
        println(s"$GREEN${BOLD}SYSTEM$RESET> Thank you for using LinRBA services. We appreciate your membership.")
        println(s"$GREEN${BOLD}SYSTEM$RESET> You are now exiting LinRBA's services!")
        println(".....")
        Thread.sleep(1000)
        println("....")
        Thread.sleep(1000)
        println("...")
        Thread.sleep(2000)
        println(s"$GREEN${BOLD}SYSTEM$RESET> Exit complete. Come again soon!")
      } else if (currCommand == ":create" && adminView) {
        print("Account Creation> The newly created account will have the following username: ")
        val usrname: String = readLine();
        if(bank.users.contains(usrname)) {
          println(s"$RED${BOLD}ERROR$RESET> Account creation with the name [" +usrname+"] failed! Username already exists")
        } else {
          print("Account Creation> The account with the username [" +usrname+"] will have the following password: ")
          val pass: String = readLine()
          print("Account Creation> The name on the account is: ")
          val name: String = readLine()
          print("Account Creation> The starting balance: ")
          val bal: Double = readDouble()
          val id = JDBC.createUser(usrname, pass, name, bal)
          bank.users += (usrname -> (pass, bal, id))
        }
      } else if (currCommand == ":delete" && adminView) {
        print("Account Deletion> The username of the account you wish to delete: ")
        val usrname: String = readLine()
        if(!bank.users.contains(usrname))
          println("Account Deletion> Failed! User does not exists")
        else {
          JDBC.deleteUser(usrname)
          bank.users -= usrname
          println("Account Deletion> The account with the username [" + usrname + "] has been successfully deleted.")
        }
      } else {
        println(s"$RED${BOLD}ERROR$RESET> Invalid Command! Please try again.")
      }
      (0 to 30).foreach {
        _ => bank.loadingBar.foreach { cc =>
          print(s"${GREEN}\r$cc${RESET}")
          Thread.sleep(5)
        }
      }
      println()
    } while(currCommand != ":quit")
  }
}
