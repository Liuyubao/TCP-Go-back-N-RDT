import sim.Packet;
import sim.RdtReceiver;

import static sim.Packet.RDT_PKTSIZE;

/**
 * My reliable data transfer receiver
 * <pre>
 *     This implementation assumes there is no packet loss, corruption,
 *     or reordering. You will need to enhance it to deal with all these
 *     situations. In this implementation, the packet format is laid out
 *     as the following:
 *
 *     |<-  1 byte  ->|<-             the rest            ->|
 *     | payload size |<-             payload             ->|
 *
 *     The first byte of each packet indicates the size of the payload
 *     (excluding this single-byte header)
 *
 *     Routines that you can call at the receiver:
 *     {@link #getSimulationTime()}         get simulation time (in seconds)
 *     {@link #sendToLowerLayer(Packet)}    pass a packet to the lower layer at the receiver
 *     {@link #sendToUpperLayer(byte[])}    deliver a message to the upper layer at the receiver
 * </pre>
 *
 * @author Jiupeng Zhang
 * @author Kai Shen
 * @since 10/04/2019
 */
public class MyRdtReceiver extends RdtReceiver {

    private int expectedSeq = 0;

    /**
     * Receiver initialization
     */
    public MyRdtReceiver() {
    }

    /**
     * Event handler, called when a packet is passed from the lower
     * layer at the receiver
     */
    public void receiveFromLowerLayer(Packet packet) {
        int rcvSeq = packet.data[1];
        long rcvChecksum = (((long)((packet.data[2]) << 8) & 0xFF00) | ((long)packet.data[3] & 0xFF));
        long computedChecksum = MyRdtSender.checkSum(packet.data);
        System.out.println("expectedSeq");
        System.out.println(expectedSeq);
        System.out.println("rcvSeq");
        System.out.println(rcvSeq);

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
}
