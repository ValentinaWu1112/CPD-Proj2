public class App{
    public static void main(String args[])
    {
        if(Integer.parseInt(args[0]) == 0){
            ServerTCP server = new ServerTCP(Integer.parseInt(args[1]));
        }
        else{
            ClientTCP client = new ClientTCP(args[1], Integer.parseInt(args[2]));
        }
    }
}