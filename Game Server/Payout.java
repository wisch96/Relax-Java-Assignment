
public class Payout {

    private int minSize;
    private int maxSize;
    private int payout;

    public Payout(int minSize, int maxSize, int payout) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.payout = payout;
    }

    public int getPayout() {
        return payout;
    }

    public boolean isInRange(int size) {
        return size >= minSize && size <= maxSize;
    }

}
