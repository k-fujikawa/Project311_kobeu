package sample;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class FactorialServer implements Runnable {
	Socket s;
	int id;

	public static void main(String arg[]) {
		int port = 9999, count = 0;
		try {
			// create new socket
			ServerSocket ss = new ServerSocket(port);
			System.out.println("Waiting for client");
			while (true) {
				Socket s = ss.accept();
				FactorialServer serve = new FactorialServer(s, count);
				// launch new thread
				Thread t = new Thread(serve);
				t.start();
			}
		} catch (Exception e) {
			// Ignore error
			System.out.println("Error");
		}
	}

	FactorialServer(Socket s, int id) {
		this.s = s;
		this.id = id;
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
			String str = br.readLine();
			int n = Integer.parseInt(str);
			int i;
			long f = 1;
			System.out.println("Number sent by client: " + n);
			for (i = 2; i <= n; i++)
				f = f * i;
			pw.println("Factorial is : " + f);
			pw.flush();
		} catch (Exception e) {
			// Ignore error
			System.out.println("Thread: Error");
		}
	}
}