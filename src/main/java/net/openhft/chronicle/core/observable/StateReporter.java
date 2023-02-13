package net.openhft.chronicle.core.observable;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface StateReporter {

    static StateReporter toAppendable(Appendable appendable) {
        return new AppendableStateReporter(appendable);
    }

    void writeProperty(String name, @Nullable CharSequence value);

    void writeChild(String name, @Nullable Object value);

    void writeChildren(String name, @Nullable Collection<?> children);
}
