/**
 * Created by Antho on 5/25/2017.
 */

import java.io.*;
import java.util.Hashtable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;


public class Server {
    private static int PORT = 5555;
    private static Hashtable<String,PrintWriter> users = new Hashtable<String,PrintWriter>();
    private static Hashtable<String,String> namePassword;
    private static  Accounts accounts;

    public Server() throws FileNotFoundException {


    }

    public static String getTag(String string){
        return string.split(" ")[0];
    }

    public static void main(String [] args) throws IOException{
        accounts = new Accounts("accounts.txt");
        namePassword = accounts.fillHash();
        System.out.println("chat server");
        ServerSocket listener = new ServerSocket(PORT);

        try{
            while(true){
                new Connection(listener.accept()).start();
            }
        } finally{
            listener.close();
        }

    }

    private static class Connection extends Thread{
        private String userLogin;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String uname;
        private String password;

        public Connection(Socket socket){
            this.socket = socket;
        }

        public void run(){
            System.out.println("running");
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

            while(true){
                String id = "USERNAME";
                out.println(id);
                try {
                    userLogin = in.readLine();
                    uname = null;
                    String tag = getTag(userLogin);
                    uname = userLogin.split(" ")[1];
                    password = userLogin.split(" ")[2];
                    if(tag.compareTo("/login") != 0)
                    {
                        if(tag.compareTo("/create") == 0)
                        {
                            synchronized(users) {
                                if (!users.containsKey(uname)) {
                                    synchronized (namePassword) {
                                        namePassword.put(uname, password);
                                        accounts.writeAccounts(namePassword);
                                    }
                                } else {
                                    System.out.println("name taken");
                                }
                            }
                        }
                        else {
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(uname == null)
                {
                    return;
                }
                if(password == null)
                {
                    return;
                }
                synchronized(users){
                    synchronized(namePassword){
                    if(namePassword.containsKey(uname)) {
                        if (namePassword.get(uname).compareTo(password) == 0) {
                            if (!users.containsKey(uname)) {
                                users.put(uname, out);
                            } else {
                                System.out.println("user already logged in");
                                return;
                            }
                            break;
                        }
                    }
                        else
                        {
                            System.out.println("invalid password username combo");
                        }
                    }
                }
            }

            out.println("NAMEACCEPT");
            while(true){
                try {
                    if(in.ready()) {
                        String input = in.readLine();
                        synchronized (users) {
                            Enumeration e = users.keys();
                            while (e.hasMoreElements()) {
                                String next = (String) e.nextElement();
                                if (next != uname) {
                                    users.get(next).println(uname + ": " + input);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    synchronized(users)
                    {
                        users.remove(uname,out);
                    }
                    return;
                }

            }
        }
    }
}
