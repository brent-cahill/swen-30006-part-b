package strategies;

import automail.*;

import java.util.List;

public class Automail {
	      
    public Robot[] robot;
    public IMailPool mailPool;
    
    public Automail(IMailPool mailPool, IMailDelivery delivery, List<Robot.RobotType> robotTypes) {
    	// Swap between simple provided strategies and your strategies here
    	    	
    	/** Initialize the MailPool */
    	
    	this.mailPool = mailPool;
    	
        /** Initialize the RobotAction */
    	boolean weak = false;  // Can't handle more than 2000 grams
    	boolean strong = true; // Can handle any weight that arrives at the building
    	
    	/** Initialize robots */
    	robot = new Robot[robotTypes.size()];
    	for (int r = 0; r < robotTypes.size(); r++) {
    		switch (robotTypes.get(r)) {
				case Big:
					robot[r] = new StrongRobot(delivery, mailPool);
					break;
				case Careful:
					robot[r] = new CarefulRobot(delivery, mailPool, false);
					break;
				case Standard:
					robot[r] = new StandardRobot(delivery, mailPool);
					break;
				case Weak:
					robot[r] = new WeakRobot(delivery, mailPool);
					break;
			}
		}
    }
    
}
