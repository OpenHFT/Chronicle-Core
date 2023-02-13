package net.openhft.chronicle.core.observable;

import net.openhft.chronicle.core.io.IORuntimeException;

import java.io.IOException;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppendableStateReporter implements StateReporter {

    private static final String INDENT = "   ";
    private static final String COLLECTION_SEPARATOR = "---";
    private static final String NULL_STRING = "<null>";
    private static final String ID_PROPERTY = "_id";
    private final Appendable appendable;
    private final IdentityHashMap<Object, Object> seenObjects;
    private int indentLevel = 0;

    public AppendableStateReporter(Appendable appendable) {
        this.appendable = appendable;
        this.seenObjects = new IdentityHashMap<>();
    }

    @Override
    public void writeProperty(String name, CharSequence value) {
        writeIndent().append(name).append(": ").append(value != null ? value : NULL_STRING).newLine();

    }

    @Override
    public void writeChild(String name, Object value) {
        writeIndent().append(name).append(":").newLine();
        indentLevel++;
        writeSingleChild(value);
        indentLevel--;
    }

    private void writeSingleChild(Object value) {
        if (value == null) {
            writeIndent().append(NULL_STRING).newLine();
        } else {
            if (value instanceof Observable) {
                Observable observable = (Observable) value;
                writeIndent().append(ID_PROPERTY).append(": ").append(observable.idString()).newLine();
                if (!seenObjects.containsKey(value)) {
                    seenObjects.put(value, value);
                    observable.dumpState(this);
                }
            } else {
                writeIndent().append(value.toString()).newLine();
            }
        }
    }

    @Override
    public void writeChildren(String name, Collection<?> children) {
        writeIndent().append(name).append(":").newLine();
        indentLevel++;
        AtomicBoolean first = new AtomicBoolean(true);
        children.forEach(child -> {
            if (!first.get()) {
                writeIndent().append(COLLECTION_SEPARATOR).newLine();
            }
            writeSingleChild(child);
            first.set(false);
        });
        indentLevel--;
    }

    private void newLine() {
        append(System.lineSeparator());
    }

    private AppendableStateReporter writeIndent() {
        for (int i = 0; i < indentLevel; i++) {
            append(INDENT);
        }
        return this;
    }

    private AppendableStateReporter append(CharSequence string) {
        try {
            appendable.append(string);
            return this;
        } catch (IOException e) {
            throw new IORuntimeException("Couldn't append!", e);
        }
    }
}
