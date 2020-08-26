package clients;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class CustomerClient extends Thread{
	// Writing streams
	OutputStream os;
	PrintWriter out;
	
	// Reading streams
	InputStream is;
	BufferedReader bf;
	int id;
	
	int port;
	
	public CustomerClient(int i, int port) {
		this.id = i;
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
			sop("Customer"+ id + " Connection: " + bf.readLine());
			// write to server
			out.println("customer");	
			
			
			String reply;
			while(!(reply = bf.readLine()).equals("true")) {
				sop(reply);
			}
			
			out.close();
			bf.close();
			server.close();
			sop("Customer"+ id + " Disconnected");
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
		for(int i = 0; i < 20; i++) {
			new CustomerClient(i, port).start();
		}	
	}

}
