package net.openhft.chronicle.core.time;

public class BackgroundTimeProviderMain {
    public static void main(String[] args) throws InterruptedException {
        int under = 0, over = 0;
        for (int i = 0; i < 6_000; i++) {
            BackgroundTimeProvider tp = BackgroundTimeProvider.INSTANCE;
            long nowMS;
            long nowNS;
            if (i % 2 == 0) {
                nowMS = System.currentTimeMillis();
                nowNS = tp.currentTimeNanos();
            } else {
                nowNS = tp.currentTimeNanos();
                nowMS = System.currentTimeMillis();
            }
            long diff = nowNS - nowMS * 1000000;
            if (diff < -1e5 || diff > 11e5)
                System.out.println("diff: " + diff);
            if (diff < 0) {
                under += 1 + (diff / -1e4);
            }
            if (diff > 1e6) {
                over += 1 + (diff - 1e6) / 1e4;
            }
            Thread.sleep(1);
        }
        System.out.println("under: " + under + ", over: " + over);
    }

}