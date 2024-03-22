package DBActions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader; 
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.*; 

public class JsonReader {
  
  public static String getDataFromJson(String filePath, String key) throws FileNotFoundException, IOException, ParseException {
	  
	  	Object obj = new JSONParser().parse(new FileReader(filePath)); 
	    JSONObject jo = (JSONObject) obj; 
	    String value = (String) jo.get(key);   
	    
	    return value;
  }
}
