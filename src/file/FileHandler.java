package file;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

/* 
    Static class meant to ease file/directory manipulation.
    pathName : current dir
    All string needs to be in format "/dir" or "/file.txt"
    For delete if you give the path "dir1/dir2" will delete all the things insisde dir2 and dir2 inclusiver
*/

public final class FileHandler {

    private static String pathName = System.getProperty("user.dir");

    private FileHandler(){}

    public static String getPathName (){
        return pathName;
    }

    public static boolean createFile(String path, String fileName){
        try {
            File new_file = new File(pathName + "/" + path + "" + fileName );
            if(!new_file.exists()){
                new_file.createNewFile();
                return true;
            }
            else{
                System.err.println("File '" + fileName + "' in path '" + path + "' already exists.");
                return false;
            }
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createDirectory(String path, String dirName){
        try {
            File new_dir = new File(pathName + "/" + path + "" + dirName);
            if(!new_dir.exists()){
                new_dir.mkdirs();
                return true;
            }
            else{
                System.err.println("Directory '" + dirName + "' in path '" + path + "' already exists.");
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

            FileWriter myWriter = new FileWriter(pathName + "/" + path + "" + name  );
            myWriter.write(value);
            myWriter.close();
            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static String readFile(String path, String name){
        try(BufferedReader br = new BufferedReader(new FileReader(pathName + "/" + path + "" + name ))) {
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

    public static boolean deleteDir(String path){
        try{
            File eliminate = new File(pathName + "/" + path);
            
            if(eliminate.exists()){
                String[]entries = eliminate.list();
                deleteDirRec(entries, eliminate.getPath());
                if(eliminate.delete()) return true;
                else System.out.println("Failed to eliminate");
                return false;
            }
            else System.out.println(pathName + "" + path + "doesn't exist");
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteDirRec(String[] entries, String path){
        if(entries==null) return;
        for(String s: entries){
            File currentFile = new File(path,s);
            String[] currentEntries = currentFile.list();
            deleteDirRec(currentEntries, currentFile.getPath());
            currentFile.delete();
        }
        return;
    }

}
