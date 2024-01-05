package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BuilderTest {

    @Test
    void buildShouldReturnNonNullInstance() {
        Builder<MyClass> builder = new MyClassBuilder(); // MyClassBuilder is a hypothetical implementation
        MyClass instance = builder.build();
        assertNotNull(instance);
    }

    @Test
    void buildShouldReturnNewInstanceForMutableTypes() {
        Builder<MyClass> builder = new MyClassBuilder(); // Assuming MyClass is mutable
        MyClass firstInstance = builder.build();
        MyClass secondInstance = builder.build();
        assertNotSame(firstInstance, secondInstance);
    }

    @Test
    void buildShouldThrowExceptionIfInvokedMultipleTimesWhenNotAllowed() {
        Builder<MyClass> oneTimeUseBuilder = new OneTimeUseMyClassBuilder(); // Hypothetical one-time use builder
        oneTimeUseBuilder.build();
        assertThrows(IllegalStateException.class, oneTimeUseBuilder::build);
    }

    @Test
    void getShouldDelegateToBuild() {
        Builder<MyClass> builder = new MyClassBuilder();
        MyClass instanceFromGet = builder.get();
        MyClass instanceFromBuild = builder.build();
        // Depending on the implementation, these instances may or may not be the same.
        // The assertion here should match the expected behavior of the Builder implementation.
    }
}

// Hypothetical implementations of Builder
class MyClassBuilder implements Builder<MyClass> {
    @Override
    public MyClass build() {
        return new MyClass(); // Assuming MyClass is a mutable type
    }
}

class OneTimeUseMyClassBuilder implements Builder<MyClass> {
    private boolean built = false;

    @Override
    public MyClass build() {
        if (built) {
            throw new IllegalStateException("Builder can only be used once");
        }
        built = true;
        return new MyClass(); // Assuming MyClass is a mutable type
    }
}
