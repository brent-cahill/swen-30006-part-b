package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import exceptions.FragileItemBrokenException;
import strategies.IMailPool;
import java.util.Map;
import java.util.TreeMap;

/**
 * The robot delivers mail!
 */
public class Robot {

    public enum RobotType { Big, Careful, Standard, Weak };
	StorageTube tube;
    IMailDelivery delivery;
    private final String id;
    /** Possible states the robot can be in */
    public enum RobotState { DELIVERING, WAITING, RETURNING }
    public RobotState current_state;
    protected int current_floor;
    protected int destination_floor;
    protected IMailPool mailPool;
    protected boolean receivedDispatch;
    protected boolean strong;
    protected int pace;
    protected int move_cooldown;

    protected MailItem deliveryItem;

    // Counters for deliveries this Robot has made
    // deliveryCounter includes fragileDeliveryCounter
    private int deliveryCounter;
    private int fragileDeliveryCounter;


    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     * @param strong is whether the robot can carry heavy items
     */
    public Robot(IMailDelivery delivery, IMailPool mailPool, boolean strong){
    	id = "R" + hashCode();
        // current_state = RobotState.WAITING;
    	current_state = RobotState.RETURNING;
        current_floor = Building.MAILROOM_LOCATION;
        tube = new StorageTube();
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.strong = strong;
        this.deliveryCounter = 0;
        this.fragileDeliveryCounter = 0;
        this.pace = 1;
        resetCooldown();
    }
    
    public void dispatch() {
    	receivedDispatch = true;
    }

    public boolean isStrong() {
    	return strong;
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void step() throws ExcessiveDeliveryException, ItemTooHeavyException, FragileItemBrokenException {
    	switch(current_state) {
    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
    		case RETURNING:
    			/** If its current position is at the mailroom, then the robot should change state */
                if(current_floor == Building.MAILROOM_LOCATION){
                	while(!tube.isEmpty()) {
                		MailItem mailItem = tube.pop();
                		mailPool.addToPool(mailItem);
                        System.out.printf("T: %3d > old addToPool [%s]%n", Clock.Time(), mailItem.toString());
                	}
        			/** Tell the sorter the robot is ready */
        			mailPool.registerWaiting(this);
                	changeState(RobotState.WAITING);
                } else {
                	/** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.MAILROOM_LOCATION);
                	break;
                }
    		case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!tube.isEmpty() && receivedDispatch){
                	receivedDispatch = false;
                	deliveryCounter = 0; // reset delivery counter
                    fragileDeliveryCounter = 0;
        			setRoute();
        			mailPool.deregisterWaiting(this);
                	changeState(RobotState.DELIVERING);
                }
                break;
    		case DELIVERING:
    			if(current_floor == destination_floor){ // If already here drop off either way
                    /** Delivery complete, report this to the simulator! */
                    delivery.deliver(deliveryItem);
                    deliveryCounter++;
                    if (deliveryItem.getFragile())
                        fragileDeliveryCounter++;
                    if(deliveryCounter > getTube().MAXIMUM_CAPACITY){  // Implies a simulation bug
                    	throw new ExcessiveDeliveryException();
                    }
                    if(this instanceof CarefulRobot && fragileDeliveryCounter > 1){
                        throw new FragileItemBrokenException();
                    }

                    /** Check if want to return, i.e. if there are no more items in the tube*/
                    if(tube.isEmpty()){
                    	changeState(RobotState.RETURNING);
                    }
                    else{
                        /** If there are more items, set the robot's route to the location to deliver the item */
                        setRoute();
                        changeState(RobotState.DELIVERING);
                    }
    			} else {
	        		/** The robot is not at the destination yet, move towards it! */
	                moveTowards(destination_floor);
    			}
                break;
    	}
    }

    /**
     * Check if the robot can take the mail item
     * @param mailItem Mail item to check
     * @return Whether the robot can take the mail item
     */
    public boolean canTakeMailItem(MailItem mailItem) {
        return !getTube().isFull() && !mailItem.fragile;
    }

    /**
     * Sets the route for the robot
     */
    private void setRoute() throws ItemTooHeavyException{
        /** Pop the item from the StorageUnit */
        deliveryItem = tube.pop();
        if (!strong && deliveryItem.weight > 2000) throw new ItemTooHeavyException();
        /** Set the destination floor */
        destination_floor = deliveryItem.getDestFloor();
    }

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    protected void moveTowards(int destination) throws FragileItemBrokenException {
        if (move_cooldown-- == 0) {
            if (deliveryItem != null && deliveryItem.getFragile() || !tube.isEmpty() && tube.peek().getFragile())
                throw new FragileItemBrokenException();
            if (current_floor < destination) {
                current_floor++;
            } else {
                current_floor--;
            }
            resetCooldown();
        }
    }

    private String getIdTube() {
    	return String.format("%s(%1d/%1d)", id, tube.getSize(), tube.MAXIMUM_CAPACITY);
    }

    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    private void changeState(RobotState nextState){
    	if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
    	}
    	current_state = nextState;
    	if(nextState == RobotState.DELIVERING){
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
    	}
    }

    public int getPace() { return this.pace; }
    public void setPace(int pace){ this.pace = pace; }
    public void resetCooldown() {this.move_cooldown = this.pace - 1;}

	public StorageTube getTube() {
		return tube;
	}

	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}
}
