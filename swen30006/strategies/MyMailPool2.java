package strategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import automail.*;
import exceptions.FragileItemBrokenException;
import exceptions.TubeFullException;

/**
 * Basic greedy approach to sorting the mail. <br>
 * As mail arrives it is placed in a queue structure depending on priority, then arrival time. <br>
 * Any available robots are loaded one at a time with as many items as possible to deliver in priority order.
 * Careful robots are given only one fragile item if available, or loaded in the same way as other robots.<p>
 *
 * <b>Possible Improvements</b><br>
 * Consider arrival time as a factor when sorting the queue<br>
 * Distribute items between the robots rather than loading them to capacity one at a time <br>
 * Monitor when the robots are expected to return to plan deliveries. <br>
 * Give careful robots lowest priority for jobs
 *
 * @author Nicholas Edsall 934422
 *
 */
public class MyMailPool2 implements IMailPool {

    private ArrayList<MailItem> mailItems;
    private ArrayList<MailItem> fragileItems;

    // index in the mail queue where non-priority
    // items begin (or would if there were any)
    private int standardIndex;
    private int fragileIndex;


    private static final int HEAVY_WEIGHT = 2000;
    private ArrayList<Robot> robots;

    public MyMailPool2() {
        // Start empty
        mailItems = new ArrayList<MailItem>();
        fragileItems = new ArrayList<MailItem>();
        standardIndex = 0;
        fragileIndex = 0;
        robots = new ArrayList<Robot>();
    }

    @Override
    public void addToPool(MailItem mailItem) {

        ArrayList<MailItem> pool = mailItem.getFragile() ? fragileItems : mailItems;
        int regInx = mailItem.getFragile() ? fragileIndex : standardIndex;

        // If this is a priority item we need to determine where to place it in the queue
        // Otherwise it is a non-priority item, send it to the back of the queue
        if (mailItem instanceof PriorityMailItem) {
            PriorityMailItem it = (PriorityMailItem) mailItem;
            int prInx = 0; // the index to insert this item

            // search for an item with lower priority than this
            for (int i = 0; i < regInx; i++) {
                if (it.getPriorityLevel() > ((PriorityMailItem) pool
                        .get(i)).getPriorityLevel()) {
                    break;
                } else
                    prInx++;
            }

            pool.add(prInx, it);
            // there is now one more priority item before the standard items
            if (it.getFragile())
                fragileIndex++;
            else
                standardIndex++;

        } else {
            pool.add(mailItem);
        }
    }

    @Override
    public void step() {
        
        // If there is mail to deliver, load up the available robots
        if (!mailItems.isEmpty() || !fragileItems.isEmpty()) {
            // Robots are deregistered after this step() so there should be no issues iterating like this
            for (Robot robot : robots) {

                fillStorageTube(robot);

                // Does this robot have something to deliver?
                if (!robot.getTube().isEmpty()) {
                    robot.getTube().sort(Comparator.comparingInt(MailItem::getDestFloor));
                    robot.dispatch();
                }
            }
        }
    }


    /**
     * *
     * Fills up a robot's storage tube as much as possible from the mail queue
     *
     * @param robot
     */
    private void fillStorageTube(Robot robot) {

        StorageTube tube = robot.getTube();
        while (!tube.isFull()) {
            // Try to find an item to put in the tube
            MailItem item = findItem(robot);

            if (item != null) {
                try {
                    tube.addItem(item);

                    if (item.getFragile()){
                        if (item instanceof PriorityMailItem)
                            fragileIndex--; // there is now one less priority
                        // item
                        fragileItems.remove(item);
                        return; // Careful robots can only handle one fragile item at a time
                        // They are also slower so it is likely best to leave other items
                    }
                    else {
                        if (item instanceof PriorityMailItem)
                            standardIndex--; // there is now one less priority
                        // item
                        mailItems.remove(item);
                    }
                } catch (TubeFullException e) {
                    e.printStackTrace();
                } catch (FragileItemBrokenException e){
                    e.printStackTrace();
                }

            } else // this robot cannot deliver anything from the current queue
                return;
        }
    }

    /**
     * Attempts to find a MailItem for a robot to deliver
     *
     * @param robot
     *            to be loaded
     * @return the item found (or null if no suitable item found)
     */
    private MailItem findItem(Robot robot) {
        StorageTube tube = robot.getTube();

        // Try to find an item this robot can take

        if (!tube.isFull()) {
            if (robot instanceof CarefulRobot) {
                // Just return the highest priority fragile item
                // In this version the careful robot can handle any weight
                for (MailItem item : fragileItems) {
                    return item;
                }
            }
            for (MailItem item : mailItems) {
                if (robot.canTakeMailItem(item)) {
                    // This robot is able to handle the item.
                    // Add to the tube and remove it from the mailpool
                    return item;
                }
            }
        }

        return null;

    }

    @Override
    public void registerWaiting(Robot robot) {
        robots.add(robot);
    }

    @Override
    public void deregisterWaiting(Robot robot) {
        robots.remove(robot);
    }

}