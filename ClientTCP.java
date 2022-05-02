import java.net.*;
import java.io.*;

public class ClientTCP{
    // initialize socket and input output streams
    private Socket socket            = null;
    private DataInputStream  input   = null;
    private DataOutputStream out     = null;
    private DataInputStream input_server = null;

    // constructor to put ip address and port
    public ClientTCP(String address, int port){
        // establish a connection
        try{
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from terminal. NO it doesnt omg fuckin poor ass documentation
            // this line simply creates an object THAT ALLOWS you to explicitly
            // read input from whatever stream, system.in in this case.. omg you people
            // dont understand your own code man please..giving me a really hard time
            // comprehending this basic fuckin functionality man
            input  = new DataInputStream(System.in);

            // sends output to the socket
            out    = new DataOutputStream(socket.getOutputStream());

            //receive input Server
            input_server = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }
        catch(UnknownHostException u){
            System.out.println(u);
        }
        catch(IOException i){
            System.out.println(i);
        }

        // string to read message from input
        String line = "";
        String server = "";

        // keep reading until "Over" is input
        while (!line.equals("Over")){
            try
            {
                line = input.readLine();
                out.writeUTF(line);
                server = input_server.readUTF();
                System.out.println(server);
            }
            catch(IOException i)
            {
                System.out.println(i);
            }
        }

        // close the connection
        try{
            input.close();
            out.close();
            socket.close();
        }
        catch(IOException i){
            System.out.println(i);
        }
    }
}
