package wsnsimulation.model.utils;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GeneratorUtils {
	
	public static JSONObject loadJSONFile(String path) {
		JSONParser parser = new JSONParser();
		try {
			return (JSONObject) parser.parse(new FileReader(path));
		} catch (IOException | ParseException e) {
			if(e instanceof java.io.FileNotFoundException) return null;
			else e.printStackTrace();
		}
		return null;
	}
}
