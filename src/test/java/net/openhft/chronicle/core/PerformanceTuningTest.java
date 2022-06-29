package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.IOTools;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class PerformanceTuningTest {
    public static void main(String[] args) {
        assumeTrue(OS.isLinux());

        File cpu0ScalingGovernor = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
        assertTrue(cpu0ScalingGovernor.exists());

        String scalingGovernorsCheck = PerformanceTuning.checkScalingGovernors();
        System.out.println("Result of checking CPU scaling governors: " + scalingGovernorsCheck);
        assertNull(scalingGovernorsCheck);

        assertTrue(PerformanceTuning.issues().isEmpty());

        PerformanceTuning.reportIssues();
    }

    @Test
    public void scalingGovernorsSetToPerformance() throws FileNotFoundException {
        assumeTrue(OS.isLinux());
        assertNull(PerformanceTuning.checkScalingGovernors(pathTo("/cpu-performance")));
    }

    @Test
    public void scalingGovernorsForCpu2and3SetToPowersave() throws FileNotFoundException {
        assumeTrue(OS.isLinux());
        assertEquals(
                "Following CPUs have non-performant scaling governor setting: cpu2, cpu3.",
                PerformanceTuning.checkScalingGovernors(pathTo("/cpu-2-and-3-powersave")));
    }

    @Test
    public void readKernelCommandLineParameters() throws FileNotFoundException {
        assumeTrue(OS.isLinux());
        Set<String> kernelCmdLineParams = PerformanceTuning.kernelCommandLineParameters(pathTo("proc/cmdline"));

        assertEquals(
                set(
                        "BOOT_IMAGE=/vmlinuz-3.10.0-1160.25.1.el7.x86_64",
                        "root=/dev/mapper/centos-root",
                        "ro",
                        "LANG=en_GB.UTF-8",
                        "nohz_full=2-11",
                        "spectre_v2=off",
                        "processor.max_cstate=1",
                        "intel_idle.max_cstate=0",
                        "idle=poll",
                        "mce=ignore_ce",
                        "nosoftlockup",
                        "audit=0",
                        "nopti",
                        "crashkernel=auto",
                        "rd.lvm.lv=centos/root",
                        "rd.lvm.lv=centos/swap",
                        "rhgb",
                        "quiet"
                ),
                kernelCmdLineParams
        );
    }

    @Test
    public void checkSpectreMitigation() {
        assertNull(PerformanceTuning.checkSpectreMitigation(set("spectre_v2=off")));
        assertNull(PerformanceTuning.checkSpectreMitigation(set("nospectre_v2")));
        assertNull(PerformanceTuning.checkSpectreMitigation(set("mitigations=off")));

        assertNotNull(PerformanceTuning.checkSpectreMitigation(set("nosoftlockup")));
    }

    @Test
    public void checkCStates() {
        assertNull(PerformanceTuning.checkCStates(set("processor.max_cstate=1", "intel_idle.max_cstate=0", "idle=poll")));

        assertNotNull(PerformanceTuning.checkCStates(set("intel_idle.max_cstate=0", "idle=poll")));
        assertNotNull(PerformanceTuning.checkCStates(set("processor.max_cstate=1", "idle=poll")));
    }

    @Test
    public void checkMCE() {
        assertNull(PerformanceTuning.checkMCE(set("mce=ignore_ce")));

        assertNotNull(PerformanceTuning.checkMCE(set("inteethodFill_idle.max_cstate=0")));
        assertNotNull(PerformanceTuning.checkMCE(set("mce=off")));
    }

    @Test
    public void checkAudit() {
        assertNull(PerformanceTuning.checkAudit(set("audit=0")));
        assertNull(PerformanceTuning.checkAudit(set("audit=off")));

        assertNotNull(PerformanceTuning.checkAudit(set("audit=1")));
        assertNotNull(PerformanceTuning.checkAudit(set("audit=on")));
    }

    @Test
    public void checkPageTableIsolation() {
        assertNull(PerformanceTuning.checkPageTableIsolation(set("nopti")));
        assertNull(PerformanceTuning.checkPageTableIsolation(set("pti=off")));

        assertNotNull(PerformanceTuning.checkPageTableIsolation(set("pti=on")));
    }

    @SafeVarargs
    private static <T> Set<T> set(T... elements) {
        return new HashSet<>(asList(elements));
    }

    private static Path pathTo(String path) throws FileNotFoundException {
        return Paths.get(IOTools.urlFor(PerformanceTuningTest.class, path).getFile());
    }
}
