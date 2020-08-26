package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
	
	public MainServer(int port) {
		try {
			
			
			ServerSocket server = new ServerSocket(port);
			System.out.println("Server Started");
			
			// Writer: Output Stream
			OutputStream os; 
			
			// Reader: Input Stream
			InputStream is;
			
			while(true)
			{
				
				Socket clientSocket = server.accept();
				
				// Convert Output Stream to a PrintWriter
				os = clientSocket.getOutputStream();
				PrintWriter out = new PrintWriter(os, true);
				out.println("successful");
				
				// Convert Input Stream to char stream
				is = clientSocket.getInputStream();
				InputStreamReader in = new InputStreamReader(is);
				BufferedReader bf = new BufferedReader(in);
				
				String clientType = bf.readLine();
				switch(clientType) 
				{
					case "manager":
						sop(clientType + ": connected");
						new Thread(new Manager(20, clientSocket , clientType)).start();
						break;
					case "customer":
						sop(clientType + ": connected");
						new Thread(new Customer(clientSocket , clientType)).start();
						break;
					case "employee":
						sop(clientType + ": connected");
						new Thread(new Employee(clientSocket, clientType)).start();
				}
				
				
				
				
				
			}
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
		new MainServer(port);

	}

}
