package automail;

import strategies.IMailPool;

public class WeakRobot extends Robot {

    private final int MAX_ITEM_WEIGHT = 2000;

    public WeakRobot(IMailDelivery delivery, IMailPool mailPool) {
        super(delivery, mailPool, false);
    }

    @Override
    public boolean canTakeMailItem(MailItem mailItem) {
        return !getTube().isFull() && !mailItem.fragile && mailItem.weight < MAX_ITEM_WEIGHT;
    }
}
