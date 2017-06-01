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

    BufferedReader read = new BufferedReader(new InputStreamReader(System.in));

    public Client(){

    }

    private void run() throws IOException{
        String serverAddress = "127.0.0.1";
        Socket socket = new Socket(serverAddress,5555);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(),true);

        while(true){
            String line = in.readLine();
            System.out.println(line);
            if(line == "USERNAME"){
                System.out.println("enter username and password:");
                out.println("/login " + read.readLine());
            } else {
                String message = read.readLine();
                if(message != null)
                {
                    out.println(message);
                }
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
