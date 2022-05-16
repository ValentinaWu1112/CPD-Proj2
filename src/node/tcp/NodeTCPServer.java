package node.tcp;

import java.io.*;
import java.net.*;
import java.lang.Thread;

/* 
    Thread responsible for TCP 'server side', i.e, listens for connections and receives messages..
*/

public class NodeTCPServer extends Thread{
    private String raw_ip;
    private InetAddress ip;
    private int port;
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in =  null;
    private DataOutputStream out = null;

    public boolean sendTCPMessage(String message){
        try{
            this.out.writeUTF(message);
            return true;
        }
        catch(IOException e){
            System.err.println(e);
            return false;
        }
    }

    public NodeTCPServer(String ip, int port){
        this.raw_ip = ip;
        this.port = port;
    }

    public void run(){
        try{
            this.ip = InetAddress.getByName(this.raw_ip);
            server = new ServerSocket(this.port, 50, this.ip);
            while(true){
                System.out.println("Accepting TCP connections at " + server.getLocalSocketAddress());
                socket = server.accept();
                System.out.println("Client accepted");

                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());

                String tcp_message = "";
                while (!tcp_message.equals("close")){
                    try{
                        tcp_message = in.readUTF();
                        System.out.println("[TCP]: " + tcp_message);
                    }
                    catch(IOException i){
                        System.out.println(i);
                    }
                }

                socket.close();
                in.close();
                System.out.println("Connection closed.");
            }
        }
        catch(IOException i){
            System.out.println(i);
        }
    }
}
