Chronicle Core
==========

This library wraps up low level access to

 - [Off Heap Memory Access](#off-heap-memory-access)
 - [JVM Access Methods](#jvm-access-methods)
 - [OS Calls](#os-calls)
 - [Resource Reference Counting](#resource-reference-counting)
 - [Object Pools](#object-pools)
 - [Class Local Caching](#class-local-caching)
 - [Maths Functions](#maths-functions) for casting types, rounding double, faster hashing.
 - [Serializable Lambdas](#serializable-lambdas)
 - [Histogram](#histogram) A high performance wide range histogram.
 
Off Heap Memory Access
=================
This allows you to access native memory using primitives and some thread safe operations.

```java
Memory memory = OS.memory();
long address = memory.allocate(1024);
try {
    memory.writeInt(address, 1);
    assert memory.readInt(address) == 1;
    final boolean swapped = memory.compareAndSwapInt(address, 1, 2);
    assert swapped;
    assert memory.readInt(address) == 2;
} finally {
    memory.freeMemory(address, 1024);
}
```

JVM Access Methods
=================
Check the JVM is running in debug mode
```java
if (Jvm.isDebug()) {
   // running in debug.
```

Rethrow a checked exception as an unchecked one.

```java
try {
    // IO operation
} catch (IOException ioe) {
    throw Jvm.rethrow(ioe);
}
```

Get a Field for a Class by name

```java
Field theUnsafe = Jvm.getField(Unsafe.class, "theUnsafe");
Unsafe unsafe = (Unsafe) theUnsafe.get(null);
```

OS Calls
=================
Access to system calls 
```java
int processId = OS.getProcessId();
int maxProcessId = OS.getMaxProcessId();
int pageSize = OS.getPageSize();
boolean isWindows = OS.isWindows();
boolean is64bit = OS.is64Bit();
String hostname = OS.getHostName();
String username = OS.getUserName();
String targetDir = OS.getTarget(); // where is the target directory during builds.
```

Memory mapped files
```java
FileChannel fc = new RandomAccessFile(fileName, "rw").getChannel();
// map in 64 KiB
long address = OS.map(fc, MapMode.READ_WRITE, 0, 64 << 10);
// use address
OS.memory().writeLong(1024L, 0x1234567890ABCDEFL);
// unmap memory region
OS.unmap(address, 64 << 10);
```

Resource Reference Counting
=================
Use reference counting to deterministically release resources.
```java
MappedFile mf = MappedFile.mappedFile(tmp, chunkSize, 0);
MappedBytesStore bs = mf.acquireByteStore(chunkSize + (1 << 10));

assertEquals(2, mf.refCount());
assertEquals(3, bs.refCount());
assertEquals("refCount: 2, 0, 3", mf.referenceCounts());

mf.close();
assertEquals(2, bs.refCount());
assertEquals("refCount: 1, 0, 2", mf.referenceCounts());
bs2.release();
assertEquals(1, mf.refCount());
assertEquals(1, bs.refCount());
bs.release();
assertEquals(0, bs.refCount());
assertEquals(0, mf.refCount());
assertEquals("refCount: 0, 0, 0", mf.referenceCounts());
```
 
Object Pools
=================
There is String and Enum object pools to turn a CharSequence into a String.
```java
Bytes b = Bytes.from("Hello World");
b.readSkip(6);

StringInterner si = new StringInterner(128);
String s = si.intern(b);
String s2 = si.intern(b);
assertEquals("World", s);
assertSame(s, s2);
```

Class Local Caching
=================
Add caching of a data structure for each class using a lambda
```java
public static final ClassLocal<EnumInterner> ENUM_INTERNER = 
        ClassLocal.withInitial(c -> new EnumInterner<>(c));
        
E enumValue = ENUM_INTERNER.get(enumClass).intern(stringBuilder);
```

Maths Functions
=================
Maths functions to support rounds

```java
double a = 0.1;
double b = 0.3;
double c= Maths.round2(b - a); // 0.2 rounded to 2 decimal places
```

Checking type conversions

```java
int i = Maths.toInt32(longValue);
```

Serializable Lambdas
=================
There is a number of FunctionalInterfaces you can utilise as method arguments.
This allows implicitly making a lambda Serializable.

```java
// in KeyedVisitable
default <R> R applyToKey(K key, @NotNull SerializableFunction<E, R> function) {

// in code

String fullename = map.applyToKey("u:123223", u -> u.getFullName());
```

Histogram
=================
A high dynamic range histogram with tunable accuracy.

```java
Histogram h = new Histogram(32, 4);
long start = instance.ticks(), prev = start;
for (int i = 0; i <= 1000_000_000; i++) {
    long now = instance.ticks();
    long time = now - prev;
    h.sample(time);
    prev = now;
}
System.out.println(h.toLongMicrosFormat(instance::toMicros));
```