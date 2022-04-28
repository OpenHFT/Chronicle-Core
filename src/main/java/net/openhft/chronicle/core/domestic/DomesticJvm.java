package net.openhft.chronicle.core.domestic;

import java.util.List;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;

public final class DomesticJvm {

    private static final int COMPILE_THRESHOLD = getCompileThreshold0();
    private static final List<String> INPUT_ARGUMENTS = getRuntimeMXBean().getInputArguments();

    // Suppresses default constructor, ensuring non-instantiability.
    private DomesticJvm() {
    }

    /**
     * Returns the compiler threshold for the JVM or else an
     * estimate thereof (e.g. 10_000).
     * <p>
     * The compiler threshold can be explicitly set using the command
     * line parameter "-XX:CompileThreshold="
     *
     * @return the compiler threshold for the JVM or else an
     * estimate thereof (e.g. 10_000)
     */
    public static int compileThreshold() {
        return COMPILE_THRESHOLD;
    }

    private static int getCompileThreshold0() {
        for (String inputArgument : INPUT_ARGUMENTS) {
            final String prefix = "-XX:CompileThreshold=";
            if (inputArgument.startsWith(prefix)) {
                try {
                    return Integer.parseInt(inputArgument.substring(prefix.length()));
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
        }
        return 10_000;
    }


}