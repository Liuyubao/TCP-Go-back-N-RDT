package sim;

/**
 * Reliable data transfer events
 *
 * @author Jiupeng Zhang
 * @see Event
 * @since 10/04/2019
 */
class RdtEvent {
    /**
     * Event that the upper layer at the sender instructs rdt
     * layer to send out a message
     */
    static class SenderFromUpperLayer extends Event {
        SenderFromUpperLayer(double scheduledTime) {
            super(scheduledTime);
        }
    }

    /**
     * Event that the lower layer at the sender informs the rdt
     * layer that a packet is received from the link
     */
    static class SenderFromLowerLayer extends Event {
        private Packet packet;

        SenderFromLowerLayer(Packet packet, double scheduledTime) {
            super(scheduledTime);
            this.packet = packet;
        }

        Packet getPacket() {
            return packet;
        }
    }

    /**
     * Event that the timer at the sender expires
     */
    static class SenderTimeout extends Event {
        SenderTimeout(double scheduledTime) {
            super(scheduledTime);
        }
    }

    /**
     * Event that the lower layer at the receiver informs the
     * rdt layer that a packet is received from the link
     */
    static class ReceiverFromLowerLayer extends Event {
        private Packet packet;

        ReceiverFromLowerLayer(Packet packet, double scheduledTime) {
            super(scheduledTime);
            this.packet = packet;
        }

        Packet getPacket() {
            return packet;
        }
    }

    /**
     * Event that receiver is ready to informs the upper layer
     * and delivery messages
     */
    static class ReceiverToUpperLayer extends Event {
        private byte[] message;

        ReceiverToUpperLayer(byte[] message, double scheduledTime) {
            super(scheduledTime);
            this.message = message;
        }

        byte[] getMessage() {
            return message;
        }
    }
}
