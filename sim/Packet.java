package sim;

import java.util.Random;

/**
 * Data unit passed between rdt layer and the lower layer,
 * each packet has a fixed size of {@code RDT_PKTSIZE}
 *
 * @author Jiupeng Zhang
 * @since 10/04/2019
 */
public class Packet {
    public static final int RDT_PKTSIZE = 64;
    private static Random random = new Random();

    public byte[] data;

    public Packet() {
        this.data = new byte[RDT_PKTSIZE];
        random.nextBytes(this.data); // random fill
    }
}
