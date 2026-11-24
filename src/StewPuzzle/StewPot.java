package StewPuzzle;

// IN3042 Advanced Programming : Concurrency
// Coursework 2025/2026
//
// Submission by
//  Amin Ali

/*
    The FSP model has POT[i] representing the state of the stew pot with i servings left. In the Java implementation, I used a servings
    variable to track this. Whenever a worker gets a serving from the pot, servings is decremented by 1. When the pot is refilled by the cook, servings is reset to N, the pot's capacity.
    The refillRequested boolean variable is used to track whether a worker has requested a refill. When a worker calls askRefill, refillRequested is set to true.
    The cook thread waits until refillRequested is true and servings is 0 before refilling the pot. After refilling, refillRequested is reset to false.
    
    The askRefill method throws an exception if a worker tries to request a refill when there are still servings left in the pot, ensuring that workers only ask for refills when the pot is empty.
    Similarly, the getServing method throws an exception if a worker tries to get a serving when the pot is empty.
*/

public class StewPot {
    private int servings; 
    private boolean refillRequested = false;
    private final int N;   
    
    public StewPot(int N) {
        this.N = N;
        this.servings = N;  
    }
    
    public synchronized void getServing() throws Exception {
        if (servings == 0) {
            throw new Exception("pot empty");
        }
        servings--;
        notifyAll();  
    }

    public synchronized void askRefill() throws Exception {
        if (servings > 0) {
            throw new Exception("pot not empty");
        }
        refillRequested = true;
        notifyAll();  
        
        while (servings == 0) {
            wait();
        }
    }
    
    public synchronized void refill() throws Exception {
        while (servings > 0 || !refillRequested) {
            wait(); 
        }
        servings = N;
        refillRequested = false;
        notifyAll();    
    }
}

