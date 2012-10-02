package natto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NattoUtils {

	public static Date parseID2Date(String id) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		return sdf.parse(id); 
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
