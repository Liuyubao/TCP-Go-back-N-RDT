package sim;

/**
 * Simulation event base
 *
 * @author Jiupeng Zhang
 * @since 10/04/2019
 */
abstract class Event {
    private double scheduledTime;

    Event(double scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    double getScheduledTime() {
        return scheduledTime;
    }

    void reschedule(double scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
