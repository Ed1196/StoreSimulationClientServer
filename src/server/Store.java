package server;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

class Store {
	
	Store(){
		registers = new int[numSelf_checkout];
		Arrays.fill(registers, -1);
		
	}
	// Vectors used before store opens
	protected static Vector<Object> shoppersWaitOpen = new Vector<Object>();
	protected static Vector<Object> employeeWaitsOpen = new Vector<Object>();
	protected static Vector<Object> managerWaitsOpen = new Vector<Object>();
	
	// Objects/Vectors used for letting groups in
	protected static Object notGroupsTurn = new Object();
	protected static Object groupsTurn = new Object();
	protected static Vector<Object> managerWaitsOnGroup = new Vector<Object>();
	
	// Objects/Vector used for shoppers once in the store
	protected static Vector<Object> shoppersWaitRegister = new Vector<Object>();
	protected static Vector<Object> employeeWaitToHelp = new Vector<Object>();
	
	// Queue/Vector used for shoppers leaving the parking lot
	protected static Object [] parkingLotQueue;
	protected static Vector<Object> employeeWaitsParkingLot = new Vector<Object>();
	
	
	
	
	// Local Variables
		// Store is closed at the start
	protected static boolean isOpen = false;
		// Boolean to let the Manager know there is people in the store
	protected static boolean fullStore = false;
		// number of Customers waiting with for store to open
	protected static int waitingShoppers = 0;
		// Counter for current group allowed in the store
	protected static int  groupCounter = 0;
		// Number of people in the group
	protected static int peopleInGroup = 0;
		// Counter for tickets, keeps track of the amount of Customers
	protected static int ticketCount = -1;
		// Counter for Customers inside the store
	protected static int peopleInsideStore = 0;
		// Boolean to check if Employee done working inside the store
	protected static boolean isWorking = false;
		// Counters for Customers in the parking lot
	protected static int NumCustomers = 0;
	protected static int shoppersWaitingToLeave = 0;
		
	// Initial Values for Store:
	protected static int Store_capacity = 6;
	protected static int numSelf_checkout = 4;
	protected static int [] registers;
	
	public static final Random random = new Random();
	
	/**
	 * Employee Methods:
	 * 
	 * Employee checks if the store is open, if not store its Notification Object in the
	 * Vector employeeWaitsOpen so that it can notified later on.
	 * 
	 * @param convey, Notification object for Employee
	 * @return boolean that represents if the Employee can start working or will block
	 */
	public synchronized boolean cannotStartWorking(Object convey) {
		// TODO Auto-generated method stub
				boolean status;
				if(isOpen) {
					status = false;
				} else {
					employeeWaitsOpen.addElement(convey);
					status = true;
				}
				return status;
	}
	
	/**
	 * Employee Methods:
	 * 
	 * Employees Notification Object is stored in Vector employeeWaitHelp until a Customer 
	 * notifies that it has finished using a register.
	 * 
	 * @param convey, Notification object for Employee
	 * 
	 */
	public synchronized void waitUntilAskedForHelp(Object convey) {
		if(employeeWaitToHelp.size() == 0)
			employeeWaitToHelp.addElement(convey);
	}
	
	/**
	 * Employee Methods:
	 * 
	 * Once a Customer notifies the Employee that its done with a register,
	 * the Employee checks to see if there are Customers waiting to use the register,
	 * if they are, then notify to move to the register
	 */
	public synchronized void directShopperToRegister(Object convey) {
		synchronized(shoppersWaitRegister.elementAt(0)) {
			shoppersWaitRegister.elementAt(0).notify();
		}
		shoppersWaitRegister.removeElementAt(0);
		employeeWaitToHelp.addElement(convey);
	}
	
	
	
	/**
	 * Employee Methods:
	 * 
	 * Employee notifies a Customer to leave the parking lot
	 * 
	 * @param convey Notification Object for Customer
	 */
	public synchronized void letShopperLeave(Object convey) {
		synchronized(convey) {
			convey.notify();
		}
	}
	
	/**
	 * Employee Methods:
	 * 
	 * Employee notifies a Customer to leave the parking lot, then it stores its Notification
	 * Object in Vector employeeWaitsParkingLot. It will notified once the Customer leaves the parking lot,
	 * then it will notify the next Customer.
	 * 
	 * @param convey Notification Object for Employee
	 */
	public synchronized void waitForCustomerToLeave(Object convey) {
		employeeWaitsParkingLot.addElement(convey);
	}
	

	/**
	 * Customer Method:
	 * 
	 * If the store is not open, store Customers Notification Object so that it can
	 * be notified later once the store opens. 
	 * 
	 * If the store is open, increase the number of waitingShoppers
	 * 
	 * @param convey Notification Object for Customer
	 * @return boolean that represents if the store is open or not
	 */
	public synchronized boolean cannotEnterStore(Object convey) {
		boolean status;
		if(isOpen) {
			waitingShoppers++;
			status = false;
		} else {
			shoppersWaitOpen.addElement(convey);
			if(managerWaitsOpen.size() > 0) {
				synchronized (managerWaitsOpen.elementAt(0)) {
					managerWaitsOpen.elementAt(0).notify();
				}
				managerWaitsOpen.removeElementAt(0);
			}
			status = true;
		}
		return status;
	}
	
	/**
	 * Customer Method:
	 * 
	 * Assigns tickets to customers as they arrive
	 * 
	 * @return ticket number
	 */
	public synchronized int getTicket() {
		ticketCount++;
		return ticketCount;
	}
	
	/**
	 * Customer Method:
	 * 
	 * Increases the number of customers in the store. 
	 * This is done in counter peopleInsideStore.
	 */
	public synchronized void enterStore(){
		peopleInsideStore++;
		if(peopleInsideStore == Store_capacity) {
			synchronized(notGroupsTurn) {
				notGroupsTurn.notifyAll();
			}
		}
	}
	
	/**
	 * Customer Method:
	 * 
	 * Platoon policy to notify the Customer behind us that the store is open
	 */
	public synchronized void notifyShopperBehindYou(){
        if(shoppersWaitOpen.size() > 0) {
            // Notify shopper that the store is open
            synchronized (shoppersWaitOpen.elementAt(0)) {
            	shoppersWaitOpen.elementAt(0).notify();
            }
            shoppersWaitOpen.removeElementAt(0);
        }
    }

	/**
	 * Customer Method:
	 * 
	 * Using the Customers group number, it checks that number to the group that is allowed to enter next. 
	 * 
	 * @param group Customers group number
	 * @return Whether the Customer is part of the group or not
	 */
	public synchronized boolean cannotBePartOfGroup(int group) {
		if(group == groupCounter) {
			peopleInGroup++;
			waitingShoppers--;
			if(peopleInGroup == Store_capacity) {
				
				if(managerWaitsOnGroup.size() > 0) {
					synchronized(managerWaitsOnGroup.elementAt(0)){
						managerWaitsOnGroup.elementAt(0).notify();
					}
					managerWaitsOnGroup.removeElementAt(0);
				}
			}
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * Customer Method:
	 * 
	 * Since we can only use every other register, 
	 * for 4 registers we can only use 1 or 3, registers 2 and 4 are not used as to keep
	 * people away from each other.
	 * 
	 * A register that is not used has a value of -1. If a Customer starts using it, that -1 is
	 * replaced with the ticket number of the Customer using it
	 * 
	 * @param convey Notification Object for Customer
	 * @param ticket Customers ticket of when they arrived to the store
	 * @return Whether a Customer can check out or not
	 */
	public synchronized boolean cannotCheckOut(Object convey, int ticket){
		
			for(int i = 1; i <= registers.length; i=i+2 ) {
				if(registers[i] == -1) {
					registers[i] = ticket;
					return false;
				}
			}
			
			shoppersWaitRegister.addElement(convey);	
		return true;
	}
	
	/**
	 * Just returns the register that a Customer is using. Only used so that it can be printed
	 * to the console.
	 * 
	 * @param ticket Customers ticket
	 * @return Register that the Customer is using
	 */
	public String getRegister(int ticket) {
		String register = "";
		for(int i = 1; i <= registers.length; i=i+2 ) {
			if(registers[i] == ticket) {
				register = "register"+ i;
				break;
			}
		}
		return register;
	}
	
	/**
	 * Customer finished using the register. It does so by restoring the register value to -1.
	 * On its way out, it notifies the Employee that there is a register free.
	 * It also checks to see if they are the last Customer in the group, if they are, the Customer
	 * notifies the manager that its fine to allow the next group in
	 * 
	 * @param ticket Customers ticket number
	 */
	public synchronized void freeResisterExitStore(int ticket) {
		// Free the register
		for(int i = 1; i <= registers.length; i=i+2 ) {
			if(registers[i] == ticket) {
				registers[i] = -1;
				break;
			}
		}
		
		// Dec. the number of people in the store
		peopleInsideStore--;
		
		if(employeeWaitToHelp.size() > 0) {
			synchronized(employeeWaitToHelp.elementAt(0)){
				employeeWaitToHelp.elementAt(0).notify();
			}
			employeeWaitToHelp.removeElementAt(0);
		}
		

		if(peopleInsideStore == 0) {
			fullStore = false;
			
			if(waitingShoppers == 0) {	
				if(managerWaitsOnGroup.size() > 0) {
					synchronized(managerWaitsOnGroup.elementAt(0)){
						managerWaitsOnGroup.elementAt(0).notify();
					}
					managerWaitsOnGroup.removeElementAt(0);
					
				}
			} else if(managerWaitsOnGroup.size() > 0) {
				synchronized(managerWaitsOnGroup.elementAt(0)){
					managerWaitsOnGroup.elementAt(0).notify();
				}
				managerWaitsOnGroup.removeElementAt(0);
			}
		}
	}
	
	/**
	 * Customer Method:
	 * 
	 * Leave the parking lot, notify Employee you have left
	 */
	public synchronized void leaveStore() {
		if(employeeWaitsParkingLot.size() > 0) {
			synchronized(employeeWaitsParkingLot.elementAt(0)) {
				employeeWaitsParkingLot.elementAt(0).notify();
			}
			employeeWaitsParkingLot.removeElementAt(0);
		}
	}
	
	
	/**
	 * Customer Method:
	 * 
	 * Once a customer leaves the store, it will block waiting in the parking lot
	 * Its Notification object will be stored in the parkingLotQueue to be notified
	 * later by the Employee
	 * 
	 * @param convey Customer Notification Object
	 * @param ticket Customers ticket number
	 * @return Whether a Customer can leave or not
	 */
	public synchronized boolean cannotLeaveParkingLot(Object convey, int ticket) {
		if(isWorking) {
			shoppersWaitingToLeave++;
			parkingLotQueue[ticket] = convey;
			
			return true;
		}
		return false;
	}

	/**
	 * Manager Methods:
	 *
	 * Store will not be opened until some Customers wait outside. Manger Notification
	 * Object will be added to the employeeWaitOpen Vector to be notified later
	 * once some Customers arrive and are added to the shopperWaitOpen vector. 
	 * 
	 * @param convey Notification Object for Manager
	 * @return Whether the manager can open the store or not
	 */
	public synchronized boolean cannotOpenStore(Object convey) {
	if(shoppersWaitOpen.size() > 0) {
		synchronized (shoppersWaitOpen.elementAt(0)) {
			shoppersWaitOpen.elementAt(0).notify();
		}
		shoppersWaitOpen.removeElementAt(0);
		isOpen = true; 
		if(employeeWaitsOpen.size() > 0) {
			synchronized (employeeWaitsOpen.elementAt(0)) {
				employeeWaitsOpen.elementAt(0).notify();
			}
			employeeWaitsOpen.removeElementAt(0);
		}
			return false;
		} else {
			managerWaitsOpen.addElement(convey);
			return true;
		}
	}

	// 
	/**
	 * Manager Method:
	 * 
	 * Don't let the group in until its group is of size Store_capacity
	 * or the number of waiting Customers is less than the Store_capacity
	 * 
	 * @param convey Manager Notification Object
	 * @return
	 */
	public synchronized boolean cannotLetGroupIn(Object convey) {
	if(peopleInGroup != Store_capacity || fullStore) {
		if(managerWaitsOnGroup.size() == 0)
			managerWaitsOnGroup.addElement(convey);
		return true;
	}
	
	return false;
}

	/**
	 * Manager Method:
	 * 
	 * Notify All the customers blocking on Object groupsTurn that they
	 * can enter the store.
	 * 
	 */
	public synchronized void allowGroupIn() {
	peopleInGroup = 0;
	groupCounter++;
	fullStore = true;
	synchronized(groupsTurn) {
		groupsTurn.notifyAll();
	}
}
	
	
	public void rest() {
		try {
            Thread.sleep(1 + (int)(random.nextDouble()* 3000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

}
