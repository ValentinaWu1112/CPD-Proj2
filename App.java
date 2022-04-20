public class App{
    public static void main(String args[])
    {
        if(Integer.parseInt(args[0]) == 0){
            ServerTCP server = new ServerTCP(8000);
        }
        else{
            ClientTCP client = new ClientTCP("127.0.0.1", 8000);
        }
    }
}