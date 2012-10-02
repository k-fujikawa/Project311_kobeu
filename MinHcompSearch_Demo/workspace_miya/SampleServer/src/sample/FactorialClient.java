package sample;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

class FactorialClient {
	public static void main(String arg[]) {
		int port = 9999;
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
			System.out.print("Enter a Number :  ");
			String str = br.readLine();

			pw.println(str);
			pw.flush();
			msg = br1.readLine();
			System.out.println("Answer from server : ");
			System.out.println(msg);
		} catch (Exception e) {
			// Ignore
		}
	}
}