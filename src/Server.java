/**
 * Created by Antho on 5/25/2017.
 */


import java.io.*;
import java.util.Hashtable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Scanner;


public class Server {
    private static int PORT = 5555;
    private static Hashtable<String, PrintWriter> users = new Hashtable<String, PrintWriter>();
    private static Hashtable<String, String> namePassword = new Hashtable<>();
    private static Hashtable<String, History> chatHistory = new Hashtable<>();
    private static Accounts accounts;


    public Server() throws FileNotFoundException {


    }

    public static String getTag(String string) {
        return string.split(" ")[0];
    }

    public static void main(String[] args) throws IOException {
        //load account information and populate namePassword hash table with it
        accounts = new Accounts("accounts.txt");
        namePassword = accounts.fillHash();



        System.out.println("chat server");
        ServerSocket listener = new ServerSocket(PORT);

        //try to make a connection
        try {
            while (true) {
                //start a new thread with the connection
                new Connection(listener.accept()).start();
            }
        } finally {
            listener.close();

        }

    }

    private static class Connection extends Thread {
        private String userLogin;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String uname;
        private String password;
        private boolean uptodate = false;

        public Connection(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            System.out.println("running");
            //get chat history
            History generalHistory = new History("history.txt");


            //connect up readers and writers so that the client can be written to and read from
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //get login credentials
            while (true) {
                out.flush();

                //send token to client to let it know to prompt user for login
                String id = null;
                id = "/USERNAME";

                out.println(id);
                try {
                    //read response from client
                    userLogin = in.readLine();
                    uname = null;
                    //get the tag that says if this is a login or create new

                    String tag = getTag(userLogin);
                    try {
                        uname = userLogin.split(" ")[1];
                        password = userLogin.split(" ")[2];
                    }catch(ArrayIndexOutOfBoundsException e) {
                        uname = "bob2";
                        password = "111123232";
                    }

                    //if not login
                    if (tag.compareTo("/login") != 0) {
                        //if createing a new user
                        if (tag.compareTo("/create") == 0) {
                            //lock the namePassword table and check if the name is available
                            synchronized (namePassword) {
                                //if it is
                                if (!namePassword.containsKey(uname)) {
                                    //add the new name and password to the namePassword table
                                    //and to the users table
                                    namePassword.put(uname, password);
                                    accounts.writeAccounts(namePassword);
                                    users.put(uname,out);
                                    History personalHistory = new History(uname + ".txt");
                                    chatHistory.put(uname, personalHistory);
                                    //exit the loop
                                    break;
                                } else {
                                   //else print an internam message on the server just for
                                    //checking and then go back to the beginning of the loop
                                    System.out.println("name taken");
                                    continue;
                                }
                            }
                        } else {
                            //if we do not recognize the tag go back to the beginning
                            continue;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (uname == null) {
                    // if they did not provide a name
                    continue;
                }
                if (password == null) {
                    //if they did not provide a password
                    continue;
                }
                //check login credentials if they chose to login instead of create
                synchronized (users) {
                    synchronized (namePassword) {
                        if (namePassword.containsKey(uname)) {
                            if (namePassword.get(uname).compareTo(password) == 0) {
                                if (!users.containsKey(uname)) {
                                    users.put(uname, out);
                                } else {
                                    //if they try to login as someone who is online
                                    //make a note of it on the server and reprompt them
                                    System.out.println("user already logged in");
                                    continue;
                                }
                                //add their personal history to the history table
                                History personalHistory = new History(uname + ".txt");
                                chatHistory.put(uname, personalHistory);

                                //exit the login loop
                                break;
                            }
                        } else {
                            //if login info did not match up reprompt and make a system note.
                            System.out.println("invalid password username combo");
                            continue;
                        }
                    }
                }
            }

            //if login sucess send a token to the client to let it know.
            out.println("/NAMEACCEPT");
            {
                //messaging loop
                while (true) {
                    //if the histories have not been read out to the user yet
                    if(!uptodate){
                        //read them out
                        generalHistory.read(out);
                        chatHistory.get(uname).read(out);
                        //set uptodate to true
                        uptodate = true;
                    }

                    //checck that the socket is still open
                    try {
                        if (socket.isClosed()) {
                            System.out.print("exiting user");
                            users.remove(uname, out);

                            //if it isnt leave the loop
                            break;
                        }
                        //if there is input to read
                        if (in.ready()) {
                            //read it
                            String input = in.readLine();
                            //if the input is the tag /exit then remove the user and break
                            //the message loop
                            if (input.compareTo("/exit") == 0) {
                                users.remove(uname, out);
                                break;
                            }
                            //if the input is the tag /loggedin then read out the list of logged in users
                            if(input.compareTo("/loggedin") == 0){
                                synchronized ((users)){
                                    //e holds the keys by name
                                    Enumeration e = users.keys();
                                    out.println("users:");
                                    while(e.hasMoreElements()){
                                        String next = (String)e.nextElement();
                                        out.println(next);
                                    }
                                }
                            }
                            //if the tag part of the message is /message then this is a general message
                            if (input.split(" ")[0].compareTo("/message") == 0) {
                                input = input.replaceFirst("/message", "");
                                synchronized (users) {
                                    //print out the message to all users except the one who sent it
                                    Enumeration e = users.keys();
                                    generalHistory.write(uname + ": " + input);
                                    while (e.hasMoreElements()) {
                                        String next = (String) e.nextElement();
                                        if (next != uname) {
                                            users.get(next).println(uname + ": " + input);
                                        }
                                    }
                                }
                            } else {
                                //if not assume the tag is a user name and look for that user
                                //if they are found send them the message.
                                String pm = input.split(" ")[0];
                                input = input.replaceFirst(pm, "");
                                pm = pm.replace("/", "");
                                synchronized (users) {
                                    Enumeration e = users.keys();
                                    while (e.hasMoreElements()) {
                                        String next = (String) e.nextElement();
                                        if (next.compareTo(pm) == 0) {
                                            users.get(next).println(uname + ": " + input);
                                            chatHistory.get(next).write(uname + ": " + input);

                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        return;
                    }

                }
            }
        }
    }
}
