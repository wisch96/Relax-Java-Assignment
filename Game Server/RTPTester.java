public class RTPTester {
    public static void main(String[] args) {
        int simulations = 1000;
        long totalPayout = 0;
        long totalBet = simulations * 10;

        GameServer server = new GameServer(null);
        for (int i = 0; i < simulations; i++) {
            totalPayout += server.simulateGameRound();
        }

        double rtp = (double) totalPayout / totalBet;
        System.out.printf("RTP over %,d games: %.2f%%%n", simulations, rtp * 100);
    }
}
