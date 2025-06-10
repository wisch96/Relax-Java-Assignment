import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class GameServer {
    private final Symbol H1 = new Symbol("H1", 100);
    private final Symbol H2 = new Symbol("H2", 100);
    private final Symbol H3 = new Symbol("H3", 100);
    private final Symbol H4 = new Symbol("H4", 100);
    private final Symbol L5 = new Symbol("L5", 100);
    private final Symbol L6 = new Symbol("L6", 100);
    private final Symbol L7 = new Symbol("L7", 100);
    private final Symbol L8 = new Symbol("L8", 100);
    private final Symbol WR = new Symbol("WR", 100);
    private final Symbol BLOCKER = new Symbol("BLOCKER", 100);
    private final Symbol EMPTY = new Symbol("EMPTY", -1);

    private final int GRID_ROWS = 8;
    private final int GRID_COLS = 8;

    private final int MIN_CLUSTER_SIZE = 5;

    private Symbol[] symbolArr = new Symbol[] { H1, H2, H3, H4, L5, L6, L7, L8, WR, BLOCKER };

    private Symbol[][] grid = new Symbol[GRID_ROWS][GRID_COLS];

    private Random random;
    private HashMap<Symbol, List<Payout>> symbolPayoutMap = new HashMap<Symbol, List<Payout>>();
    private Set<Integer> affectedCols = new HashSet<>();

    private int totalPayout;
    private List<Cluster> clusters = new ArrayList<>();

    public GameServer(Long seed) {
        if (seed != null) {
            this.random = new Random(seed);
        } else {
            this.random = new Random();
        }
        initializePayouts();
    }

    public int simulateGameRound() {
        totalPayout = 0;
        createGrid();

        boolean hasClusters = findClusters();
        while (hasClusters) {
            destroySymbols();
            callAvalanche();
            hasClusters = findClusters();
        }
        boolean isDoubling = random.nextBoolean();
        if (isDoubling) {
            isDoubleWager();
        }
        return totalPayout;
    }

    public void createGrid() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                grid[row][col] = generateSymbol(random, true);
            }
        }
    }

    public Symbol[][] getGrid() {
        return grid;
    }

    public int getTotalPayout() {
        return totalPayout;
    }

    private Symbol generateSymbol(Random random, boolean includeBlockers) {
        int totalWeight = 0;
        for (Symbol symbol : symbolArr) {
            if (!includeBlockers && symbol.equals(BLOCKER)) {
                continue;
            }
            totalWeight += symbol.getWeight();
        }
        int r = random.nextInt(totalWeight);
        int currentWeight = 0;
        for (Symbol symbol : symbolArr) {
            currentWeight += symbol.getWeight();
            if (r < currentWeight) {
                return symbol;
            }
        }
        throw new IllegalStateException("Failed to select symbol by weight");
    }

    public boolean findClusters() {
        boolean[][] globalVisited = new boolean[GRID_ROWS][GRID_COLS];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (!globalVisited[row][col]) {
                    Symbol symbol = grid[row][col];
                    if (symbol.equals(WR) || symbol.equals(BLOCKER)) {
                        continue;
                    }
                    Cluster cluster = bfs(row, col, globalVisited, symbol);
                    if (cluster.getClusterSize() >= MIN_CLUSTER_SIZE) {
                        clusters.add(cluster);
                    }
                }
            }
        }
        setPayout(clusters);
        return !clusters.isEmpty();
    }

    private Cluster bfs(int startRow, int startCol, boolean[][] globalVisited, Symbol symbol) {
        List<int[]> cluster = new ArrayList<>();
        List<int[]> blockerList = new ArrayList<>();
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] localVisited = new boolean[GRID_ROWS][GRID_COLS];
        queue.add(new int[] { startRow, startCol });

        globalVisited[startRow][startCol] = true;

        localVisited[startRow][startCol] = true;
        int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            cluster.add(current);
            int row = current[0];
            int col = current[1];

            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                if (!isInBounds(newRow, newCol) || localVisited[newRow][newCol])
                    continue;

                Symbol newSymbol = grid[newRow][newCol];

                boolean isVisitable = (newSymbol.equals(symbol) || newSymbol.equals(WR)) || newSymbol.equals(BLOCKER);

                if (isVisitable) {
                    if (!newSymbol.equals(WR)) {
                        globalVisited[newRow][newCol] = true;
                    }
                    if (newSymbol.equals(BLOCKER)) {
                        blockerList.add(new int[] { newRow, newCol });
                        localVisited[newRow][newCol] = true;
                    } else {
                        queue.add(new int[] { newRow, newCol });
                        localVisited[newRow][newCol] = true;
                    }
                }
            }
        }
        return new Cluster(symbol, cluster, blockerList);
    }

    private boolean isInBounds(int row, int col) {
        boolean withinRows = row >= 0 && row < grid.length;
        boolean withinCols = col >= 0 && col < grid[0].length;
        return withinRows && withinCols;
    }

    public void setPayout(List<Cluster> allClusters) {
        for (Cluster cluster : allClusters) {
            Symbol symbol = cluster.getSymbol();
            int size = cluster.getClusterSize();
            List<Payout> payoutList = symbolPayoutMap.get(symbol);
            for (Payout clusterPayout : payoutList) {
                if (clusterPayout.isInRange(size)) {
                    totalPayout += clusterPayout.getPayout();
                    break;
                }
            }
        }
    }

    public void destroySymbols() {
        for (Cluster cluster : clusters) {
            List<int[]> positonList = cluster.getPositions();
            for (int[] position : positonList) {
                int row = position[0];
                int col = position[1];
                grid[row][col] = EMPTY;
                affectedCols.add(col);
            }
            List<int[]> blockerList = cluster.getBlockerList();
            if (blockerList.isEmpty()) {
                continue;
            }
            for (int[] blocker : blockerList) {
                int row = blocker[0];
                int col = blocker[1];
                grid[row][col] = EMPTY;
                affectedCols.add(col);
            }
        }
        clusters.clear();
    }

    public void callAvalanche() {
        for (int col : affectedCols) {
            collapseColumn(col);
        }
        affectedCols.clear();
    }

    private void collapseColumn(int col) {
        int writeRow = GRID_ROWS - 1;

        for (int row = GRID_ROWS - 1; row >= 0; row--) {
            if (!grid[row][col].equals(EMPTY)) {
                grid[writeRow][col] = grid[row][col];
                writeRow--;
            }
        }

        while (writeRow >= 0) {
            grid[writeRow][col] = generateSymbol(random, false);
            writeRow--;
        }

    }

    public boolean isDoubleWager() {
        boolean win = random.nextBoolean();
        if (win) {
            totalPayout *= 2;
            return true;
        }
        totalPayout = 0;
        return false;
    }

    private void initializePayouts() {
        List<Payout> h1Payouts = new ArrayList<>();
        h1Payouts.add(new Payout(5, 8, 5));
        h1Payouts.add(new Payout(9, 12, 6));
        h1Payouts.add(new Payout(13, 16, 7));
        h1Payouts.add(new Payout(17, 20, 5));
        h1Payouts.add(new Payout(21, Integer.MAX_VALUE, 10));
        symbolPayoutMap.put(H1, h1Payouts);

        List<Payout> h2Payouts = new ArrayList<>();
        h2Payouts.add(new Payout(5, 8, 4));
        h2Payouts.add(new Payout(9, 12, 5));
        h2Payouts.add(new Payout(13, 16, 6));
        h2Payouts.add(new Payout(17, 20, 7));
        h2Payouts.add(new Payout(21, Integer.MAX_VALUE, 9));
        symbolPayoutMap.put(H2, h2Payouts);

        List<Payout> h3Payouts = new ArrayList<>();
        h3Payouts.add(new Payout(5, 8, 4));
        h3Payouts.add(new Payout(9, 12, 5));
        h3Payouts.add(new Payout(13, 16, 6));
        h3Payouts.add(new Payout(17, 20, 7));
        h3Payouts.add(new Payout(21, Integer.MAX_VALUE, 9));
        symbolPayoutMap.put(H3, h3Payouts);

        List<Payout> h4Payouts = new ArrayList<>();
        h4Payouts.add(new Payout(5, 8, 3));
        h4Payouts.add(new Payout(9, 12, 4));
        h4Payouts.add(new Payout(13, 16, 5));
        h4Payouts.add(new Payout(17, 20, 6));
        h4Payouts.add(new Payout(21, Integer.MAX_VALUE, 7));
        symbolPayoutMap.put(H4, h4Payouts);

        List<Payout> L5Payouts = new ArrayList<>();
        L5Payouts.add(new Payout(5, 8, 1));
        L5Payouts.add(new Payout(9, 12, 2));
        L5Payouts.add(new Payout(13, 16, 3));
        L5Payouts.add(new Payout(17, 20, 4));
        L5Payouts.add(new Payout(21, Integer.MAX_VALUE, 5));
        symbolPayoutMap.put(L5, L5Payouts);

        List<Payout> L6Payouts = new ArrayList<>();
        L6Payouts.add(new Payout(5, 8, 1));
        L6Payouts.add(new Payout(9, 12, 2));
        L6Payouts.add(new Payout(13, 16, 3));
        L6Payouts.add(new Payout(17, 20, 4));
        L6Payouts.add(new Payout(21, Integer.MAX_VALUE, 5));
        symbolPayoutMap.put(L6, L6Payouts);

        List<Payout> L7Payouts = new ArrayList<>();
        L7Payouts.add(new Payout(5, 8, 1));
        L7Payouts.add(new Payout(9, 12, 2));
        L7Payouts.add(new Payout(13, 16, 3));
        L7Payouts.add(new Payout(17, 20, 4));
        L7Payouts.add(new Payout(21, Integer.MAX_VALUE, 5));
        symbolPayoutMap.put(L7, L7Payouts);

        List<Payout> L8Payouts = new ArrayList<>();
        L8Payouts.add(new Payout(5, 8, 1));
        L8Payouts.add(new Payout(9, 12, 2));
        L8Payouts.add(new Payout(13, 16, 3));
        L8Payouts.add(new Payout(17, 20, 4));
        L8Payouts.add(new Payout(21, Integer.MAX_VALUE, 5));
        symbolPayoutMap.put(L8, L8Payouts);
    }

}