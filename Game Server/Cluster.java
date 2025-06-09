import java.util.List;

public class Cluster {
    private Symbol symbol;
    private List<int[]> positions;
    private List<int[]> blockerList;

    public Cluster(Symbol symbol, List<int[]> positions, List<int[]> blockerList) {
        this.symbol = symbol;
        this.positions = positions;
        this.blockerList = blockerList;
    }

    public List<int[]> getPositions() {
        return positions;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public int getClusterSize() {
        return positions.size();
    }

    public List<int[]> getBlockerList() {
        return blockerList;
    }
}
