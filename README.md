

## Quick Start
- Clone the repo by `git clone https://github.com/Liuyubao/TCP-Go-back-N-RDT`.
- Run `make` to compile the code.
- Execute `./rdt_sim 1000 0.1 100 0 0 0 0`.



# 1.Overview
For this project, I implemented the sending and receiving side of a reliable data transport (RDT) protocol. It achieves error-free, loss-free, and in-order data delivery on top of a link medium that can lose, reorder, and corrupt packets. The implementation follows sliding window protocol (Go-Back-N protocol).




# 2.Design explanation
## 2.1Implemented Routines
I modified two files (MyRdtSender.java and MyRdtReceiver.java) by enhancing three methods for handling requests to send data from the upper layer, and receipt of data from the lower layer for both sender and receiver. The routines are detailed below. Such routines in real life would be part of the operating system.
 * (Sender) public void receiveFromUpperLayer(byte[] message);
 * (Sender) public void receiveFromLowerLayer(Packet packet);
 * (Sender) public void onTimeout();
 * (Receiver) public void receiveFromLowerLayer(Packet packet);

## 2.2Called Routines
The routines I call (implemented by the simulated environment) are detailed below. Such routines in real life would also be part of the operating system.
* (Sender) public void startTimer(double timeout); - start the sender timer with a specified timeout (in seconds). This timer is canceled when (Sender) public void stopTimer() is called or a new (Sender) public void startTimer() is called before the current timer expires.
* (Sender) onTimeout() will be called when the timer expires.
* (Sender) public void stopTimer(); - stop the sender timer.
* (Sender) public boolean isTimerSet(); - check whether the sender timer is being set, return true if the timer is set, return false otherwise.
* (Sender) public double getSimulationTime(); - return the local simulation time, which may be useful in timer management. You should not assume that the time is synchronized between the sender and receiver side.
* (Sender) public void sendToLowerLayer(Packet packet); - pass a packet to the lower layer at the sender for delivery.
* (Receiver) public void sendToLowerLayer(Packet packet); - pass a packet to the lower layer at the receiver for delivery.
* void (Receiver) sendToUpperLayer(byte[] message); - deliver a message to the upper layer at the receiver.

## 2.3Parameters
* sim_time - total simulation time, the simulation will end at this time (in seconds).
* mean_msg_arrivalint - average intervals between consecutive messages passed from the upper layer at the sender (in seconds). The actual interval varies between zero and twice the average.
* mean_msg_size - average size of messages (in bytes). The actual size varies between one and twice the average.
* outoforder_rate - the probability that a packet is not delivered with the normal latency - 100ms. A value of 0.1 means that one in ten packets are not delivered with the normal latency. When this occurs, the latency varies between zero and 200ms.
* loss_rate - packet loss probability: a value of 0.1 means that one in ten packets are lost on average. corrupt_rate - packet corruption probability: a value of 0.1 means that one in ten packets (excluding those lost) are corrupted on average. Note that any part of a packet can be corrupted.
* tracing_level - levels of trace printouts (higher level always prints out more information): a tracing level of 0 turns off all traces, a tracing level of 1 turns on regular traces, a tracing level of 2 prints out the delivered message. Most likely you want to use level 1 for debugging and use level 0 for final testing.

## 2.4Packet format
In my implementation, the packet format is laid out as the following:

|<-    1 byte    ->|<-    1 byte    ->|<-    2 byte    ->|<-             the rest               ->|
|   payload size   |   seq number    |     checksum     |<-             payload              ->|

The first byte of each packet indicates the size of the payload.
The second byte of each packet indicates the sequence number of the packet.
The third and fourth byte of each packet indicates the checksum.
The rest bytes of each packet indicate the actual payload.
## 2.5My RDT Sender
I used one window to store the packets that are sent but not acknowledged. I used one buffer to store the packets that are waiting for the window available. When there are ACKs received from the lower layer, I correspondingly free the spaces in window and acquire more packets from buffer if available. The buffer is used to temparorily store packets from the upper layer. If the window is already full with no position. All the message will store in the buffer. I used the internet checksum to ensure the accuracy of the datas.


## 2.6My RDT Receiver

I used a variable to indicate the expected sequence number receiver expected to receive. When the packet received has the different sequence number from the expected number, it will not accept it. Otherwise, send back the ACK to the lower layer.
# 3.Implementation details
## 3.1 Variable and Parameters
Here I used Queue to implement the buffer and window. Set the size of Window as 10 and Timeout as 0.3. I used lastAck to keep track of acknowledged packets in window.

private Queue<Packet> buffer; // only unsent data
private Queue<Packet> window; // sent but unAcked data

public static final int WINDOW_SIZE = 10;
private static final double TIMEOUT = 0.3;
public static final int SEQUENCE_SIZE = 128;    // sequence number has 1 byte equals to 8 bits, so total is 2 pow 7 = 128
public static int headerSize = 4;
private int seq;
private int lastAck;


## 3.2 Sender: ReceiveFromUpperLayer
When the message’s length is bigger than the maxPayload, which equals to 60, I fragment it to lots of packets and not only put it to window queue, but also sent it out to lower layer.
Also, I acquire packets from buffer if it can.
```java
public void receiveFromUpperLayer(byte[] message) { // make packet
    int msgPointer = 0;
    int maxPayload = RDT_PKTSIZE - headerSize;
    while (message.length-msgPointer > maxPayload) {
        msgPointer = createPacket(maxPayload, message, msgPointer);
    }
    if (message.length > msgPointer)
        createPacket(message.length-msgPointer, message, msgPointer);

    sentFromBufferToWindowIfCan();
}
```

## 3.3 Sender: ReceiveFromLowerLayer
The only packet that sent back from lower layer is the ACK packet. I check if it is correct using the checksum and the sequence number inside. If it is correct, I poll all the packets till the acknowledged one in window. Also, I acquire packets from buffer if it can.

```java
public void receiveFromLowerLayer(Packet packet) {
    int ack = packet.data[1];
    long computedChecksum = checkSum(packet.data);
    long rcvChecksum = (((long)((packet.data[2]) << 8) & 0xFF00) | ((long)packet.data[3] & 0xFF));
    int gap = (ack - lastAck) % (SEQUENCE_SIZE - 1);
    if (gap < 0) gap += (SEQUENCE_SIZE - 1);

    if (ack > lastAck && (lastAck + SEQUENCE_SIZE - 1 - ack <= WINDOW_SIZE)) return;
    if ((gap != 0) && (gap <= WINDOW_SIZE) && (computedChecksum == rcvChecksum)){
        for (int i = 0; i< gap && !window.isEmpty() && window.peek().data[1] != lastAck; i++) {
            window.poll();
        }
        if(!window.isEmpty() && window.peek().data[1] == lastAck){
            window.poll();
        }
        lastAck = ack;
    }

    sentFromBufferToWindowIfCan();

}
```


3.4 SentFromBufferToWindowIfCan
Check if current window is available for more packets and if still some packets in buffer, if yes, sent it from buffer to window, and sent it out to lower layer as well. In addition, start the timer.
```java
private void sentFromBufferToWindowIfCan(){
    /* if still room for window, try to get packets from buffer*/
    if (window.size() < WINDOW_SIZE){
        for (int i = 0; i < WINDOW_SIZE -window.size() && !buffer.isEmpty(); i++) {
            Packet pkt = buffer.poll();
            window.offer(pkt); // send to window
            Packet pktCopy = new Packet();
            System.arraycopy(pkt.data, 0, pktCopy.data, 0, 64);
            sendToLowerLayer(pktCopy);
            startTimer(TIMEOUT);
        }
    }

}
```


3.5 OnTimeout
When time is out, resent all the packets in the window and restart the timer.
```java
public void onTimeout() {
    if (!window.isEmpty()) {
        for (Packet pkt : window) {
            Packet pktCopy = new Packet();
            System.arraycopy(pkt.data, 0, pktCopy.data, 0, 64);
            sendToLowerLayer(pktCopy);
            startTimer(TIMEOUT);
        }
    }
}
```

3.6 CreatePacket
Given the payloadsize and message and the current pointer on message, I move the message pointer according to the size. When consist a packet, I send it out to buffer.
```java
private int createPacket(int payloadSize, byte[] message, int msgPointer) {
    Packet pkt = new Packet();
    pkt.data[0] = (byte) payloadSize;
    pkt.data[1] = (byte) seq;
    System.arraycopy(message, msgPointer, pkt.data, headerSize, pkt.data[0]);
    long checksum = checkSum(pkt.data);
    pkt.data[3] = (byte) (checksum & 0xFF);
    pkt.data[2] = (byte) ((checksum >> 8) & 0xFF);
    buffer.offer(pkt);
    msgPointer += payloadSize;
    seq = (seq + 1) % (SEQUENCE_SIZE - 1);
    return msgPointer;
}
```

3.7 CheckSum
According to the internet checksum algorithm, I compute the checksum.
```java
public static final long checkSum(byte[] pkt) {
    long checksum = (((pkt[0] << 8) & 0xFF00) | ((pkt[1]) & 0xFF));  // compute for the payloadsize and seqnum
    if ((checksum & 0xFFFF0000) > 0) {
        checksum = (checksum & 0xFFFF) + 1;
    }
    int start = headerSize ; // skip the first 4 bytes including the checksum 2 bytes

    while (start < RDT_PKTSIZE) {
        checksum += (((pkt[start] << 8) & 0xFF00) | ((pkt[start + 1]) & 0xFF));
        if ((checksum & 0xFFFF0000) > 0) {
            checksum = (checksum & 0xFFFF) + 1;
        }
        start += 2;
    }
    return ~checksum & 0xFFFF;
}
```

3.8  Receiver: ReceiveFromLowerLayer
On the receiver side, I not only check the checksum, but also check if the payloadsize is valid. If both yes, I create a new message and send it to the upper layer. At the same time, I send the ACK packet back using the original packet sequence number.
```java
public void receiveFromLowerLayer(Packet packet) {
    int rcvSeq = packet.data[1];
    long rcvChecksum = (((long)((packet.data[2]) << 8) & 0xFF00) | ((long)packet.data[3] & 0xFF));
    long computedChecksum = MyRdtSender.checkSum(packet.data);

    if (computedChecksum == rcvChecksum && rcvSeq == expectedSeq) {
        int payloadsize = packet.data[0];
        if (payloadsize < 0 || payloadsize > RDT_PKTSIZE - MyRdtSender.headerSize)
            return ;
        byte[] message = new byte[packet.data[0]];
        System.arraycopy(packet.data, 4, message, 0, packet.data[0]);
        sendToUpperLayer(message);
        sendToLowerLayer(packet);
        expectedSeq = (expectedSeq + 1) % (MyRdtSender.SEQUENCE_SIZE - 1);
    }
}
```



# 4.Passed on different arguments tests
## 4.1 Clean data
./rdt_sim 1000 0.1 100 0 0 0 0



## 4.2 Standard Test

./rdt_sim 1000 0.1 100 0.02 0.02 0.02 0



## 4.3 Harder Test: More message sent
sim_tim: 1000 -> 10000
./rdt_sim 1000 0.1 100 0.02 0.02 0.02 0




## 4.4 Harder Test: Improve wrong rate
Out_of_Order_rate: 0.02 -> 0.1
Loss_rate: 0.02 -> 0.1
Corrupt_rate: 0.02 -> 0.1
sim_tim: 1000
./rdt_sim 1000 0.1 100 0.1 0.1 0.1 0



## 4.5 Harder Test: Improve wrong rate v2
Out_of_Order_rate: 0.02 -> 0.2
Loss_rate: 0.02 -> 0.2
Corrupt_rate: 0.02 -> 0.2
sim_tim: 100
./rdt_sim 100 0.1 100 0.2 0.2 0.2 0


## 4.6 Harder Test: Extreme out of order 90%
Out_of_Order_rate: 0.02 -> 0.9
Loss_rate: 0.02 -> 0.02
Corrupt_rate: 0.02 -> 0.02
sim_tim: 1000
./rdt_sim 1000 0.1 100 0.9 0.02 0.02 0



## 4.7 Harder Test: Loss rate 30%
Out_of_Order_rate: 0.02 -> 0.02
Loss_rate: 0.02 -> 0.3
Corrupt_rate: 0.02 -> 0.02
sim_tim: 1000

./rdt_sim 1000 0.1 100 0.02 0.3 0.02 0


## 4.8 Harder Test: Corrupted rate 30%
Out_of_Order_rate: 0.02 -> 0.02
Loss_rate: 0.02 -> 0.02
Corrupt_rate: 0.02 -> 0.3
sim_tim: 1000


The RDT layer is implemented in `MyRdtSender.java` and `MyRdtReceiver`, The current implementation assumes there is no packet loss, corruption, or reordering in the underlying link medium. You will need to enhance the implementation to deal with all these situations. In general, you are not supposed to change sourcecode under the folder `sim`.

For debugging purpose, you may want to add more print statements in the simulator. If you do so, definitely remember to test your program with the original simulator before turn-in.


## Testing
The provided simulation files should compile and run. However, they only work correctly when there is no packet loss, corruption, or reordering in the underlying link medium. Run `rdt_sim 1000 0.1 100 0 0 0 0` to see what happens. In summary, the following are a few test cases you may want to use.

- `rdt_sim 1000 0.1 100 0 0 0 0` - there is no packet loss, corruption, or reordering in the underlying link medium.
- `rdt_sim 1000 0.1 100 0.02 0 0 0` - there is no packet loss or corruption, but there is reordering in the underlying link medium.
- `rdt_sim 1000 0.1 100 0 0.02 0 0` - there is no packet corruption or reordering, but there is packet loss in the underlying link medium.
- `rdt_sim 1000 0.1 100 0 0 0.02 0` - there is no packet loss or reordering, but there is packet corruption in the underlying link medium.
- `rdt_sim 1000 0.1 100 0.02 0.02 0.02 0` - there could be packet loss, corruption, or reordering in the underlying link medium.


## Authors
Go-back-N implemented by Yubao Liu
Using the skeleton provided by Jiupeng Zhang
