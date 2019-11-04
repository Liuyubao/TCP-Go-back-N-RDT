package sim;

import java.nio.charset.StandardCharsets;

import static sim.Packet.RDT_PKTSIZE;

/**
 * Reliable data transfer receiver
 *
 * @author Kai Shen
 * @author Jiupeng Zhang
 * @since 10/04/2019
 */
public abstract class RdtReceiver {
    private RdtSession session;

    void join(RdtSession session) {
        this.session = session;
    }

    /**
     * Event handler, called when a packet is passed from the lower
     * layer at the receiver
     */
    public abstract void receiveFromLowerLayer(Packet packet);

    /**
     * Get simulation time (in seconds)
     *
     * @return current simulation time
     */
    public double getSimulationTime() {
        return session.getTime();
    }

    /**
     * Pass a packet to the lower layer at the receiver
     */
    public void sendToLowerLayer(Packet packet) {
        if (session == null) throw new IllegalStateException("session not registered");

        // packet lost at lossRate
        double mR = Math.random();
        if (mR < session.lossRate) {
            System.out.println("session.lossRate");
            System.out.println(session.lossRate);
            System.out.println("Math.random()");
            System.out.println(mR);
            return;
        }

        // packet corrupted at corruptRate
        if (Math.random() < session.corruptRate) {
            for (int i = 0; i < RDT_PKTSIZE; i++) {
                packet.data[i] += Math.random() * 20 - 10;
            }
        }

        // schedule the packet arrival event at the other side
        double latency = session.averagePacketLatency;
        if (Math.random() < session.outOfOrderRate) {
            latency *= Math.random() * 2;
        }

        session.schedule(new RdtEvent.SenderFromLowerLayer(packet, session.getTime() + latency));
        session.counter.packetPassed++;
    }

    /**
     * Deliver a message to the upper layer at the receiver
     */
    int c2 = 0; // validate delivered message

    public void sendToUpperLayer(byte[] message) {
        if (session == null) throw new IllegalStateException("session not registered");

        for (int i = 0; i < message.length; i++, c2 = (c2 + 1) % 10) {
            if (message[i] != (byte) '0' + c2) {
                session.counter.failure++;
                RdtSession.LOG.severe(String.format("Time %.2fs (Receiver): delivered corrupted " +
                        "message \"%s\"", session.getTime(), new String(message, StandardCharsets.UTF_8)));
            }
        }
        session.counter.delivered += message.length;
    }
}
