package net.openhft.chronicle.core;

/**
 * Created by peter.lawrey on 28/02/15.
 */
public interface PersistenceMode {
    static final PersistenceMode ASYNCHRONOUS = Modes.ASYNCHRONOUS;
    static final PersistenceMode TO_DISK = Modes.TO_DISK;
    static final PersistenceMode REMOTE_PUSHED = Modes.REMOTE_PUSHED;
    static final PersistenceMode TO_DISK_REMOTE_PUSHED = Modes.TO_DISK_REMOTE_PUSHED;
    static final PersistenceMode COMMIT_TWICE = Modes.COMMIT_TWICE;

    /**
     * whether to wait for a flush to local disk
     */
    boolean localCommit();

    /**
     * the number of remote confirmations to wait for
     */
    int minConfirms();

    /**
     * the number of remote commits to wait for
     */
    int minRemoteCommits();

    enum Modes implements PersistenceMode {
        /**
         * Fully asynchronous
         */
        ASYNCHRONOUS(false, 0, 0),

        /**
         * committed to at least one node.
         */
        TO_DISK(true, 1, 0),

        /**
         * pushed to at least two nodes.
         */
        REMOTE_PUSHED(false, 2, 0),

        /**
         * committed to at least one node and pushed to a second mode.
         */
        TO_DISK_REMOTE_PUSHED(true, 2, 1),

        /**
         * committed to at least one node and pushed to a second mode.
         */
        COMMIT_TWICE(true, 2, 2);

        private final boolean localCommit;
        private final int minConfirms;
        private final int minRemoteCommits;

        Modes(boolean localCommit, int minConfirms, int minRemoteCommits) {
            this.localCommit = localCommit;
            this.minConfirms = minConfirms;
            this.minRemoteCommits = minRemoteCommits;
        }

        @Override
        public boolean localCommit() {
            return localCommit;
        }

        @Override
        public int minConfirms() {
            return minConfirms;
        }

        @Override
        public int minRemoteCommits() {
            return minRemoteCommits;
        }
    }
}
