package helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
	BufferedWriter outLog;
	
	public Logger(String filename){

		try {
			outLog = new BufferedWriter(new FileWriter(filename + "Log" + System.currentTimeMillis() + ".txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void log(String message){
		try {
			System.out.println(message);
			outLog.write(message + "\n");
			outLog.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
