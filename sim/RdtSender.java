package sim;

import static sim.Packet.RDT_PKTSIZE;

/**
 * Reliable data transfer sender
 *
 * @author Kai Shen
 * @author Jiupeng Zhang
 * @since 10/04/2019
 */
public abstract class RdtSender {
    private RdtSession session;
    private RdtEvent.SenderTimeout timer;

    void join(RdtSession session) {
        this.session = session;
    }

    /**
     * Event handler, called when a message is passed from the upper
     * layer at the sender
     */
    public abstract void receiveFromUpperLayer(byte[] message);

    /**
     * Event handler, called when a packet is passed from the lower
     * layer at the sender
     */
    public abstract void receiveFromLowerLayer(Packet packet);

    /**
     * Event handler, called when the timer expires
     */
    public abstract void onTimeout();

    /**
     * Get simulation time (in seconds)
     *
     * @return current simulation time
     */
    public double getSimulationTime() {
        return session.getTime();
    }

    /**
     * Start the sender timer with a specified timeout (in seconds).
     * the timer is canceled with {@link #stopTimer()} is called or a new
     * startTimer(double) is called before the current timer expires.
     * {@link #onTimeout()} will be called when the timer expires.
     */
    public void startTimer(double timeout) {
        if (isTimerSet()) stopTimer();
        RdtEvent.SenderTimeout e = new RdtEvent.SenderTimeout(session.getTime() + timeout);
        session.schedule(e);
        timer = e;
    }

    /**
     * Stop the sender timer
     */
    public void stopTimer() {
        if (timer != null) {
            session.cancel(timer);
            timer = null;
        }
    }

    /**
     * Check whether the sender timer is being set
     *
     * @return if the timer is set
     */
    public boolean isTimerSet() {
        return timer != null;
    }

    /**
     * Pass a packet to the lower layer at the sender
     */
    public void sendToLowerLayer(Packet packet) {
        if (session == null) throw new IllegalStateException("session not registered");

//        // packet lost at lossRate
//        if (Math.random() < session.lossRate) return;

        // packet lost at lossRate
        double mR = Math.random();
        if (mR < session.lossRate) {
//            System.out.println("session.lossRate");
//            System.out.println(session.lossRate);
//            System.out.println("Math.random()");
//            System.out.println(mR);
            System.out.println("loss packet seq number");
            System.out.println(packet.data[1]);
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
        if (Math.random() < session.outOfOrderRate) latency *= Math.random() * 2;
        session.schedule(new RdtEvent.ReceiverFromLowerLayer(packet, session.getTime() + latency));
        session.counter.packetPassed++;
    }
}
