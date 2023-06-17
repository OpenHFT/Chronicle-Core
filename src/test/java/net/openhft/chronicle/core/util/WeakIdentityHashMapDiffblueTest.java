package net.openhft.chronicle.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class WeakIdentityHashMapDiffblueTest {
  /**
   * Method under test: {@link WeakIdentityHashMap#isEmpty()}
   */
  @Test
  public void testIsEmpty() {
    // Arrange, Act and Assert
    assertTrue((new WeakIdentityHashMap<>()).isEmpty());
  }

  /**
   * Method under test: {@link WeakIdentityHashMap#isEmpty()}
   */
  @Test
  public void testIsEmpty2() {
    // Arrange
    WeakIdentityHashMap<Object, Object> objectObjectMap = new WeakIdentityHashMap<>();
    objectObjectMap.put("Key", "Value");

    // Act and Assert
    assertFalse(objectObjectMap.isEmpty());
  }

  /**
   * Method under test: {@link WeakIdentityHashMap#get(Object)}
   */
  @Test
  public void testGet() {
    // Arrange, Act and Assert
    assertNull((new WeakIdentityHashMap<>()).get("Key"));
  }

  /**
   * Method under test: {@link WeakIdentityHashMap#get(Object)}
   */
  @Test
  public void testGet2() {
    // Arrange
    WeakIdentityHashMap<Object, Object> objectObjectMap = new WeakIdentityHashMap<>();
    objectObjectMap.put("Key", "Value");

    // Act and Assert
    assertEquals("Value", objectObjectMap.get("Key"));
  }

  /**
   * Method under test: {@link WeakIdentityHashMap#put(Object, Object)}
   */
  @Test
  public void testPut() {
    // Arrange, Act and Assert
    assertNull((new WeakIdentityHashMap<>()).put("Key", "Value"));
  }

  /**
   * Method under test: {@link WeakIdentityHashMap#put(Object, Object)}
   */
  @Test
  public void testPut2() {
    // Arrange
    WeakIdentityHashMap<Object, Object> objectObjectMap = new WeakIdentityHashMap<>();
    objectObjectMap.put("Key", "Value");

    // Act and Assert
    assertEquals("Value", objectObjectMap.put("Key", "Value"));
  }

  /**
   * Method under test: {@link WeakIdentityHashMap#remove(Object)}
   */
  @Test
  public void testRemove() {
    // Arrange, Act and Assert
    assertNull((new WeakIdentityHashMap<>()).remove("Key"));
  }

  /**
   * Method under test: {@link WeakIdentityHashMap#remove(Object)}
   */
  @Test
  public void testRemove2() {
    // Arrange
    WeakIdentityHashMap<Object, Object> objectObjectMap = new WeakIdentityHashMap<>();
    objectObjectMap.put("Key", "Value");

    // Act and Assert
    assertEquals("Value", objectObjectMap.remove("Key"));
  }

  /**
   * Method under test: {@link WeakIdentityHashMap#keySet()}
   */
  @Test
  public void testKeySet() {
    // Arrange, Act and Assert
    assertTrue((new WeakIdentityHashMap<>()).keySet().isEmpty());
  }

  /**
  * Method under test: default or parameterless constructor of {@link WeakIdentityHashMap}
  */
  @Test
  public void testConstructor() {
    // Arrange, Act and Assert
    assertTrue((new WeakIdentityHashMap<>()).isEmpty());
  }

  /**
   * Method under test: {@link WeakIdentityHashMap#entrySet()}
   */
  @Test
  public void testEntrySet() {
    // Arrange, Act and Assert
    assertTrue((new WeakIdentityHashMap<>()).entrySet().isEmpty());
  }
}

