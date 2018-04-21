package net.openhft.chronicle.core.jlbh;

final class ThreadSafeJLBHResultConsumer implements JLBHResultConsumer {

    // the assumption is that the JLBHResult is immutable
    private volatile JLBHResult result;

    /**
     * Must be immutable.
     *
     * @param result Result provided by the JLBH
     */
    @Override
    public void accept(JLBHResult result) {
        this.result = result;
    }

    @Override
    public JLBHResult get() {
        return result;
    }
}