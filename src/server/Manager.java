package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class Manager extends Store implements Runnable{
	public static long time = System.currentTimeMillis();
	
	Socket clientSocket;	
	String clientType;
	PrintWriter out;
	BufferedReader bf;

	
	public Manager(int numCustomers, Socket clientSocket, String clientType) {
		NumCustomers = numCustomers;
		parkingLotQueue = new Object[numCustomers];
		
		this.clientSocket = clientSocket;
		this.clientType = clientType;
		try {
			// Writer: Output Stream
			this.out = new PrintWriter(clientSocket.getOutputStream(), true);
			// Reader: Get method to run
			this.bf = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	/**
	 * Manager checks to see if there are Customers waiting,
	 * if they are open the Store, else wait until some people arrive
	 * 
	 * Manager blocks on its Object convey
	 */
	public void openStore() {
		Object convey = new Object();
		synchronized (convey) {
			while(cannotOpenStore(convey)) {
				try {
					msg("      No customers waiting, wait until they arrive");
					convey.wait();
					out.println("manager: executes openStore()");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
			rest();
			msg("     Customers arriving, open doors");
		}
	}
	
	/**
	 * Once the doors are open, the manager waits until Customers form a group or 
	 * there are a less Customers waiting than the Store limit
	 * 
	 * Manager blocks on its Object convey until a group forms 
	 */
	public void letGroupsIn(){
		Object convey = new Object();
		synchronized (convey) {
			while(true) {
				while(cannotLetGroupIn(convey)) {
					try {
						msg("     Wait until the customers form a group or store is empty");
						convey.wait();
						out.println("manager: executes letGroupsIn()");
						if(waitingShoppers == 0)break;
							
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				msg("    Allow Group in, notifyAll on groupsTurn Obj.");
				allowGroupIn();	
				if(waitingShoppers == 0)break;
				
			}
		}
		
	}
	
	// Auxiliary functions
	public void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis() - time)+ "]" + getName() + ": " + m);
	}
	
	public String getName() {
		return "Manager";	}
	
	
	public void rest() {
		try {
            Thread.sleep(1 + (int)(random.nextDouble()* 500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}



	@Override
	public void run() {
		String request;
		
		try {

			openStore();
			letGroupsIn();
			out.println("true");
			
			out.close();
			bf.close();
			clientSocket.close();
			sop("manager: Disconnected");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void sop(String t) {
		System.out.println(t);
	}

}