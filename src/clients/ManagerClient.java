package clients;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ManagerClient extends Thread{
	
	// Writing streams
	OutputStream os;
	PrintWriter out;
	
	// Reading streams
	InputStream is;
	BufferedReader bf;
	
	int port;
	
	public ManagerClient(int port) {
		this.port = port;
	}

	public void run() {
		try {
			Socket server = new Socket("localhost", port);
			
			// Writer: Output stream
			os = server.getOutputStream();
			out = new PrintWriter(os, true);
			out.flush();
			
			// Reader: Input stream
			is = server.getInputStream();
			InputStreamReader in = new InputStreamReader(is);
			bf = new BufferedReader(in);
			
			// confirm: successful connection
			sop("Connection: " + bf.readLine());
			// write to server
			out.println("manager");	
			out.flush();
			
			String reply;
			while(!(reply = bf.readLine()).equals("true")) {
				sop(reply);
			}
			
			
			bf.close();
			out.close();
			server.close();
			sop("manager: Disconnected");
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void sop(String t) {
		System.out.println(t);
	}

	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]);
		new ManagerClient(port).start();
	}

}
