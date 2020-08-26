package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

class Employee extends Store implements Runnable{
	 
	Socket clientSocket;	
	String clientType;
	PrintWriter out;
	BufferedReader bf;
	
	public static long time = System.currentTimeMillis();
	
	public Employee(Socket clientSocket, String clientType) {
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

	@Override
	public void run() {
		
		try {

			startWorking();
			helpCustomers();
			helpParkingLot();
			
			rest();
			msg("   Leaves the parking lot");
			
			out.close();
			bf.close();
			clientSocket.close();
			sop("employee: Disconnected");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	

	/**
	 * Employee tries to enter the store and start working
	 * If the Manager hasn't opened the store yet, wait until its open
	 * 
	 * Employee blocks on its object convey
	 */
	public void startWorking() {
		Object convey = new Object();
		rest();
		synchronized (convey) {
			while(cannotStartWorking(convey)) {
				try {
					msg("   Wait for store to open");
					convey.wait();
					out.println("employee: executed startWorking()");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			isWorking = true;
		}
	}
	
	/**
	 * Once the Employee enters the Store, he can start working
	 * The Employee waits until the Registers are full and a Customer is waiting 
	 * for a Register. 
	 * 
	 * A Customer that finished using a register notifies the Employee, 
	 * who then notifies the waiting Customer
	 * 
	 * Employee blocks on its Object convey
	 * 
	 */
	public void helpCustomers() {
		msg("   Enter Store and start working");
		Object convey = new Object();
		synchronized (convey) {
			while(true) {
				waitUntilAskedForHelp(convey);
				try {
					convey.wait();
					out.println("employee: executed helpCustomers()");
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(shoppersWaitRegister.size()>0) {
					msg("    Notify register available");
					directShopperToRegister(convey);	
				}	
				
				if(shoppersWaitingToLeave == NumCustomers) {
					break;
				}	
				
			}
		}
		
	}
	
	/**
	 * Once the Employee finishes helping Customers in the store, he 
	 * closes and exits the store to the parking lot
	 * 
	 * Once in the parking lot he notifies the Customers one by one that
	 * they can leave in the order they came in
	 * 
	 * Employee waits on its Object convey until the Customer leaves
	 */
	public void helpParkingLot() {
		msg("   Closes store, walks out");
		Object convey = new Object();
		synchronized(convey){
			msg("   Direct shoppers to leave in ascending order.");
			for(int i = 0; i < NumCustomers; i++) {
				waitForCustomerToLeave(convey);
				try {
					letShopperLeave(parkingLotQueue[i]);
					convey.wait();
					out.println("employee: executed helpParkingLot()");
					rest();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			out.println("true");
		}
	}
	
	// Auxiliary functions
	public void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis() - time)+ "]" + getName() + ": " + m);
	}
	
	public String getName() {
		return "Employee";	}
	
	
	public void rest() {
		try {
            Thread.sleep(1 + (int)(random.nextDouble()* 700));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

	public static void sop(String t) {
		System.out.println(t);
	}
	

}
