package net.openhft.chronicle.core.jlbh;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.jlbh.JLBH;
import net.openhft.chronicle.jlbh.JLBHOptions;
import net.openhft.chronicle.jlbh.JLBHTask;
import net.openhft.chronicle.jlbh.TeamCityHelper;

/**
 * JLBH test for TeamCity graphs. See also the benchmarks module
 */
public class CopyMemoryJLBH implements JLBHTask {
    private final int capacity;
    private final int iterations;
    private JLBH jlbh;
    private long addr1;
    private long addr2;

    private CopyMemoryJLBH(int capacity, int iterations) {
        this.capacity = capacity;
        this.iterations = iterations;
    }

    @Override
    public void init(JLBH jlbh) {
        this.jlbh = jlbh;
        addr1 = UnsafeMemory.MEMORY.allocate(capacity);
        addr2 = UnsafeMemory.MEMORY.allocate(capacity);
    }

    @Override
    public void run(long startTimeNS) {
        UnsafeMemory.copyMemory(addr1, addr2, capacity);
        jlbh.sample(System.nanoTime() - startTimeNS);
    }

    @Override
    public void complete() {
        TeamCityHelper.teamCityStatsLastRun(this.getClass().getSimpleName() + "." + capacity, jlbh, iterations, System.out);
    }

    public static void main(String[] args) {
        System.setProperty("jvm.resource.tracing", "false");
        Jvm.init();
        final int capacity = Integer.getInteger("capacity", 122);
        final int throughput = Integer.getInteger("throughput", 5_000_000);
        final int iterations = Integer.getInteger("iterations", 50_000_000);
        final int warmup = Integer.getInteger("warmup", 5_000_000);
        final int runs = Integer.getInteger("runs", 4);
        System.out.println("capacity="+capacity);
        JLBHOptions jlbhOptions = new JLBHOptions().
                runs(runs).
                warmUpIterations(warmup).
                throughput(throughput).
                iterations(iterations).
                pauseAfterWarmupMS(100).
                recordOSJitter(false).
                jlbhTask(new CopyMemoryJLBH(capacity, iterations));
        JLBH jlbh = new JLBH(jlbhOptions);
        jlbh.start();
    }
}
