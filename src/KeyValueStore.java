package src;

public interface KeyValueStore{
    /**
    * @return 
    * @param key
    * @param value
    */
    public void put(String key, DataObject value);
    /**
    * @return 
    * @param key
    */
    public void get(String key);
    /**
    * @return 
    * @param key
    */
    public void delete(String key);
}