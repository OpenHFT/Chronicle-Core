import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This class has no package declaration.
 */
public class ClassWithNoPackageTest {
    @Test
    public void getPackageName() {
        assertEquals("", Jvm.getPackageName(ClassWithNoPackageTest.class));
    }
}
