
import java.util.Random;
import java.util.Scanner;

/**
 * This is a test game class for debugging.
 * Feel free to use it if you like!
 */

public class SlotGame {

    private Random random = new Random();
    private int totalWin;
    private boolean isSimulationMode;
    private Scanner scanner = new Scanner(System.in);
    private GameServer server;

    public SlotGame(Long seed, boolean isSimulationMode) {
        this.isSimulationMode = isSimulationMode;
        if (seed != null) {
            this.random = new Random(seed);
        } else {
            this.random = new Random();
        }
        server = new GameServer(seed);
    }

    public int run() {
        String response;
        do {
            server.createGrid();
            printGrid(server.getGrid());
            playGameRound();
            System.out.println("GAME OVER!");
            if (server.getTotalPayout() > 0) {
                offerDoubleWager();
                totalWin += server.getTotalPayout();
            }
            System.out.println("Try again? (y/n)");
            if (isSimulationMode) {
                response = "n";
            } else {
                response = scanner.nextLine().trim().toLowerCase();
            }
        } while (response.equals("y"));
        System.out.println("Thanks for Playing!");

        if (!isSimulationMode) {
            scanner.close();
        }
        return totalWin;
    }

    private void playGameRound() {
        boolean hasClusters = server.findClusters();
        while (hasClusters) {
            System.out.println("Current Payout: " + server.getTotalPayout());
            server.destroySymbols();
            printGrid(server.getGrid());
            System.out.println("AVALANCHE!");
            server.callAvalanche();
            printGrid(server.getGrid());
            hasClusters = server.findClusters();
        }
    }

    private void offerDoubleWager() {
        System.out.println("You won " + server.getTotalPayout() + "!");
        System.out.println("Do you want to double your wager? (y/n)");
        String response;
        if (isSimulationMode) {
            boolean isDoubling = random.nextBoolean();
            if (isDoubling) {
                response = "y";
            } else {
                response = "n";
            }
        } else {
            response = scanner.nextLine().trim().toLowerCase();
        }
        if (response.equals("y")) {
            boolean win = server.isDoubleWager();

            if (win) {
                System.out.println("YOU WON! Your final win is: " + (server.getTotalPayout()));
            } else {
                System.out.println("You lost. Your final win is: " + server.getTotalPayout());
            }
        } else {
            System.out
                    .println("You chose to keep your money. Your final win is: " + (server.getTotalPayout()));
        }
    }

    private void printGrid(Symbol[][] grid) {
        for (Symbol[] row : grid) {
            for (Symbol cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        new SlotGame(null, false).run();
    }

}
