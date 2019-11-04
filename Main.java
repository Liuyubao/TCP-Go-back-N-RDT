import sim.RdtSimulator;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Main routine to run the simulator
 *
 * @author Jiupeng Zhang
 * @since 10/04/2019
 */
public class Main {
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$-7s %5$s %n"); // set logging format
    }

    private static void setLogLevel(Level level) {
        Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(level));
    }

    public static void main(String[] args) {
        RdtSimulator simulator = new RdtSimulator(new MyRdtSender(), new MyRdtReceiver());
        if (args.length > 0) {
            try {
                simulator.setSimulationTime(Double.parseDouble(args[0]));    // total simulation time
                simulator.setMessageInterval(Double.parseDouble(args[1]));   // intervals between messages arrival
                simulator.setAverageMessageSize(Integer.parseInt(args[2]));  // average size of messages (in bytes)
                simulator.setOutOfOrderRate(Double.parseDouble(args[3]));    // probability of abnormal latency
                simulator.setLossRate(Double.parseDouble(args[4]));          // probability of packet loss
                simulator.setCorruptRate(Double.parseDouble(args[5]));       // probability of packet corruption
                if (args.length > 6) {
                    switch (args[6]) {
                        case "0": setLogLevel(Level.OFF); break;
                        case "1": setLogLevel(Level.WARNING); break;
                        case "2": setLogLevel(Level.ALL); break;
                    }
                }
            } catch (Throwable e) {
                System.out.println("usage: ./rdt_sim <sim_time> <mean_msg_arrivalint> <mean_msg_size> " +
                        "<outoforder_rate> <loss_rate> <corrupt_rate> <tracing_level>\n");
                System.exit(-1);
            }
        }
        simulator.start();
    }
}
