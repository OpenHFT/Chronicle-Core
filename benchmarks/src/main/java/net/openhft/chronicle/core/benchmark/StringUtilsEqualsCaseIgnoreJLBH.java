package net.openhft.chronicle.core.benchmark;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.StringUtils;
import net.openhft.chronicle.jlbh.JLBH;
import net.openhft.chronicle.jlbh.JLBHOptions;
import net.openhft.chronicle.jlbh.JLBHTask;
import net.openhft.chronicle.jlbh.TeamCityHelper;

public class StringUtilsEqualsCaseIgnoreJLBH implements JLBHTask {

    private static String input;
    private final int iterations;
    private JLBH jlbh;

    private StringUtilsEqualsCaseIgnoreJLBH(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public void init(JLBH jlbh) {
        this.jlbh = jlbh;
    }

    @Override
    public void run(long startTimeNS) {
        StringUtils.equalsCaseIgnore(input, input);
        jlbh.sample(System.nanoTime() - startTimeNS);
    }

    @Override
    public void complete() {
        TeamCityHelper.teamCityStatsLastRun(this.getClass().getSimpleName(), jlbh, iterations, System.out);
    }

    public static void main(String[] args) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 1024; i++) {
            sb.append((byte) 0);
        }
        input = sb.toString();

        System.setProperty("jvm.resource.tracing", "false");
        Jvm.init();
        final int throughput = Integer.getInteger("throughput", 500_000);
        final int iterations = Integer.getInteger("iterations", 10_000_000);
        final int warmup = Integer.getInteger("warmup", 5_000_000);
        final int runs = Integer.getInteger("runs", 4);
        JLBHOptions jlbhOptions = new JLBHOptions().
                runs(runs).
                warmUpIterations(warmup).
                throughput(throughput).
                iterations(iterations).
                pauseAfterWarmupMS(100).
                recordOSJitter(false).
                jlbhTask(new StringUtilsEqualsCaseIgnoreJLBH(iterations));
        JLBH jlbh = new JLBH(jlbhOptions);
        jlbh.start();
    }

}
