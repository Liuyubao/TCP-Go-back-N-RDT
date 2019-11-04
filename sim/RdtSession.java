package sim;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/**
 * Reliable data transfer session
 *
 * @author Jiupeng Zhang
 * @see RdtEvent
 * @since 10/04/2019
 */
class RdtSession {
    static final Logger LOG = Logger.getLogger(RdtSession.class.getName());

    static class Counter {
        long sent = 0;
        long delivered = 0;
        long packetPassed = 0;
        long failure = 0;
    }

    Counter counter;
    private PriorityQueue<Event> events;
    private double time; // virtual time

    int averageMessageSize;
    double simulationTime, messageInterval, averagePacketLatency, outOfOrderRate, lossRate, corruptRate;

    RdtSession(double initTime, double simulationTime, double messageInterval,
               int averageMessageSize, double averagePacketLatency, double outOfOrderRate,
               double lossRate, double corruptRate) {
        this.counter = new Counter();
        this.events = new PriorityQueue<>(Comparator.comparingDouble(Event::getScheduledTime));
        this.time = initTime;
        this.averageMessageSize = averageMessageSize;
        this.simulationTime = simulationTime;
        this.messageInterval = messageInterval;
        this.averagePacketLatency = averagePacketLatency;
        this.outOfOrderRate = outOfOrderRate;
        this.lossRate = lossRate;
        this.corruptRate = corruptRate;
    }

    void schedule(Event e) {
        if (e.getScheduledTime() < time) return; // ignore pass events
        events.add(e);
        time += .00001; // avoid scheduling conflicts
    }

    void cancel(Event e) {
        events.remove(e);
    }

    boolean hasNext() {
        return !events.isEmpty();
    }

    Event next() {
        if (events.isEmpty()) return null;

        Event e = events.poll();
        time = e.getScheduledTime();
        return e;
    }

    double getTime() {
        return time;
    }
}
