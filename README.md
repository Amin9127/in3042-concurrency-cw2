# IN3042 Concurrency Coursework 2

Java coursework and LTSA models for IN3042 Advanced Programming: Concurrency.

- `src/PacketDispatcher/PacketDispatcher.java`: port-based message passing between threads, throwing an exception if two threads listen on the same port.
- `src/MatrixMultiplier/MatrixMultiplier.java`: multithreaded matrix multiplication (`setNumberOfThreads`, `multiply`).
- `src/StewPuzzle/`: synchronized producer-consumer solution (`Cook.java`, `StewPot.java`, `Worker.java`).
- `ltsa/`: FSP models (`stew.lts`, `stew_test.lts`, `barbershop.lts`, `friendlyLunch.lts`) checked in LTSA.
