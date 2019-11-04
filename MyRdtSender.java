import sim.Packet;
import sim.RdtSender;

import static sim.Packet.RDT_PKTSIZE;
import java.util.LinkedList;
import java.util.Queue;

/**
 * My reliable data transfer sender
 * <pre>
 *     This implementation assumes there is no packet loss, corruption,
 *     or reordering. You will need to enhance it to deal with all these
 *     situations. In this implementation, the packet format is laid out
 *     as the following:
 *
 *     |<-  1 byte  ->|<-  1 byte  ->|<-  2 byte  ->|<-             the rest            ->|
 *     | payload size |  seq number  |   checksum   |<-             payload             ->|
 *
 *     The first byte of each packet indicates the size of the payload
 *     (excluding this single-byte header)
 *
 *     Routines that you can call at the sender:
 *     {@link #getSimulationTime()}         get simulation time (in seconds)
 *     {@link #startTimer(double)}          set a specified timeout (in seconds)
 *     {@link #stopTimer()}                 stop the sender timer
 *     {@link #isTimerSet()}                check whether the sender timer is being set
 *     {@link #sendToLowerLayer(Packet)}    pass a packet to the lower layer at the sender
 * </pre>
 *
 * @author Jiupeng Zhang
 * @author Kai Shen
 * @since 10/04/2019
 */
public class MyRdtSender extends RdtSender {

    /**
     * In our implementation, the packet format is laid out as the following:
     *
     *      |<-  1 byte  ->|<-  1 byte  ->|<-  2 byte  ->|<-             the rest            ->|
     *      | payload size |  seq number  |   checksum   |<-             payload             ->|
     *
     */

    private Queue<Packet> buffer; // only unsent data
    private Queue<Packet> window; // sent but unAcked data

    public static final int WINDOW_SIZE = 15;
    private static final double TIMEOUT = 0.3;
    public static final int SEQUENCE_SIZE = 128;    // sequence number has 1 byte equals to 8 bits, so total is 2 pow 7 = 128
    public static int headerSize = 4;
    private int seq;
    private int lastAck;


    public MyRdtSender() {
        seq = 0;
        lastAck = -1;
        buffer = new LinkedList<>();
        window = new LinkedList<>();
    }

    /**
     * Event handler, called when a message is passed from the upper
     * layer at the sender
     */
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


    /**
     * Event handler, called when a packet is passed from the lower
     * layer at the sender
     */
    public void receiveFromLowerLayer(Packet packet) {
        int ack = packet.data[1];   // ack seq number
        long computedChecksum = checkSum(packet.data);
        long rcvChecksum = (((long)((packet.data[2]) << 8) & 0xFF00) | ((long)packet.data[3] & 0xFF));
        int gap = (ack - lastAck) % (SEQUENCE_SIZE - 1);
        if (gap < 0) gap += (SEQUENCE_SIZE - 1);

        if (ack > lastAck && (lastAck + SEQUENCE_SIZE - 1 - ack <= WINDOW_SIZE)) return;
        if ((gap != 0) && (gap <= WINDOW_SIZE) && (computedChecksum == rcvChecksum)){
//            Queue<Packet> newWindow = new LinkedList<>();
//            while (!window.isEmpty()){
//                Packet top = window.poll();
//                int topSeq =
//            }

//            System.out.println("lastAck");
//            System.out.println(lastAck);
//            System.out.println("ack number");
//            System.out.println(ack);
//            System.out.println("gap");
//            System.out.println(gap);

//            for (int i = 0; i< gap && !window.isEmpty(); i++){
////                System.out.println("poll window seq number");
////                System.out.println(window.peek().data[1]);
//                window.poll();
//            }

            /* when rcv correct ACK, poll out the pkts in window whose seq number range from lastACK to ACK  */
            for (int i = 0; i< gap && !window.isEmpty() && window.peek().data[1] != lastAck; i++) {
                System.out.println("poll window seq number");
                System.out.println(window.peek().data[1]);
                window.poll();
            }

            if(!window.isEmpty() && window.peek().data[1] == lastAck){  // window still space and the last
                window.poll();
            }
            lastAck = ack;
        }

        sentFromBufferToWindowIfCan();

    }

    /**
     * acquire packets from buffer if it can.
     */
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
    /**
     * when timeout, send all the packets in window out
     */
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


    /**
     * use payloadsize and message and cursor to create a packet
     * send the fragmented cursor to buffer
     */
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

    /**
     * compute checksum for the packet using the first 2bytes and rest 60bytes
     */
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
}
