package net.openhft.chronicle.core.io;

public class VanillaReferenceOwner implements ReferenceOwner, QueryCloseable {
    private final String name;

    public VanillaReferenceOwner(String name) {
        this.name = name;
    }

    @Override
    public String referenceName() {
        return toString();
    }

    @Override
    public String toString() {
        return "VanillaReferenceOwner{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
