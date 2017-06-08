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
    //private static File history;
    // private static FileWriter fout;
    //private static BufferedWriter historyBW;

    public Server() throws FileNotFoundException {


    }

    public static String getTag(String string) {
        return string.split(" ")[0];
    }

    public static void main(String[] args) throws IOException {
        accounts = new Accounts("accounts.txt");
        namePassword = accounts.fillHash();

        //history = new File("history.txt");
        // if(history == null) {
        //      history.createNewFile();
        //  }
        //fout = new FileWriter(history,true);
        //  historyBW = new BufferedWriter(fout);


        System.out.println("chat server");
        ServerSocket listener = new ServerSocket(PORT);

        try {
            while (true) {
                new Connection(listener.accept()).start();
            }
        } finally {
            listener.close();
            //  historyBW.close();
            //  fout.close();

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
            History generalHistory = new History("history.txt");


            //   Scanner fscan = null;
            //  try {
            //     fscan = new Scanner(history);
            // } catch (FileNotFoundException e) {
            //      e.printStackTrace();
            // }

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

            while (true) {
                out.flush();

                String id = null;
                id = "/USERNAME";

                out.println(id);
                try {
                    userLogin = in.readLine();
                    uname = null;
                    String tag = getTag(userLogin);
                    uname = userLogin.split(" ")[1];
                    password = userLogin.split(" ")[2];
                    if (tag.compareTo("/login") != 0) {
                        if (tag.compareTo("/create") == 0) {
                            synchronized (namePassword) {
                                if (!namePassword.containsKey(uname)) {
                                    namePassword.put(uname, password);
                                    accounts.writeAccounts(namePassword);
                                    users.put(uname,out);
                                    History personalHistory = new History(uname + ".txt");
                                    chatHistory.put(uname, personalHistory);
                                    break;
                                } else {
                                    System.out.println("name taken");
                                    // out.print("/BADNAME");
                                    //return;
                                    continue;
                                }
                            }
                        } else {
                            // return;
                            continue;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (uname == null) {
                    // return;
                    continue;
                }
                if (password == null) {
                    //return;
                    continue;
                }
                synchronized (users) {
                    synchronized (namePassword) {
                        if (namePassword.containsKey(uname)) {
                            if (namePassword.get(uname).compareTo(password) == 0) {
                                if (!users.containsKey(uname)) {
                                    users.put(uname, out);
                                } else {
                                    System.out.println("user already logged in");
                                    // return;
                                    continue;
                                }
                                History personalHistory = new History(uname + ".txt");
                                chatHistory.put(uname, personalHistory);

                                break;
                            }
                        } else {
                            System.out.println("invalid password username combo");
                        }
                    }
                }
            }

            out.println("/NAMEACCEPT");
            // while(fscan.hasNextLine())
            {
                //      out.print(fscan.nextLine());
                //  }
                //  fscan.close();
                while (true) {
                    if(!uptodate){
                        generalHistory.read(out);
                        chatHistory.get(uname).read(out);
                        uptodate = true;
                    }

                    try {
                        if (socket.isClosed()) {
                            System.out.print("exiting user");
                            users.remove(uname, out);
                            //    historyBW.close();

                            break;
                        }
                        if (in.ready()) {
                            String input = in.readLine();
                            if (input.compareTo("/exit") == 0) {
                                users.remove(uname, out);
                                //    historyBW.close();
                                break;
                            }
                            if(input.compareTo("/loggedin") == 0){
                                synchronized ((users)){
                                    Enumeration e = users.keys();
                                    out.println("users:");
                                    while(e.hasMoreElements()){
                                        String next = (String)e.nextElement();
                                        out.println(next);
                                    }
                                }
                            }
                            if (input.split(" ")[0].compareTo("/message") == 0) {
                                input = input.replaceFirst("/message", "");
                                synchronized (users) {
                                    Enumeration e = users.keys();
                                    generalHistory.write(uname + ": " + input);
                                    //    historyBW.write(uname + ": " + input +"\n");
                                    while (e.hasMoreElements()) {
                                        String next = (String) e.nextElement();
                                        if (next != uname) {
                                            users.get(next).println(uname + ": " + input);
                                        }
                                    }
                                }
                            } else {
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
                        synchronized (users) {

                        }
                        return;
                    }

                }
            }
        }
    }
}
