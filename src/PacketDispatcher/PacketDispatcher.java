// IN3042 Advanced Programming : Concurrency
// Coursework 2025/2026
//
// Submission by
//  Amin Ali

import java.util.*;


// DO NOT EDIT starts

// This is a class that implements an asynchronous message passing system.
//
// For the purposes of this coursework the message passing system only
// moves packets between local threads but in a real system this could
// use the network.
//
// For the purposes of the coursework the contents of the packets do not matter.
// What matters is how this class co-ordinates the access to the system.
// The communication system has a number of ports.
//
// At most one thread can be listening to a port at a time.
// A second thread attempting to use a port should result in an exception.
// Packets sent to ports which have not thread listening are discarded.
//
// There are two different interfaces that a thread can use to get packets
// from a port : blocking, non-buffered and non-blocking, buffered.
// You need to implement both.

interface PacketDispatcherInterface {

    // Sends the given packet on the given port.
    public void sendPacket(byte outPort, byte [] packet) throws Exception;
    
    /*** Blocking, non-buffered access ***/
    // The calling thread waits for a packet to arrive on the given port.
    // If one arrives then it is returned.
    // If one does not arrive in timeout milliseconds then it returns null.
    // If the port is in use it throws an exception.
    // A time out of 0 should be interpreted as an unlimited timeout.
    public byte [] waitForPacket(byte inPort, long timeout) throws Exception;

    // Send the given packet on outPort and then waits for a response
    // on inPort.  It's return behaviour should be the same as waitForPacket.
    // This should have rendezvous semantics.
    public byte [] sendAndWaitForResponse(byte outPort, byte [] packet, byte inPort, long timeout) throws Exception;
    

    
    /*** Non-blocking, buffered access ***/
    // Registers that a thread is interested in port and future packets
    // recieved on that port should be buffered for access.
    // This port should be regarded as in use and subsequent uses
    // of waitForPacket and sendAndWaitForResponse on this port
    // should throw exceptions.
    public void startBuffering (byte port) throws Exception;

    // Undoes the effects of startBuffering and discards any buffered packets.
    public void endBuffering (byte port) throws Exception;

    // Gets the oldest packet from the buffer or returns null if there
    // are not any packets waiting.  Throws an exeception if the port has
    // not had startBuffering called.
    public byte [] getPacketFromBuffer(byte port) throws Exception;
    
}
// DO NOT EDIT ends



/*
    Explanation:
    The PacketDispatcher class uses synchrnised methods to make message passing thread-safe.
    each port has 3 states Free, Blocking, Buffering. all methods are synchronized so only 1 thread runs at a time.
    this prevents race conditions. on the 3 data structures used to manage the ports.
    PortStates which state each port is in.  PortBuffers, the queued packets. and pending packets waiting to be delivered.

    This design supports 2 types of messsaging synchronous, which blocks the thread until a packet is received.
    asynchronous buffered, which stores packets in a queue for later retrieval. before using a port it checks the state of the port if already in use
    as in not in the free state it throws an exception. which prevents 2 threads using the same port at the same time.

    sendPacket checks the port state. if blocking it adds the packet to pending and notifies waiting threads.
    if buffering it adds the packet to the port's buffer queue. waitForPacket  blocks until a packet is available or timeout expires.
    it uses a while loop to wait for the packet to arrive. if timeout expires it returns null. once a packet is received it removes it from pending and returns it
    sendAndWaitForResponse combines sending a packet and waiting for a response by calling sendPacket and waitForPacket in sequence. 

    The wait and notifyAll methods are used to manage the blocking behavior of threads waiting for packets.
    When a thread calls waitForPacket, it waits until a packet is available or the timeout expires. When a packet is sent to
    a port that is in BLOCKING state, notifyAll() is called to wake up any waiting threads. 
    This prevents deadlocks and ensures that threads are properly notified when packets arrive.

*/


// Complete this class!
public class PacketDispatcher implements PacketDispatcherInterface {
    
    enum PortState {
        FREE,
        BLOCKING,
        BUFFERING
    }
    
    Map<Byte, PortState> portStates = new HashMap<>();
    Map<Byte, Queue<byte[]>> portBuffers = new HashMap<>();
    Map<Byte, byte[]> pending = new HashMap<>();



    public synchronized void sendPacket(byte outPort, byte [] packet) throws Exception {
        PortState state = portStates.getOrDefault(outPort, PortState.FREE);

        if (state == PortState.BLOCKING) {
            pending.put(outPort, packet);
            notifyAll();
        } else if (state == PortState.BUFFERING) {
            portBuffers.get(outPort).add(packet);
        }
    }

    public synchronized byte [] waitForPacket(byte inPort, long timeout) throws Exception {
        if (portStates.getOrDefault(inPort, PortState.FREE) != PortState.FREE) {
            throw new Exception("Port in use");
        }

        portStates.put(inPort, PortState.BLOCKING);

        // Wait for packet
        long startTime = System.currentTimeMillis();
        while (!pending.containsKey(inPort)) {
            if (timeout > 0 && System.currentTimeMillis() - startTime >= timeout) {
                portStates.put(inPort, PortState.FREE);
                return null; 
            }
            wait(timeout);
        }

        byte[] packet = pending.remove(inPort);
        portStates.put(inPort, PortState.FREE);
        return packet;
    }

    public synchronized byte [] sendAndWaitForResponse(byte outPort, byte [] packet, byte inPort, long timeout) throws Exception {
        sendPacket(outPort, packet);
        return waitForPacket(inPort, timeout);
    }


    
    public synchronized void startBuffering (byte port) throws Exception {
        if (portStates.getOrDefault(port, PortState.FREE) != PortState.FREE) {
            throw new Exception("Port in use");
        }
        portStates.put(port, PortState.BUFFERING);
        portBuffers.put(port, new LinkedList<>());

    }

    public synchronized void endBuffering (byte port) throws Exception {
        if (portStates.getOrDefault(port, PortState.FREE) != PortState.BUFFERING) {
            throw new Exception("Port not buffering");
        }
        portStates.put(port, PortState.FREE);
        portBuffers.remove(port);

    }

    public synchronized byte [] getPacketFromBuffer(byte port) throws Exception {
        if (!portBuffers.containsKey(port)) {
            throw new Exception("Port not buffering");
        }
        Queue<byte[]> buffer = portBuffers.get(port);
        return buffer.isEmpty() ? null : buffer.poll();

    }
 }

