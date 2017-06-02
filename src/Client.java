/**
 * Created by Antho on 5/25/2017.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



public class Client {
    BufferedReader in;
    PrintWriter out;
    String name;
    boolean loggedIn = false;

    BufferedReader read = new BufferedReader(new InputStreamReader(System.in));

    public Client(){

    }

    private void run() throws IOException{
        String serverAddress = "127.0.0.1";
        Socket socket = new Socket(serverAddress,5555);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(),true);

        while(true){
            while(!loggedIn){
                //while not logged in
                String line = in.readLine();
                //poll for username and password
                if(line.compareTo("/BADNAME") == 0)
                {
                    System.out.println("wrong username or password or the username was taken");
                    line = "/USERNAAME";
                }
                if(line.compareTo("/USERNAME")==0){
                    System.out.println("login or create account:");
                    String response;
                    //response will be turned into a button press to avoid ambiguous answers
                    response = read.readLine();
                    //if a username needs to be created
                    if(response.compareTo("/create") == 0)
                    {
                        System.out.println("enter new username and password:");
                        String accountInfo = read.readLine();
                        out.println("/create " + accountInfo);
                    }
                    //else entering a valid username, this will be abstracted by the GUI so there
                    //is no other options
                    else {
                        System.out.println("enter username and password:");
                        out.println("/login " + read.readLine());
                    }
                }
                if(line.compareTo("/NAMEACCEPT") == 0)
                {
                    loggedIn = true;
                }
            }
                    while(!read.ready())
                    {
                        if(in.ready()) {
                            String line = in.readLine();
                            System.out.println(line);
                        }
                    }
                    String message = read.readLine();
                    if (message != null) {
                        out.println(message);
                    }
        }
    }
    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
