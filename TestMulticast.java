import java.io.*;

public class TestMulticast{
    public static void main(String args[]){
        MulticastServer s1 = new MulticastServer();
        System.out.println(s1);
        MulticastClient c1 = new MulticastClient();
    }
}