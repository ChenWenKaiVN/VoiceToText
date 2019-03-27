package com.voice.text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	public static String getDataPrefix(){
		return df.format(new Date()) + ":  ";
	}

}
