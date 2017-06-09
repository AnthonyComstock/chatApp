/**
 * Created by Antho on 5/25/2017.
 */

import java.util.Enumeration;
import java.util.Scanner;
import java.io.*;
import java.util.Hashtable;

public class Accounts {
    //reads and writes usernames and passwords to file as well
    //as updateing the hashtable
    private static File in = null;
    private static Scanner sc = null;
    private static String filename = null;


    public Accounts(String filename) throws FileNotFoundException{
        //if the file exists grab it or else make it.
            in = new File(filename);
        try {
            in.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.filename = filename;

    }

    public Hashtable<String,String> fillHash() throws FileNotFoundException{
        //create a hashtable full of all of the entries in the account.txt file
        //return that out.
        Hashtable<String,String> hash = new Hashtable<String,String>();
        sc = new Scanner(in);
        while(sc.hasNextLine())
        {
            String name = sc.nextLine();
            String password = sc.nextLine();
            hash.put(name,password);
        }
        sc.close();
        return hash;
    }

    public void writeAccounts(Hashtable<String,String> hash) throws IOException{
        //rewrite the account.txt file with any new information from the hash.
        Enumeration e = hash.keys();
        FileWriter fWrite = new FileWriter(in);
        PrintWriter pWrite = new PrintWriter(fWrite);
        while(e.hasMoreElements()) {
            String name = (String) e.nextElement();
            String password = hash.get(name);
            pWrite.println(name);
            pWrite.println(password);

        }
        pWrite.close();
    }
}
