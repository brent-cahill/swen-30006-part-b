package automail;

import exceptions.FragileItemBrokenException;
import strategies.IMailPool;

public class CarefulRobot extends Robot {
    private int fragileDeliveryCounter;

    public CarefulRobot(IMailDelivery delivery, IMailPool mailPool, boolean strong){
        super(delivery, mailPool, true);
        tube = new StorageTube(3);
        setPace(2);
        resetCooldown();
    }

    @Override
    protected void moveTowards(int destination){
        if (move_cooldown-- == 0) {
            if (current_floor < destination) {
                current_floor++;
            } else {
                current_floor--;
            }
            resetCooldown();
        }
    }

    @Override
    public boolean canTakeMailItem(MailItem mailItem) {
        return !getTube().isFull();
    }
}
