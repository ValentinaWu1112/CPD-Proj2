package node.tcp;

import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.concurrent.TimeUnit;

/* 
    Thread responsible for TCP 'client side', i.e, starting TCP connections, sending messages and
    closing connections. 
*/

public class NodeTCPClient extends Thread{
    private String ip_target;
    private int port_target;
    private volatile DataOutputStream out;
    private DataInputStream input_server;
    private Socket socket;

    public NodeTCPClient(String target, String port){
        this.ip_target = target;
        this.port_target = Integer.parseInt(port);
    }

    public boolean sendTCPMessage(String message){
        try{
            while(!this.getTCPOutSocket()){}
            this.out.writeUTF(message);
            return true;
        }
        catch(IOException e){
            System.err.println(e);
            return false;
        }
    }

    public boolean getTCPOutSocket(){
        return out == null ? false : true;
    }

    public boolean closeTCPConnection(){
        try{
            out.close();
            socket.close();
            System.out.println("Connection closed");
            return true;
        }
        catch(IOException e){
            System.err.println(e);
            return false;
        }
    }

    public void run(){
        try{
            socket = new Socket(this.ip_target, this.port_target);
            System.out.println("Connected");

            out = new DataOutputStream(socket.getOutputStream());
            input_server = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            String tcp_message = "";
            while (!socket.isClosed()){
                try{
                    tcp_message = input_server.readUTF();
                }
                catch(IOException i){
                    System.out.println(i);
                }
            }
        }
        catch(UnknownHostException u){
            System.out.println("UnknownHostException: " + u);
        }
        catch(IOException i){
            System.out.println("IOException: " + i);
        }
    }
}
