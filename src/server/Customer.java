package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class Customer extends Store implements Runnable{
	int ticket;
	int group;
	public static long time = System.currentTimeMillis();
	
	Socket clientSocket;	
	String clientType;
	PrintWriter out;
	BufferedReader bf;

	public Customer(Socket clientSocket, String clientType) {
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
		rest();
		arrieveAtStore();
		rest();
		formGroup();
		doShopping();
		checkOut();
		waitParkingLot();
		msg( "Leaves the parking lot");
		
		
		try {
			out.close();
			bf.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sop("customer: " + this.ticket + ": disconnected");
	}
	/**
	 *  Customer arrives at the store, if closed wait in line in the order they came in
	 *  Once the Manager opens the doors, Customers are notified they can start 
	 *  forming groups.
	 *  
	 *  Customers wait in their Object convey
	 *  
	 *  Platoon policy implemented as a Customer in front of the line notifies a Customer
	 *  behind him, who also notifies the Customer behind him and so on.
	 */
	public void arrieveAtStore() {
		Object convey = new Object();
		synchronized (convey) {
			this.ticket = getTicket();
			while(cannotEnterStore(convey)) {
				try {
					convey.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
			msg(" Arrive at store, wait in line");
			out.println("customer" + this.ticket + ": executed arrieveAtStore()");
			rest();
			if(shoppersWaitOpen.size()>0)
				notifyShopperBehindYou();
		}
	}
	
	/**
	 * Once the Manager opens the doors, a Customer is assigned a group depending 
	 * on the order they came in. A Customer gets its group and blocks on Object groupsTurn
	 * if their group is assigned to enter next. They block until the manager notifiesAll on object
	 * groupsTurn and allowed them to enter the store.
	 *  
	 * Groups have to be of max six 6 if there are 6 or more
	 * Customers waiting to enter the store.
	 */
	public void formGroup() {
		synchronized (notGroupsTurn) {
			this.group = this.ticket / 6;
			while(cannotBePartOfGroup(this.group)) {
				try {
					notGroupsTurn.wait();
					
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
				rest2();
			}
		}
		synchronized (groupsTurn) {
			try {
				msg("  joins group " + this.group + ", block on object groupsTurn");
				groupsTurn.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			out.println("customer" + this.ticket + ": executed formGroup()");
			rest2();
			enterStore();
			msg(" Enters store with group " + this.group);	
		}
		
	}
		
	/**
	 * Puts a Customer Thread to sleep to simulate shopping
	 */
	public void doShopping() {
		
		out.println("customer" + this.ticket + ": executed doShopping()");
		rest();
		msg("  Start shopping");
		rest();
	}
	
	/**
	 * Once a Customer is done shopping, they can attempt to check out.
	 * They can only use every other register
	 * so if 4 registers, then only register 1 and 3 can be used.
	 * 
	 * If there is no free registers, then they wait in their Object convey.
	 * Once they check out, they leave the store to the parking lot, notifying 
	 * the Manager there is one less Customer in the store 
	 */
		public void checkOut() {
			Object convey = new Object();
			synchronized (convey) {
				while(cannotCheckOut(convey, this.ticket)) {
					msg(" Wait for register");
					try {
						convey.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				String register = getRegister(this.ticket);
				msg("   self-checkout in " + register);
				out.println("customer" + this.ticket + ": executed checkOut()");
				rest();
				freeResisterExitStore(this.ticket);
			}
		}
	
	/**
	 * Once in the parking lot, the Customer waits until the Employee is done
	 * helping all the Customers before they can leave the parking lot
	 * 
	 * Customer blocks in their Object convey, until notified by the Employee
	 */
	public void waitParkingLot() {
		Object convey = new Object();
		synchronized (convey) {
			if(cannotLeaveParkingLot(convey, this.ticket)) {
				try {
					msg(" Waits at the parking lot");
					convey.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			out.println("customer" + this.ticket + ": executed waitParkingLot()");
			leaveStore();
			out.println("true");
		}
	}
	
	public static void sop(String t) {
		System.out.println(t);
	}
	
	// Auxiliary function 
	public String getName() {
		String name = "Customer " + this.ticket; 
		return name;
	}
	
	public void rest() {
		try {
            Thread.sleep(1 + (int)(random.nextDouble()* 3000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
	
	public void rest2() {
		try {
            Thread.sleep(1 + (int)(random.nextDouble()* 500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
	
	public void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis() - time)+ "]" + getName() + ": " + m);
	}
	
	
	
	

}