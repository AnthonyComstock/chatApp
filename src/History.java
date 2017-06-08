/**
 * Created by Antho on 6/2/2017.
 */
import java.io.*;

public class History {
    File history;
    FileWriter fw;
    FileReader fr;
    BufferedReader br;
    BufferedWriter bw;

    public History(String fileName){
        history = new File(fileName);
            try {
                history.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public boolean write(String message){
        try {
            fw = new FileWriter(history, true);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        bw = new BufferedWriter(fw);

        try {
            bw.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean read(PrintWriter out){
        try {
            fr = new FileReader(history);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        br = new BufferedReader(fr);

        try {
            while(br.ready())
            {
                out.println(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
