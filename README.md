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
Memory m = OS.memory();
try {
    long addr = m.allocate(1024);
    m.writeInt(0L, 1);
    boolean b = m.compareAndSwap(0L, 1, 2);
    assert b;
} finally {
    m.free(addr);
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
FileChanel fc = new RandomAccessFile(fileName, "rw").getFileChannel();
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

Maths Functions
=================

Serializable Lambdas
=================

Histogram
=================
