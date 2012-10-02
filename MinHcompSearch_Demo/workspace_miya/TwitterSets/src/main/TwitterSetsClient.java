package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TwitterSetsClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int port = 9998;
		Socket s;
		String msg = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			InetAddress addr = InetAddress.getByName(null);
			s = new Socket(addr, port);
			OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
			PrintWriter pw = new PrintWriter(osw);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			//System.out.print("Enter a query :  ");
			//String query = "#combine(fifa soccer)##2022";
			String query = "#combine(fifa soccer)##2022";
			pw.println(query);
			pw.flush();
			msg = br1.readLine();
			//System.out.println("Answer from server : ");
			//System.out.println(msg);
		} catch (Exception e) {
			// Ignore
		}
	}
}
