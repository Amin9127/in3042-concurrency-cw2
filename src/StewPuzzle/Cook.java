package StewPuzzle;

public class Cook implements Runnable {
    private StewPot pot;
    
    public Cook(StewPot pot) {
        this.pot = pot;
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                pot.refill();
                System.out.println("pot refilled");
                Thread.sleep(5); 
            }
        } catch (Exception e) {
            System.err.println("Cook error: " + e.getMessage());
        }
    }
}
