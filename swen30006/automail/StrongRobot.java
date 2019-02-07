package automail;

import strategies.IMailPool;

// Strong Robot is also referred to as Big
public class StrongRobot extends Robot {

    public StrongRobot(IMailDelivery delivery, IMailPool mailPool){
        super(delivery, mailPool, true);
        this.tube = new StorageTube(6);
    }
}
