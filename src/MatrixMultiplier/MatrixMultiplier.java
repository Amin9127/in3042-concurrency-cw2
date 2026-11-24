package matrix;
// IN3042 Advanced Programming : Concurrency
// Coursework 2025/2026
//
// Submission by
//  Amin Ali

// DO NOT EDIT starts

// This is a class that multiplies two matrices.
// It uses a user-specified number of threads to increase the performance.
// Matrices do not have to be square and can be any size.
// The number of threads should not be limited but for any particular
// computer there will be a maximum number of threads that gives the
// best performance.

interface MatrixMultiplierInterface {
    // This must be called first.
    // It should throw an exception if the threadCount is negative.
    public void setNumberOfThreads(int threadCount) throws Exception;

    // Uses the specified number of threads to compute the matrix
    // multiplication of A and B.
    // If should throw an exception if the matrixes are not of compatible sizes.
    public int[][] multiply(int [][] A, int [][] B) throws Exception;
}
// DO NOT EDIT ends

/*

    Explanation:
    this class divides the matrix multiplication work among multiple threads. by partitioning the result matrix rows among the threads.
    each thread computes a a contiguous block of rows in the result matrix. so if there are 8 rows and 4 threads, each thread computes 2 rows. 
    this ensures that no two threads write to the same row in the result matrix, removing the risk of data races.

    this design avoids concurrency issues  as each threads writes to completely separate rows of the result matrix.
    so multiple reads and writes to different rows can happen without interference. the main thread waits for all worker threads to complete using join before returning the final result.
    this ensures that the result matrix is fully computed before it is accessed by any other thread.
    no synchronization mechanisms are needed within the worker threads since they operate on separate parts of the result matrix.

    the multiply method first validates the input matrices to ensure they are compatible for multiplication.
    it then creates the result matrix and calculates how many rows each thread should compute.
    it creates and starts the worker threads, each assigned a specific range of rows to compute.
    finally, it waits for all threads to finish using join before returning the completed result matrix.

*/


// Complete this class!
public class MatrixMultiplier implements MatrixMultiplierInterface {
    
    private int threadCount;
    
    public void setNumberOfThreads(int threadCount) throws Exception {
        if (threadCount < 0) {
            throw new Exception("Thread negative");
        }
        this.threadCount = threadCount;
    }

    public int[][] multiply(int [][] A, int [][] B) throws Exception {
        // Validate input 
        if (A == null || B == null) {
            throw new Exception("Matrix null");
        }
        if (A.length == 0 || B.length == 0) {
            throw new Exception("Matrix empty");
        }
        
        int m = A.length;      // rows in A
        int n = A[0].length;   // cols in A
        int p = B[0].length;   // cols in B
        
        // Check compatibility
        if (n != B.length) {
            throw new Exception("Matrix incompatible");
        }
        



        int[][] C = new int[m][p];
        int actualThreads=1;
        
        if (threadCount != 0) {
            actualThreads = threadCount;
        }

        
        // Calculate rows per thread
        int rowsPerThread = m / actualThreads;
        int remainingRows = m % actualThreads;
        
        Thread[] threads = new Thread[actualThreads];
        int startRow = 0;
        
        for (int t = 0; t < actualThreads; t++) {
            // Distribute remaining rows 
            int rowsForThisThread = rowsPerThread + (t < remainingRows ? 1 : 0);
            int endRow = startRow + rowsForThisThread;
            
            // Create worker for this row range
            threads[t] = new Thread(new Worker(A, B, C, startRow, endRow));
            threads[t].start();
            
            startRow = endRow;
        }
        
        // Wait for threads to finish
        for (int t = 0; t < actualThreads; t++) {
            threads[t].join();
        }
        
        return C;
    }
    
     private class Worker implements Runnable {
        private final int[][] A;
        private final int[][] B;
        private final int[][] C;
        private final int startRow;
        private final int endRow;
        
        public Worker(int[][] A, int[][] B, int[][] C, int startRow, int endRow) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.startRow = startRow;
            this.endRow = endRow;
        }
        
        @Override
        public void run() {
            int n = A[0].length; // cols in A = rows in B
            int p = B[0].length; // cols in B
            
            // Compute assigned rows
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < p; j++) {
                    int sum = 0;
                    for (int k = 0; k < n; k++) {
                        sum += A[i][k] * B[k][j];
                    }
                    C[i][j] = sum;
                }
            }
        }
    }
}


