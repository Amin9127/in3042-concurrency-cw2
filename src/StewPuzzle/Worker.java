package StewPuzzle;

public class Worker implements Runnable {
    private StewPot pot;
    private int id;
    private int eatingCycles;
    
    public Worker(StewPot pot, int id, int eatingCycles) {
        this.pot = pot;
        this.id = id;
        this.eatingCycles = eatingCycles;
    }
    
    @Override
    public void run() {
        try {
            for (int cycle = 0; cycle < eatingCycles; cycle++) {
                try {
                    pot.getServing();
                    System.out.println("w " + id + " got serving (cycle " + (cycle + 1) + ")");
                } catch (Exception e) {
                    System.out.println("w " + id + " ask refill (cycle " + (cycle + 1) + ")");
                    pot.askRefill();
                    System.out.println("w " + id + " refilled, eating (cycle " + (cycle + 1) + ")");
                }
                
                // Eat
                eat();
            }
        } catch (Exception e) {
            System.err.println("w " + id + " error: " + e.getMessage());
        }
    }
    
    private void eat() {
        try {
            Thread.sleep(10); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
