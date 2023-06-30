package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.openhft.chronicle.core.util.ThrowingConsumer;
import org.junit.Test;
import org.mockito.Mockito;

public class CleaningThreadLocalDiffblueTest {
  /**
   * Method under test: {@link CleaningThreadLocal#withCloseQuietly(Supplier)}
   */
  @Test
  public void testWithCloseQuietly() {
    // Arrange, Act and Assert
    assertNull(CleaningThreadLocal.<Object>withCloseQuietly(mock(Supplier.class)).get());
  }

  /**
   * Method under test: {@link CleaningThreadLocal#withCleanup(Supplier, ThrowingConsumer)}
   */
  @Test
  public void testWithCleanup() {
    // Arrange, Act and Assert
    assertNull(CleaningThreadLocal.<Object>withCleanup(mock(Supplier.class), mock(ThrowingConsumer.class)).get());
    assertNull(CleaningThreadLocal
        .<Object>withCleanup(mock(Supplier.class), mock(ThrowingConsumer.class), mock(Function.class))
        .get());
    assertNull(CleaningThreadLocal.<Object>withCleanup(mock(ThrowingConsumer.class)).get());
  }

  /**
   * Method under test: {@link CleaningThreadLocal#initialValue()}
   */
  @Test
  public void testInitialValue() {
    // Arrange, Act and Assert
    assertNull(CleaningThreadLocal.<Object>withCleanup(mock(ThrowingConsumer.class)).initialValue());
  }

  /**
   * Method under test: {@link CleaningThreadLocal#initialValue()}
   */
  @Test
  public void testInitialValue2() {
    // Arrange
    Supplier<Object> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn("Get");

    // Act and Assert
    assertEquals("Get", CleaningThreadLocal.withCloseQuietly(supplier).initialValue());
    verify(supplier).get();
  }

  /**
   * Method under test: {@link CleaningThreadLocal#get()}
   */
  @Test
  public void testGet() {
    // Arrange, Act and Assert
    assertNull(CleaningThreadLocal.<Object>withCleanup(mock(ThrowingConsumer.class)).get());
  }

  /**
   * Method under test: {@link CleaningThreadLocal#get()}
   */
  @Test
  public void testGet2() {
    // Arrange
    Supplier<Object> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn("Get");

    // Act and Assert
    assertEquals("Get", CleaningThreadLocal.withCloseQuietly(supplier).get());
    verify(supplier).get();
  }

  /**
   * Method under test: {@link CleaningThreadLocal#cleanup(Object)}
   */
  @Test
  public void testCleanup() throws Throwable {
    // Arrange
    ThrowingConsumer<Object, Exception> cleanup = mock(ThrowingConsumer.class);
    doNothing().when(cleanup).accept(Mockito.<Object>any());

    // Act
    CleaningThreadLocal.withCleanup(cleanup).cleanup("Value");

    // Assert
    verify(cleanup).accept(Mockito.<Object>any());
  }

  /**
  * Method under test: {@link CleaningThreadLocal#CleaningThreadLocal(Supplier, ThrowingConsumer)}
  */
  @Test
  public void testConstructor() {
    // Arrange, Act and Assert
    assertNull((new CleaningThreadLocal<>(mock(Supplier.class), mock(ThrowingConsumer.class))).get());
    assertNull(
        (new CleaningThreadLocal<>(mock(Supplier.class), mock(ThrowingConsumer.class), mock(UnaryOperator.class)))
            .get());
  }
}

