package net.openhft.chronicle.core.threads;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.mockito.Mockito;

public class ThreadLocalHelperDiffblueTest extends CoreTestCommon {
  /**
   * Method under test: {@link ThreadLocalHelper#getTL(ThreadLocal, Object, Function)}
   */
  @Test
  public void testGetTL() {
    // Arrange
    ThreadLocal<WeakReference<Object>> threadLocal = new ThreadLocal<>();
    Function<Object, Object> function = mock(Function.class);
    when(function.apply(Mockito.<Object>any())).thenReturn("Apply");

    // Act and Assert
    assertEquals("Apply", ThreadLocalHelper.getTL(threadLocal, "42", function));
    verify(function).apply(Mockito.<Object>any());
  }

  /**
   * Method under test: {@link ThreadLocalHelper#getTL(ThreadLocal, Object, Function, ReferenceQueue, Consumer)}
   */
  @Test
  public void testGetTL2() {
    // Arrange
    ThreadLocal<WeakReference<Object>> threadLocal = new ThreadLocal<>();
    Function<Object, Object> constructor = mock(Function.class);
    when(constructor.apply(Mockito.<Object>any())).thenReturn("Apply");
    ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
    Consumer<WeakReference<Object>> registrar = mock(Consumer.class);
    doNothing().when(registrar).accept(Mockito.<WeakReference<Object>>any());

    // Act and Assert
    assertEquals("Apply",
        ThreadLocalHelper.getTL(threadLocal, "Supplying Entity", constructor, referenceQueue, registrar));
    verify(constructor).apply(Mockito.<Object>any());
    verify(registrar).accept(Mockito.<WeakReference<Object>>any());
  }

  /**
   * Method under test: {@link ThreadLocalHelper#getTL(ThreadLocal, Object, Function, ReferenceQueue, Consumer)}
   */
  @Test
  public void testGetTL3() {
    // Arrange
    ThreadLocal<WeakReference<Object>> threadLocal = new ThreadLocal<>();
    Function<Object, Object> constructor = mock(Function.class);
    when(constructor.apply(Mockito.<Object>any())).thenReturn("Apply");
    Consumer<WeakReference<Object>> registrar = mock(Consumer.class);
    doNothing().when(registrar).accept(Mockito.<WeakReference<Object>>any());

    // Act and Assert
    assertEquals("Apply", ThreadLocalHelper.getTL(threadLocal, "Supplying Entity", constructor, null, registrar));
    verify(constructor).apply(Mockito.<Object>any());
  }

  /**
   * Method under test: {@link ThreadLocalHelper#getTL(ThreadLocal, Object, Function, ReferenceQueue, Consumer)}
   */
  @Test
  public void testGetTL4() {
    // Arrange
    ThreadLocal<WeakReference<Object>> threadLocal = new ThreadLocal<>();
    Function<Object, Object> constructor = mock(Function.class);
    when(constructor.apply(Mockito.<Object>any())).thenReturn("Apply");

    // Act and Assert
    assertEquals("Apply", ThreadLocalHelper.getTL(threadLocal, "42", constructor, new ReferenceQueue<>(), null));
    verify(constructor).apply(Mockito.<Object>any());
  }

  /**
   * Method under test: {@link ThreadLocalHelper#getTL(ThreadLocal, Supplier)}
   */
  @Test
  public void testGetTL5() {
    // Arrange
    ThreadLocal<WeakReference<Object>> threadLocal = new ThreadLocal<>();
    Supplier<Object> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn("Get");

    // Act and Assert
    assertEquals("Get", ThreadLocalHelper.getTL(threadLocal, supplier));
    verify(supplier).get();
  }

  /**
  * Method under test: {@link ThreadLocalHelper#getSTL(ThreadLocal, Supplier)}
  */
  @Test
  public void testGetSTL() {
    // Arrange
    ThreadLocal<Object> threadLocal = new ThreadLocal<>();
    Supplier<Object> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn("Get");

    // Act and Assert
    assertEquals("Get", ThreadLocalHelper.getSTL(threadLocal, supplier));
    verify(supplier).get();
  }

  /**
   * Method under test: {@link ThreadLocalHelper#getSTL(ThreadLocal, Supplier)}
   */
  @Test
  public void testGetSTL2() {
    // Arrange
    Supplier<Object> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn("Get");
    CleaningThreadLocal<Object> threadLocal = CleaningThreadLocal.withCloseQuietly(supplier);
    Supplier<Object> supplier2 = mock(Supplier.class);
    when(supplier2.get()).thenReturn("Get");

    // Act and Assert
    assertEquals("Get", ThreadLocalHelper.getSTL(threadLocal, supplier2));
    verify(supplier).get();
  }
}

