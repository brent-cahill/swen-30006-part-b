package automail;

import exceptions.FragileItemBrokenException;
import exceptions.TubeFullException;

import java.util.*;

/**
 * The storage tube carried by the robot.
 */
public class StorageTube {

    public final int MAXIMUM_CAPACITY;
    public Stack<MailItem> tube;

    /**
     * Constructor for the storage tube
     */
    public StorageTube(){ this.tube = new Stack<MailItem>(); this.MAXIMUM_CAPACITY = 4;}

    public StorageTube(int capacity){
        this.tube = new Stack<MailItem>();
        this.MAXIMUM_CAPACITY = capacity;
    }

    /**
     * @return if the storage tube is full
     */
    public boolean isFull(){
        return tube.size() == MAXIMUM_CAPACITY;
    }

    /**
     * @return if the storage tube is empty
     */
    public boolean isEmpty(){
        return tube.isEmpty();
    }

    /**
     * @return the first item in the storage tube (without removing it)
     */
    public MailItem peek() {
    	return tube.peek();
    }

    /**
     * Add an item to the tube
     * @param item The item being added
     * @throws TubeFullException thrown if an item is added which exceeds the capacity
     */
    public void addItem(MailItem item) throws TubeFullException, FragileItemBrokenException {
        if(tube.size() < MAXIMUM_CAPACITY){
        	if (tube.isEmpty()) {
        		tube.add(item);
        	} else if (item.getFragile() || tube.peek().getFragile()) {
        		throw new FragileItemBrokenException();
        	} else {
        		tube.add(item);
        	}
        } else {
            throw new TubeFullException();
        }
    }

    /** @return the size of the tube **/
    public int getSize(){
    	return tube.size();
    }

    /**
     * @return the first item in the storage tube (after removing it)
     */
    public MailItem pop(){
        return tube.pop();
    }

    private List<MailItem> getItems() {
        List<MailItem> items = new ArrayList<>();
        items.addAll(tube);
        return items;
    }

    public void sort(Comparator<MailItem> comparator) {
        List<MailItem> items = getItems();
        tube.clear();
        items.sort((item1, item2) -> {
            // Sort the list in reverse order to the given comparator to account for the storage tube being
            // a stack
            return -comparator.compare(item1, item2);
        });
        for (MailItem item: items) {
            tube.push(item);
        }
    }
}
