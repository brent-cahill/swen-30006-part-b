package automail;

import strategies.IMailPool;

public class StandardRobot extends Robot {

    public StandardRobot(IMailDelivery delivery, IMailPool mailPool){
        super(delivery, mailPool, true);
    }
}
