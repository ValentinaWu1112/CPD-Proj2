package file;

import org.json.JSONObject;

public final class JSONHandler{
    public static void jsonTest(){
       JSONObject jo = new JSONObject("{ \"abc\" : \"def\" }");
       System.out.println(jo.toString());
    }
}
