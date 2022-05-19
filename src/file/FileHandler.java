package file;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

/* 
    Static class meant to ease file/directory manipulation.
*/

public final class FileHandler {

    private FileHandler(){}

    public static boolean createFile(String path, String name){
        try {
            File new_file = new File(path + "" + name + ".txt");
            if(!new_file.exists()){
                new_file.createNewFile();
                return true;
            }
            else{
                System.err.println("File '" + name + "' in path '" + path + "' already exists.");
                return false;
            }
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createDirectory(String path, String name){
        try {
            File new_dir = new File(path + "" + name);
            if(!new_dir.exists()){
                new_dir.mkdirs();
                return true;
            }
            else{
                System.err.println("Directory '" + name + "' in path '" + path + "' already exists.");
                return false;
            }
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /* 
        Note: this method will overwrite everything in the specified file! 
            It also creates the specified path+name file, this makes the 
            createFile method sort of useless.
    */
    public static boolean writeFile(String path, String name, String value){
        try {
            FileWriter myWriter = new FileWriter(path + "" + name + ".txt");
            myWriter.write(value);
            myWriter.close();
            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static String readFile(String path, String name){
        try(BufferedReader br = new BufferedReader(new FileReader(path + "" + name + ".txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
        
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            return everything;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
