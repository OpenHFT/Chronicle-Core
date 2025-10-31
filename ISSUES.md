# SonarCloud Issues (branch `ea`)

Total open/confirmed issues: 565

## 1. AZo0trUtI-95tLi6OoTe

- **Rule**: `java:S1121`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/CleaningThreadLocal.java:166`
- **Effort**: 5min
- **Created**: 2025-10-20T13:23:39+0000
- **Assignee**: Unassigned
- **Message**:
  Extract the assignment out of this expression.

## 2. AZo0trndI-95tLi6OoTk

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:70`
- **Effort**: 1min
- **Created**: 2025-10-20T13:23:39+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this useless assignment to local variable "ctl".

## 3. AZo0trndI-95tLi6OoTl

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:70`
- **Effort**: 5min
- **Created**: 2025-10-20T13:23:39+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused "ctl" local variable.

## 4. AZo0trgcI-95tLi6OoTf

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:1278`
- **Effort**: 30min
- **Created**: 2025-10-20T08:55:56+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  This accessibility bypass should be removed.

## 5. AZo0trkBI-95tLi6OoTi

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/CleaningRandomAccessFileTest.java:63`
- **Effort**: 5min
- **Created**: 2025-10-20T08:55:56+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  This block of commented-out lines of code should be removed.

## 6. AZo0tro3I-95tLi6OoTm

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/GenericReflectionTest.java:89`
- **Effort**: 2min
- **Created**: 2025-07-28T08:25:46+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 7. AZfu9LkvMc6xYjPtL9uf

- **Rule**: `java:S2681`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/Wget.java:63`
- **Effort**: 5min
- **Created**: 2025-07-04T09:56:33+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  This statement will not be executed conditionally; only the first statement will be. The rest will
  execute unconditionally.

## 8. AZfu9LyCMc6xYjPtL9uk

- **Rule**: `java:S5778`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/LimitedInputStreamTest.java:33`
- **Effort**: 5min
- **Created**: 2025-07-04T09:56:33+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception.

## 9. AZfu9LvpMc6xYjPtL9ug

- **Rule**: `java:S5778`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/WgetTest.java:127`
- **Effort**: 5min
- **Created**: 2025-07-04T09:56:33+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception.

## 10. AZfu9LvpMc6xYjPtL9uh

- **Rule**: `java:S5778`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/WgetTest.java:178`
- **Effort**: 5min
- **Created**: 2025-07-04T09:56:33+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception.

## 11. AZfu9LvpMc6xYjPtL9ui

- **Rule**: `java:S5778`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/WgetTest.java:200`
- **Effort**: 5min
- **Created**: 2025-07-04T09:56:33+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception.

## 12. AZfu9LvpMc6xYjPtL9uj

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/WgetTest.java:213`
- **Effort**: 5min
- **Created**: 2025-07-04T09:56:33+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 13. AZfUXX-Lk0RInRIJB3jZ

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:891`
- **Effort**: 10min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Replace this use of System.err by a logger.

## 14. AZfUXX-Lk0RInRIJB3ja

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:1704`
- **Effort**: 5min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a private constructor to hide the implicit public one.

## 15. AZfUXX-Lk0RInRIJB3jb

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:1726`
- **Effort**: 5min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a private constructor to hide the implicit public one.

## 16. AZfUXX-2k0RInRIJB3je

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:567`
- **Effort**: 1min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "n".

## 17. AZfUXX-2k0RInRIJB3jf

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:567`
- **Effort**: 5min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "n" local variable.

## 18. AZfUXX-2k0RInRIJB3jg

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:771`
- **Effort**: 5min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a private constructor to hide the implicit public one.

## 19. AZfUXX-2k0RInRIJB3jh

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:775`
- **Effort**: 5min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a private constructor to hide the implicit public one.

## 20. AZfUXX-2k0RInRIJB3ji

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:794`
- **Effort**: 5min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a private constructor to hide the implicit public one.

## 21. AZfUXX-2k0RInRIJB3jj

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:801`
- **Effort**: 20min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 22. AZfUXX-2k0RInRIJB3jk

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:807`
- **Effort**: 5min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a private constructor to hide the implicit public one.

## 23. AZfUXX-2k0RInRIJB3jl

- **Rule**: `java:S1659`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:811`
- **Effort**: 2min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Declare "write0Mh2" on a separate line.

## 24. AZfUXX-2k0RInRIJB3jd

- **Rule**: `java:S1141`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:814`
- **Effort**: 20min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Extract this nested try block into a separate method.

## 25. AZfUXX-2k0RInRIJB3jm

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:821`
- **Effort**: 20min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 26. AZfUXX6Ak0RInRIJB3jY

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/ClassUtil.java:29`
- **Effort**: 5min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a private constructor to hide the implicit public one.

## 27. AZfUXYBGk0RInRIJB3jn

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/DirectBufferUtilTest.java:16`
- **Effort**: 2min
- **Created**: 2025-07-03T11:27:44+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 28. AZd9fhKOWO-0AlY2Aua1

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/Slf4jExceptionHandler.java:60`
- **Effort**: 20min
- **Created**: 2025-05-02T09:41:34+0000
- **Assignee**: keiran-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 29. AZd9fhKOWO-0AlY2Aua2

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/Slf4jExceptionHandler.java:61`
- **Effort**: 10min
- **Created**: 2025-05-02T09:41:34+0000
- **Assignee**: keiran-lawrey@github
- **Message**:
  Replace this use of System.err by a logger.

## 30. AZd9fhKOWO-0AlY2Aua3

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/Slf4jExceptionHandler.java:63`
- **Effort**: 10min
- **Created**: 2025-05-02T09:41:34+0000
- **Assignee**: keiran-lawrey@github
- **Message**:
  Replace this use of System.err by a logger.

## 31. AZd9fhKOWO-0AlY2Aua5

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/Slf4jExceptionHandler.java:74`
- **Effort**: 20min
- **Created**: 2025-05-02T09:41:34+0000
- **Assignee**: keiran-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 32. AZd9fhKOWO-0AlY2Aua6

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/Slf4jExceptionHandler.java:75`
- **Effort**: 10min
- **Created**: 2025-05-02T09:41:34+0000
- **Assignee**: keiran-lawrey@github
- **Message**:
  Replace this use of System.err by a logger.

## 33. AZd9fhKOWO-0AlY2Aua7

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/Slf4jExceptionHandler.java:77`
- **Effort**: 10min
- **Created**: 2025-05-02T09:41:34+0000
- **Assignee**: keiran-lawrey@github
- **Message**:
  Replace this use of System.err by a logger.

## 34. AZd9fhd5WO-0AlY2Aua9

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ExceptionHandlerFallbackTest.java:20`
- **Effort**: 10min
- **Created**: 2025-05-02T09:41:34+0000
- **Assignee**: keiran-lawrey@github
- **Message**:
  Add at least one assertion to this test case.

## 35. AZd9fheLWO-0AlY2Aua-

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/Slf4jExceptionHandlerTest.java:58`
- **Effort**: 10min
- **Created**: 2025-05-02T09:41:34+0000
- **Assignee**: keiran-lawrey@github
- **Message**:
  Add at least one assertion to this test case.

## 36. AZJrgNArR0DF1Q0VW8CE

- **Rule**: `java:S1220`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/ClassWithNoPackageTest.java`
- **Effort**: 10min
- **Created**: 2024-06-11T13:42:13+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Move this file to a named package.

## 37. AY_Fhec_QoTCYS6v6sy1

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/ObjectUtils.java:37`
- **Effort**: 1min
- **Created**: 2024-05-20T16:15:49+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'net.openhft.chronicle.core.Jvm.uncheckedCast'.

## 38. AZJrgM01R0DF1Q0VW8BG

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/CloseableUtilsTest.java:32`
- **Effort**: 5min
- **Created**: 2024-05-20T08:51:46+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 39. AY_Fhe7SQoTCYS6v6szj

- **Rule**: `java:S5778`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pom/PomPropertiesTest.java:19`
- **Effort**: 5min
- **Created**: 2024-04-30T08:03:05+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception.

## 40. AY_Fhe7SQoTCYS6v6szk

- **Rule**: `java:S5778`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pom/PomPropertiesTest.java:25`
- **Effort**: 5min
- **Created**: 2024-04-30T08:03:05+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception.

## 41. AY_FhenmQoTCYS6v6szE

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/pool/ParsingCache.java:19`
- **Effort**: 1min
- **Created**: 2024-04-29T11:26:13+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'net.openhft.chronicle.core.Jvm'.

## 42. AY2rh4pAJhrvgxSlk1ph

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Bootstrap.java:24`
- **Effort**: 5min
- **Created**: 2024-02-14T12:03:36+0000
- **Assignee**: Unassigned
- **Message**:
  Add a private constructor to hide the implicit public one.

## 43. AZJrgM60R0DF1Q0VW8BU

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:77`
- **Effort**: 5min
- **Created**: 2024-02-05T12:54:45+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "s" private field.

## 44. AZJrgM60R0DF1Q0VW8BV

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:85`
- **Effort**: 5min
- **Created**: 2024-02-05T12:54:45+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "s" private field.

## 45. AZJrgM60R0DF1Q0VW8BW

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:97`
- **Effort**: 5min
- **Created**: 2024-02-05T12:54:45+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "s" private field.

## 46. AZJrgM60R0DF1Q0VW8BX

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:108`
- **Effort**: 5min
- **Created**: 2024-02-05T12:54:45+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "s" private field.

## 47. AZal5MusETPomxzQiJvz

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/ChronicleInitTest.java:36`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 48. AZal5MusETPomxzQiJv0

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/ChronicleInitTest.java:41`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 49. AZJrgM9dR0DF1Q0VW8B3

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:431`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "somePrivateField" private field.

## 50. AY-YQ-HZRn1OMLrPtwqb

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryTest.java:166`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 49.

## 51. AY-YQ-HZRn1OMLrPtwqc

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryTest.java:185`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 49.

## 52. AY-YQ-HZRn1OMLrPtwqd

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryTest.java:203`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 49.

## 53. AY-YQ-HZRn1OMLrPtwqe

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryTest.java:220`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 49.

## 54. AY-YQ-HZRn1OMLrPtwqf

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryTest.java:237`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 49.

## 55. AY-YQ-HZRn1OMLrPtwqg

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryTest.java:257`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 49.

## 56. AY-YQ-GVRn1OMLrPtwqW

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CoolerTesterTest.java:27`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 57. AY0dUeHi1PIC7LhLgu5f

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CoolerTesterTest.java:27`
- **Effort**: 10min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 58. AY-YQ-GVRn1OMLrPtwqV

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CoolerTesterTest.java:32`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "tester".

## 59. AY-YQ-GVRn1OMLrPtwqX

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CoolerTesterTest.java:32`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "tester" local variable.

## 60. AY0dUeHW1PIC7LhLgu5e

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:8`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 61. AY0dUeHW1PIC7LhLgu5K

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:11`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 62. AY0dUeHW1PIC7LhLgu5L

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:16`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 63. AY0dUeHW1PIC7LhLgu5M

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:21`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 64. AY0dUeHW1PIC7LhLgu5N

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:26`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 65. AY0dUeHW1PIC7LhLgu5O

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:31`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 66. AY0dUeHW1PIC7LhLgu5P

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:36`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 67. AY0dUeHW1PIC7LhLgu5Q

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:41`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 68. AY0dUeHW1PIC7LhLgu5R

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:47`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 69. AY0dUeHW1PIC7LhLgu5S

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:52`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 70. AY0dUeHW1PIC7LhLgu5T

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:57`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 71. AY0dUeHW1PIC7LhLgu5U

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:62`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 72. AY0dUeHW1PIC7LhLgu5V

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:67`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 73. AY0dUeHW1PIC7LhLgu5W

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:72`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 74. AY0dUeHW1PIC7LhLgu5X

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:77`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 75. AY0dUeHW1PIC7LhLgu5Y

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:82`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 76. AY0dUeHW1PIC7LhLgu5Z

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:87`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 77. AY0dUeHW1PIC7LhLgu5a

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:92`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 78. AY0dUeHW1PIC7LhLgu5b

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:98`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 79. AY0dUeHW1PIC7LhLgu5c

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:103`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 80. AY0dUeHW1PIC7LhLgu5d

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cooler/CpuCoolersTest.java:108`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 81. AY0dUeBP1PIC7LhLgu3j

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/ReferenceCountedUtilsTest.java:11`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 82. AZal5MpOETPomxzQiJvp

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/ReferenceCountedUtilsTest.java:14`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 83. AZal5MpOETPomxzQiJvq

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/ReferenceCountedUtilsTest.java:19`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 84. AY0dUeBP1PIC7LhLgu3i

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/ReferenceCountedUtilsTest.java:24`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 85. AY0dUeAS1PIC7LhLgu3S

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/analytics/ReflectionUtilTest.java:10`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 86. AY0dUeAS1PIC7LhLgu3N

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/analytics/ReflectionUtilTest.java:13`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 87. AY0dUeAS1PIC7LhLgu3O

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/analytics/ReflectionUtilTest.java:20`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 88. AY0dUeAS1PIC7LhLgu3P

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/analytics/ReflectionUtilTest.java:27`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 89. AY0dUeAS1PIC7LhLgu3Q

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/analytics/ReflectionUtilTest.java:34`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 90. AY0dUeAS1PIC7LhLgu3R

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/analytics/ReflectionUtilTest.java:43`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 91. AY0dUeA51PIC7LhLgu3d

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/cleaner/Jdk9ByteBufferCleanerServiceTest.java:11`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 92. AZal5MmYETPomxzQiJvo

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/cleaner/Jdk9ByteBufferCleanerServiceTest.java:16`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 93. AY0dUeA41PIC7LhLgu3b

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/cleaner/Jdk9ByteBufferCleanerServiceTest.java:21`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 94. AY0dUeA51PIC7LhLgu3c

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/cleaner/Jdk9ByteBufferCleanerServiceTest.java:27`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 95. AY-YQ93IRn1OMLrPtwmp

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/cleaner/ReflectionBasedByteBufferCleanerServiceTest.java:3`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unnecessary import: same package classes are always implicitly imported.

## 96. AY0dUeBC1PIC7LhLgu3h

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/cleaner/ReflectionBasedByteBufferCleanerServiceTest.java:12`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 97. AY0dUeBC1PIC7LhLgu3e

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/cleaner/ReflectionBasedByteBufferCleanerServiceTest.java:18`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 98. AY0dUeBC1PIC7LhLgu3f

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/cleaner/ReflectionBasedByteBufferCleanerServiceTest.java:25`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 99. AY0dUeBC1PIC7LhLgu3g

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/cleaner/ReflectionBasedByteBufferCleanerServiceTest.java:31`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 100. AY0dUeAs1PIC7LhLgu3a

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/DirectBufferUtilTest.java:13`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 101. AY0dUeAs1PIC7LhLgu3V

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/DirectBufferUtilTest.java:21`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 102. AY0dUeAs1PIC7LhLgu3W

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/DirectBufferUtilTest.java:26`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 103. AY0dUeAs1PIC7LhLgu3X

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/DirectBufferUtilTest.java:34`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 104. AY0dUeAs1PIC7LhLgu3Y

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/DirectBufferUtilTest.java:41`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 105. AY0dUeAs1PIC7LhLgu3Z

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/DirectBufferUtilTest.java:48`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 106. AY0dUeAj1PIC7LhLgu3U

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/NopThreadConfinementAsserterTest.java:7`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 107. AY0dUeAj1PIC7LhLgu3T

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/NopThreadConfinementAsserterTest.java:10`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 108. AY-YQ92uRn1OMLrPtwmm

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/VanillaThreadConfinementAsserterTest.java:55`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "asserter" which hides the field declared at line 29.

## 109. AY-YQ92uRn1OMLrPtwmn

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/VanillaThreadConfinementAsserterTest.java:61`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "asserter" which hides the field declared at line 29.

## 110. AY-YQ92uRn1OMLrPtwmo

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/util/VanillaThreadConfinementAsserterTest.java:74`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "asserter" which hides the field declared at line 29.

## 111. AZo0trkBI-95tLi6OoTh

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/CleaningRandomAccessFileTest.java:39`
- **Effort**: 10min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add at least one assertion to this test case.

## 112. AY-YQ93zRn1OMLrPtwm2

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IORuntimeExceptionTest.java:3`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unnecessary import: same package classes are always implicitly imported.

## 113. AY0dUeBY1PIC7LhLgu3o

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IORuntimeExceptionTest.java:8`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 114. AY0dUeBY1PIC7LhLgu3k

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IORuntimeExceptionTest.java:11`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 115. AY0dUeBY1PIC7LhLgu3l

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IORuntimeExceptionTest.java:19`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 116. AY0dUeBY1PIC7LhLgu3m

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IORuntimeExceptionTest.java:27`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 117. AY0dUeBY1PIC7LhLgu3n

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IORuntimeExceptionTest.java:37`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 118. AY-YQ96pRn1OMLrPtwnI

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ManagedCloseableTest.java:4`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unnecessary import: same package classes are always implicitly imported.

## 119. AY-YQ96pRn1OMLrPtwnJ

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ManagedCloseableTest.java:5`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.junit.Before'.

## 120. AY-YQ96pRn1OMLrPtwnK

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ManagedCloseableTest.java:11`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.junit.Assume.assumeTrue'.

## 121. AY0dUeDL1PIC7LhLgu4F

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ManagedCloseableTest.java:15`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 122. AZal5MqzETPomxzQiJvt

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ManagedCloseableTest.java:17`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 123. AY0dUeDL1PIC7LhLgu4C

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ManagedCloseableTest.java:21`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 124. AY0dUeDL1PIC7LhLgu4D

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ManagedCloseableTest.java:32`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 125. AY0dUeDL1PIC7LhLgu4E

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ManagedCloseableTest.java:42`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 126. AY0dUeDC1PIC7LhLgu4B

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ReferenceChangeListenerTest.java:6`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 127. AY0dUeDC1PIC7LhLgu3-

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ReferenceChangeListenerTest.java:9`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 128. AY0dUeDC1PIC7LhLgu3_

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ReferenceChangeListenerTest.java:20`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 129. AY0dUeDC1PIC7LhLgu4A

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ReferenceChangeListenerTest.java:31`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 130. AY-YQ95pRn1OMLrPtwnG

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/SimpleCloseableTest.java:3`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unnecessary import: same package classes are always implicitly imported.

## 131. AY-YQ95pRn1OMLrPtwnH

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/SimpleCloseableTest.java:6`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.mockito.Mockito'.

## 132. AY0dUeCG1PIC7LhLgu3w

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/SimpleCloseableTest.java:8`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 133. AY0dUeCG1PIC7LhLgu3u

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/SimpleCloseableTest.java:27`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 134. AY0dUeCG1PIC7LhLgu3v

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/SimpleCloseableTest.java:40`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 135. AY0dUeCS1PIC7LhLgu30

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/SyncableTest.java:7`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 136. AY0dUeCS1PIC7LhLgu3x

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/SyncableTest.java:19`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 137. AY0dUeCS1PIC7LhLgu3y

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/SyncableTest.java:27`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 138. AY0dUeCS1PIC7LhLgu3z

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/SyncableTest.java:34`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 139. AY0dUeCf1PIC7LhLgu32

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ThreadingIllegalStateExceptionTest.java:6`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 140. AY0dUeCf1PIC7LhLgu31

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ThreadingIllegalStateExceptionTest.java:9`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 141. AY0dUeC51PIC7LhLgu39

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ValidatableUtilTest.java:7`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 142. AY0dUeC51PIC7LhLgu36

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ValidatableUtilTest.java:10`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 143. AY0dUeC51PIC7LhLgu37

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ValidatableUtilTest.java:21`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 144. AY0dUeC51PIC7LhLgu38

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/ValidatableUtilTest.java:27`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 145. AY-YQ97XRn1OMLrPtwnO

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ChainedExceptionHandlerTest.java:3`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'net.openhft.chronicle.core.util.IgnoresEverything'.

## 146. AY-YQ97XRn1OMLrPtwnP

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ChainedExceptionHandlerTest.java:7`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.slf4j.Logger'.

## 147. AY-YQ97XRn1OMLrPtwnQ

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ChainedExceptionHandlerTest.java:9`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.junit.jupiter.api.Assertions'.

## 148. AY0dUeDj1PIC7LhLgu4L

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ChainedExceptionHandlerTest.java:51`
- **Effort**: 10min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 149. AY-YQ965Rn1OMLrPtwnL

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ExceptionHandlerTest.java:25`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.junit.jupiter.api.Assumptions'.

## 150. AY-YQ965Rn1OMLrPtwnM

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ExceptionHandlerTest.java:26`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.junit.jupiter.api.BeforeEach'.

## 151. AY-YQ97ERn1OMLrPtwnN

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/RecordingExceptionHandlerTest.java:3`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unnecessary import: same package classes are always implicitly imported.

## 152. AY0dUeDW1PIC7LhLgu4K

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/RecordingExceptionHandlerTest.java:11`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 153. AZal5MpgETPomxzQiJvr

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/RecordingExceptionHandlerTest.java:19`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 154. AY0dUeDW1PIC7LhLgu4G

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/RecordingExceptionHandlerTest.java:27`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 155. AY0dUeDW1PIC7LhLgu4H

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/RecordingExceptionHandlerTest.java:37`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 156. AY0dUeDW1PIC7LhLgu4I

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/RecordingExceptionHandlerTest.java:49`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 157. AY0dUeDW1PIC7LhLgu4J

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/RecordingExceptionHandlerTest.java:58`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 158. AY0dUeDp1PIC7LhLgu4O

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ThreadLocalisedExceptionHandlerTest.java:7`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 159. AZal5Mp_ETPomxzQiJvs

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ThreadLocalisedExceptionHandlerTest.java:13`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 160. AY0dUeDp1PIC7LhLgu4M

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ThreadLocalisedExceptionHandlerTest.java:19`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 161. AY0dUeDp1PIC7LhLgu4N

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/onoes/ThreadLocalisedExceptionHandlerTest.java:24`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 162. AY-YQ-HpRn1OMLrPtwqh

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pom/PomPropertiesTest.java:4`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'java.util.Properties'.

## 163. AY0dUeGX1PIC7LhLgu5F

- **Rule**: `java:S5778`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/ClassLookupTest.java:35`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception.

## 164. AZJrgM85R0DF1Q0VW8Bv

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/shutdown/HookletTest.java:29`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 165. AZJrgM85R0DF1Q0VW8Bw

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/shutdown/HookletTest.java:57`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 166. AY0dUeG81PIC7LhLgu5J

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/shutdown/PriorityHookTest.java:9`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 167. AY0dUeG81PIC7LhLgu5G

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/shutdown/PriorityHookTest.java:12`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 168. AY0dUeG81PIC7LhLgu5H

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/shutdown/PriorityHookTest.java:22`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 169. AY0dUeG81PIC7LhLgu5I

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/shutdown/PriorityHookTest.java:36`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 170. AY0dUeEB1PIC7LhLgu4h

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:13`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 171. AY0dUeEB1PIC7LhLgu4b

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:16`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 172. AY0dUeEB1PIC7LhLgu4c

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:26`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 173. AY0dUeEB1PIC7LhLgu4d

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:34`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 174. AY0dUeEB1PIC7LhLgu4Z

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:49`
- **Effort**: 10min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 175. AY0dUeEB1PIC7LhLgu4e

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:49`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 176. AY0dUeEB1PIC7LhLgu4a

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:65`
- **Effort**: 10min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 177. AY0dUeEB1PIC7LhLgu4f

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:65`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 178. AY0dUeEB1PIC7LhLgu4g

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:74`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 179. AY-YQ98ARn1OMLrPtwnU

- **Rule**: `java:S1611`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/CleaningThreadLocalTest.java:75`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove the parentheses around the "value" parameter

## 180. AZal5MrcETPomxzQiJvu

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventHandlerTest.java:16`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 181. AY0dUeD11PIC7LhLgu4Y

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:7`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 182. AZal5MrwETPomxzQiJvw

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:13`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 183. AY0dUeD11PIC7LhLgu4P

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:19`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 184. AY0dUeD11PIC7LhLgu4Q

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:25`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 185. AY0dUeD11PIC7LhLgu4R

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:32`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 186. AY0dUeD11PIC7LhLgu4S

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:39`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 187. AY0dUeD11PIC7LhLgu4T

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:46`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 188. AY0dUeD11PIC7LhLgu4U

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:53`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 189. AY0dUeD11PIC7LhLgu4V

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:59`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 190. AY-YQ97wRn1OMLrPtwnR

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:65`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 191. AY0dUeD11PIC7LhLgu4W

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:65`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 192. AY0dUeD11PIC7LhLgu4X

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/EventLoopTest.java:72`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 193. AY0dUeEJ1PIC7LhLgu4k

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/HandlerPriorityTest.java:6`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 194. AY0dUeEJ1PIC7LhLgu4i

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/HandlerPriorityTest.java:9`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 195. AY0dUeEJ1PIC7LhLgu4j

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/HandlerPriorityTest.java:16`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 196. AY0dUeEe1PIC7LhLgu4t

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/ThreadDumpTest.java:9`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 197. AY0dUeEe1PIC7LhLgu4s

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/ThreadDumpTest.java:19`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 198. AY-YQ98gRn1OMLrPtwnV

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/ThreadLocalHelperTest.java:6`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'java.util.function.Consumer'.

## 199. AY-YQ99KRn1OMLrPtwnf

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:28`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.mockito.Mockito'.

## 200. AY-YQ99KRn1OMLrPtwng

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:33`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.mockito.ArgumentMatchers.any'.

## 201. AZJrgM5ER0DF1Q0VW8BR

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:49`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "handler" private field.

## 202. AZJrgM5ER0DF1Q0VW8BS

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:50`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "exampleMethod" private field.

## 203. AZal5MsJETPomxzQiJvx

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:53`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 204. AY-YQ99KRn1OMLrPtwnc

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:60`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "handler" which hides the field declared at line 49.

## 205. AY-YQ99KRn1OMLrPtwnd

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:73`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "handler" which hides the field declared at line 49.

## 206. AY-YQ99KRn1OMLrPtwne

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:74`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "exampleMethod" which hides the field declared at line 50.

## 207. AY0dUeFq1PIC7LhLgu48

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/BuilderTest.java:31`
- **Effort**: 10min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 208. AY-YQ-AIRn1OMLrPtwpR

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/BuilderTest.java:33`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "instanceFromGet".

## 209. AY-YQ-AIRn1OMLrPtwpS

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/BuilderTest.java:33`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "instanceFromGet" local variable.

## 210. AY-YQ-AIRn1OMLrPtwpQ

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/BuilderTest.java:34`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "instanceFromBuild".

## 211. AY-YQ-AIRn1OMLrPtwpT

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/BuilderTest.java:34`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "instanceFromBuild" local variable.

## 212. AY0dUeFU1PIC7LhLgu46

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ClassMetricsTest.java:6`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 213. AY0dUeFU1PIC7LhLgu43

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ClassMetricsTest.java:9`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 214. AY0dUeFU1PIC7LhLgu44

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ClassMetricsTest.java:19`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 215. AY0dUeFU1PIC7LhLgu45

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ClassMetricsTest.java:32`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 216. AY-YQ9-bRn1OMLrPtwnp

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ClassNotFoundRuntimeExceptionTest.java:3`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unnecessary import: same package classes are always implicitly imported.

## 217. AY0dUeFJ1PIC7LhLgu42

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ClassNotFoundRuntimeExceptionTest.java:7`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 218. AY0dUeFJ1PIC7LhLgu40

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ClassNotFoundRuntimeExceptionTest.java:10`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 219. AY0dUeFJ1PIC7LhLgu41

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ClassNotFoundRuntimeExceptionTest.java:19`
- **Effort**: 2min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 220. AY-YQ99xRn1OMLrPtwni

- **Rule**: `java:S4144`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/CompilerUtilsTest.java:20`
- **Effort**: 15min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Update this method so that its implementation is not identical to
  "defineClassShouldThrowAssertionErrorForIllegalAccessException" on line 10.

## 221. AY-YQ9-RRn1OMLrPtwnl

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/HistogramTest.java:47`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "bucket".

## 222. AY-YQ9-RRn1OMLrPtwnm

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/HistogramTest.java:47`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "bucket" local variable.

## 223. AY-YQ9-RRn1OMLrPtwnn

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/HistogramTest.java:69`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "percentiles".

## 224. AY-YQ9-RRn1OMLrPtwno

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/HistogramTest.java:69`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "percentiles" local variable.

## 225. AY0dUeEy1PIC7LhLgu4w

- **Rule**: `java:S1607`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/InvocationTargetRuntimeExceptionTest.java:34`
- **Effort**: 10min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Either add an explanation about why this test is skipped or remove the "@Ignore" annotation.

## 226. AY-YQ-AwRn1OMLrPtwpW

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:151`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "map".

## 227. AY-YQ-AwRn1OMLrPtwpX

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:151`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "map" local variable.

## 228. AY-YQ-AwRn1OMLrPtwpY

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:290`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "interfaces".

## 229. AY-YQ-AwRn1OMLrPtwpZ

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:290`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "interfaces" local variable.

## 230. AY-YQ-AwRn1OMLrPtwpa

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:296`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "interfaces".

## 231. AY-YQ-AwRn1OMLrPtwpb

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:296`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "interfaces" local variable.

## 232. AY-YQ9-ARn1OMLrPtwnk

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/SimpleCleanerTest.java:3`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unnecessary import: same package classes are always implicitly imported.

## 233. AY-YQ9-ARn1OMLrPtwnj

- **Rule**: `java:S4144`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/SimpleCleanerTest.java:23`
- **Effort**: 15min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Update this method so that its implementation is not identical to "cleanShouldExecuteRunnableOnce"
  on line 12.

## 234. AY0dUeFd1PIC7LhLgu47

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ThreadConfinementAsserterTest.java:38`
- **Effort**: 10min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 235. AY-YQ9_iRn1OMLrPtwpH

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ThreadConfinementAsserterTest.java:40`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "asserter".

## 236. AY-YQ9_iRn1OMLrPtwpI

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ThreadConfinementAsserterTest.java:40`
- **Effort**: 5min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "asserter" local variable.

## 237. AY-YQ99cRn1OMLrPtwnh

- **Rule**: `java:S4144`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/UnresolvedTypeTest.java:28`
- **Effort**: 15min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Update this method so that its implementation is not identical to
  "constructorShouldInitializeTypeName" on line 11.

## 238. AY-YQ-FKRn1OMLrPtwp-

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/values/IntValueTest.java:5`
- **Effort**: 1min
- **Created**: 2024-01-05T17:59:38+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused import 'org.mockito.Mockito'.

## 239. AYy-JA3a7ukn4P0HNdXe

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/ChronicleGuarding.java:18`
- **Effort**: 5min
- **Created**: 2023-12-29T16:58:30+0000
- **Assignee**: Unassigned
- **Message**:
  Add a private constructor to hide the implicit public one.

## 240. AYy-JA3a7ukn4P0HNdXd

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/ChronicleGuarding.java:21`
- **Effort**: 5min
- **Created**: 2023-12-29T16:58:30+0000
- **Assignee**: Unassigned
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 241. AY0dUd3Y1PIC7LhLgu1s

- **Rule**: `java:S1452`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/ObjectUtils.java:344`
- **Effort**: 20min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Remove usage of generic wildcard type.

## 242. AY0dUeHz1PIC7LhLgu5i

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:379`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 243. AY0dUeHz1PIC7LhLgu5j

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:384`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 244. AY0dUeHz1PIC7LhLgu5k

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:389`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 245. AY0dUeHz1PIC7LhLgu5l

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:394`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 246. AY0dUeHz1PIC7LhLgu5m

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:418`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 247. AY0dUeIQ1PIC7LhLgu5p

- **Rule**: `java:S3415`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:371`
- **Effort**: 2min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Swap these 2 arguments so they are in the correct order: expected value, actual value.

## 248. AY0dUeB61PIC7LhLgu3t

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IOToolsTest.java:82`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 249. AY0dUeFA1PIC7LhLgu4x

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/HistogramTest.java:45`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 250. AY0dUeFA1PIC7LhLgu4y

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/HistogramTest.java:51`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 251. AY0dUeFA1PIC7LhLgu4z

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/HistogramTest.java:67`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 252. AY0dUeF31PIC7LhLgu5C

- **Rule**: `java:S5778`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:136`
- **Effort**: 5min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception.

## 253. AY0dUeF31PIC7LhLgu49

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:142`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 254. AY0dUeF31PIC7LhLgu4_

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:149`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 255. AY0dUeF31PIC7LhLgu5A

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:289`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 256. AY0dUeF31PIC7LhLgu5B

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsTest.java:295`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 257. AY0dUeEq1PIC7LhLgu4u

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/RecordingHistogramTest.java:39`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 258. AY0dUeEq1PIC7LhLgu4v

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/RecordingHistogramTest.java:49`
- **Effort**: 10min
- **Created**: 2023-12-28T20:00:15+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 259. AYw0smM2PW4UtZt2A8DD

- **Rule**: `java:S3008`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:115`
- **Effort**: 2min
- **Created**: 2023-11-17T15:08:54+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename this field "s_blackHole" to match the regular expression '^[a-z][a-zA-Z0-9]*$'.

## 260. AYw0smIhPW4UtZt2A8C-

- **Rule**: `java:S1659`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/Bootstrap.java:43`
- **Effort**: 2min
- **Created**: 2023-11-03T13:38:16+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Declare "IS_JAVA_12_PLUS" and all following declarations on a separate line.

## 261. AYw0smIhPW4UtZt2A8C_

- **Rule**: `java:S1659`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/Bootstrap.java:44`
- **Effort**: 2min
- **Created**: 2023-11-03T13:38:16+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Declare "IS_JAVA_20_PLUS" and all following declarations on a separate line.

## 262. AY-YQ93VRn1OMLrPtwmq

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/CpuClassTest.java:32`
- **Effort**: 0min
- **Created**: 2023-10-30T04:29:39+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Complete the task associated to this TODO comment.

## 263. AYtnKNjI7YSAYjAl0pPl

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:171`
- **Effort**: 5min
- **Created**: 2023-10-18T12:37:41+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 264. AYtnKNjI7YSAYjAl0pPm

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:175`
- **Effort**: 5min
- **Created**: 2023-10-18T12:37:41+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 265. AYtnKNj87YSAYjAl0pPn

- **Rule**: `java:S1192`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:60`
- **Effort**: 10min
- **Created**: 2023-10-18T12:05:21+0000
- **Assignee**: Unassigned
- **Message**:
  Define a constant instead of duplicating this literal "sun.nio.ch.FileDispatcherImpl" 4 times.

## 266. AYou-kIwCv0VIUZRFJpL

- **Rule**: `java:S3008`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:112`
- **Effort**: 2min
- **Created**: 2023-08-25T11:57:25+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename this field "RESOURCE_TRACING" to match the regular expression '^[a-z][a-zA-Z0-9]*$'.

## 267. AY6GxEAVV8YPjYp1PvGw

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/TracingReferenceCounted.java:220`
- **Effort**: 20min
- **Created**: 2023-08-22T17:00:46+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 268. AY6GxEAVV8YPjYp1PvGx

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/TracingReferenceCounted.java:226`
- **Effort**: 20min
- **Created**: 2023-08-22T17:00:46+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 269. AY6GxEAVV8YPjYp1PvGy

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/TracingReferenceCounted.java:236`
- **Effort**: 20min
- **Created**: 2023-08-22T17:00:46+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 270. AZJrgM2YR0DF1Q0VW8BK

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCountedTest.java:104`
- **Effort**: 5min
- **Created**: 2023-08-22T17:00:46+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add the "@Override" annotation above this method signature

## 271. AZJrgM2YR0DF1Q0VW8BL

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCountedTest.java:110`
- **Effort**: 5min
- **Created**: 2023-08-22T17:00:46+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add the "@Override" annotation above this method signature

## 272. AYonsH5ph0UlFPWPHUmI

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCountedTest.java:104`
- **Effort**: 10min
- **Created**: 2023-08-22T16:54:50+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add at least one assertion to this test case.

## 273. AYonsH5ph0UlFPWPHUmJ

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCountedTest.java:110`
- **Effort**: 10min
- **Created**: 2023-08-22T16:54:50+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add at least one assertion to this test case.

## 274. AYonsHnwh0UlFPWPHUmF

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/AnnotationFinder.java:28`
- **Effort**: 5min
- **Created**: 2023-08-22T16:40:36+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a private constructor to hide the implicit public one.

## 275. AZJrgM9dR0DF1Q0VW8B1

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:471`
- **Effort**: 5min
- **Created**: 2023-08-22T16:40:36+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 276. AZJrgM9dR0DF1Q0VW8B2

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:476`
- **Effort**: 5min
- **Created**: 2023-08-22T16:40:36+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 277. AYne2wHRuA1VLDVEVVWp

- **Rule**: `java:S3776`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/ThreadDump.java:118`
- **Effort**: 15min
- **Created**: 2023-08-09T14:00:22+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Refactor this method to reduce its Cognitive Complexity from 25 to the 15 allowed.

## 278. AYne2wZVuA1VLDVEVVWu

- **Rule**: `java:S3776`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:140`
- **Effort**: 12min
- **Created**: 2023-08-02T08:50:49+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Refactor this method to reduce its Cognitive Complexity from 22 to the 15 allowed.

## 279. AYne2wZVuA1VLDVEVVWs

- **Rule**: `java:S135`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:156`
- **Effort**: 20min
- **Created**: 2023-08-02T08:50:49+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Reduce the total number of break and continue statements in this loop to use at most one.

## 280. AYne2wHRuA1VLDVEVVWo

- **Rule**: `java:S3014`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/ThreadDump.java:154`
- **Effort**: 45min
- **Created**: 2023-08-02T08:50:49+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this use of "ThreadGroup". Prefer the use of "ThreadPoolExecutor".

## 281. AYne2wHRuA1VLDVEVVWq

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/ThreadDump.java:156`
- **Effort**: 5min
- **Created**: 2023-08-02T08:50:49+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "threads" which hides the field declared at line 42.

## 282. AYne2wHRuA1VLDVEVVWr

- **Rule**: `java:S1126`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/ThreadDump.java:169`
- **Effort**: 2min
- **Created**: 2023-08-02T08:50:49+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Replace this if-then-else statement by a single return statement.

## 283. AZal5MsJETPomxzQiJvy

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:47`
- **Effort**: 2min
- **Created**: 2023-08-02T08:50:49+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this 'public' modifier.

## 284. AYl3WNSJFrX5SvpyywWz

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/ManagedCloseable.java:44`
- **Effort**: 0min
- **Created**: 2023-07-10T10:27:04+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Complete the task associated to this TODO comment.

## 285. AYl3WNSJFrX5SvpyywW0

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/ManagedCloseable.java:79`
- **Effort**: 0min
- **Created**: 2023-07-10T10:27:04+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Complete the task associated to this TODO comment.

## 286. AYl3WNRAFrX5SvpyywWx

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/ReferenceCounted.java:73`
- **Effort**: 0min
- **Created**: 2023-07-10T10:27:04+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Complete the task associated to this TODO comment.

## 287. AYl3WNRuFrX5SvpyywWy

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/ReferenceCountedTracer.java:56`
- **Effort**: 0min
- **Created**: 2023-07-10T10:27:04+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Complete the task associated to this TODO comment.

## 288. AYkeFzD6Ber657K2Xm3_

- **Rule**: `java:S1113`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java:444`
- **Effort**: 20min
- **Created**: 2023-06-30T12:41:35+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Do not override the Object.finalize() method.

## 289. AYjj_zDDofuPqSq8nRcX

- **Rule**: `java:S1172`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/LicenceCheck.java:75`
- **Effort**: 5min
- **Created**: 2023-06-20T10:10:00+0000
- **Assignee**: JerryShea@github
- **Message**:
  Remove this unused method parameter "caller".

## 290. AY6GxEFyV8YPjYp1PvG9

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:124`
- **Effort**: 20min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Error.

## 291. AYjj_zA1ofuPqSq8nRcC

- **Rule**: `java:S1121`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/Bootstrap.java:67`
- **Effort**: 5min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Extract the assignment out of this expression.

## 292. AYjj_zA1ofuPqSq8nRcD

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/Bootstrap.java:141`
- **Effort**: 10min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.err by a logger.

## 293. AYjj_zA1ofuPqSq8nRcE

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/Bootstrap.java:184`
- **Effort**: 10min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.err by a logger.

## 294. AYjj_zCaofuPqSq8nRcW

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/ClassUtil.java:102`
- **Effort**: 30min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility update should be removed.

## 295. AYjj_zB7ofuPqSq8nRcO

- **Rule**: `java:S1113`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:107`
- **Effort**: 20min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Do not override the Object.finalize() method.

## 296. AYjj_zB7ofuPqSq8nRcQ

- **Rule**: `java:S1215`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:116`
- **Effort**: 30min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Don't try to be smarter than the JVM, remove this call to run the garbage collector.

## 297. AYne2wZVuA1VLDVEVVWt

- **Rule**: `java:S3516`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:140`
- **Effort**: 4min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Refactor this method to not always return the same value.

## 298. AYjj_zB7ofuPqSq8nRcR

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:146`
- **Effort**: 10min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.err by a logger.

## 299. AYjj_zB7ofuPqSq8nRcS

- **Rule**: `java:S1119`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:150`
- **Effort**: 30min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Refactor the code to remove this label and the need for it.

## 300. AYjj_zB7ofuPqSq8nRcT

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:196`
- **Effort**: 10min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.err by a logger.

## 301. AY-YQ9n8Rn1OMLrPtwmg

- **Rule**: `java:S4838`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:234`
- **Effort**: 1min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Change "Closeable" to the type handled by the Collection.

## 302. AYjj_zB7ofuPqSq8nRcU

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:263`
- **Effort**: 30min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility update should be removed.

## 303. AYjj_zB7ofuPqSq8nRcV

- **Rule**: `java:S3776`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:314`
- **Effort**: 9min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Refactor this method to reduce its Cognitive Complexity from 19 to the 15 allowed.

## 304. AY6GxD2MV8YPjYp1PvGq

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:334`
- **Effort**: 20min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 305. AY6GxD2MV8YPjYp1PvGr

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CloseableUtils.java:341`
- **Effort**: 20min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 306. AYjj_zBWofuPqSq8nRcF

- **Rule**: `java:S1141`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CpuClass.java:57`
- **Effort**: 20min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested try block into a separate method.

## 307. AYjj_zBWofuPqSq8nRcH

- **Rule**: `java:S2629`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CpuClass.java:60`
- **Effort**: 5min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Use the built-in formatting to construct this argument.

## 308. AYjj_zBWofuPqSq8nRcJ

- **Rule**: `java:S3457`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CpuClass.java:60`
- **Effort**: 1min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Format specifiers should be used instead of string concatenation.

## 309. AYjj_zBWofuPqSq8nRcG

- **Rule**: `java:S1141`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CpuClass.java:81`
- **Effort**: 20min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested try block into a separate method.

## 310. AYjj_zBWofuPqSq8nRcI

- **Rule**: `java:S2629`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CpuClass.java:84`
- **Effort**: 5min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Use the built-in formatting to construct this argument.

## 311. AYjj_zBWofuPqSq8nRcK

- **Rule**: `java:S3457`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/CpuClass.java:84`
- **Effort**: 1min
- **Created**: 2023-06-20T07:43:13+0000
- **Assignee**: Unassigned
- **Message**:
  Format specifiers should be used instead of string concatenation.

## 312. AY-YQ94NRn1OMLrPtwm5

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/CloseableTest.java:91`
- **Effort**: 5min
- **Created**: 2023-06-08T11:40:46+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  This block of commented-out lines of code should be removed.

## 313. AYicajmbpYIB9vYWC7kW

- **Rule**: `java:S2699`
- **Severity**: BLOCKER
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/CloseableTest.java:87`
- **Effort**: 10min
- **Created**: 2023-06-08T10:56:59+0000
- **Assignee**: Unassigned
- **Message**:
  Add at least one assertion to this test case.

## 314. AY6GxECgV8YPjYp1PvG2

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/ChainedExceptionHandler.java:82`
- **Effort**: 20min
- **Created**: 2023-05-05T13:51:03+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 315. AY6GxECgV8YPjYp1PvG3

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/ChainedExceptionHandler.java:101`
- **Effort**: 20min
- **Created**: 2023-05-05T13:51:03+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 316. AYfARPFYvldWr60ea5AP

- **Rule**: `java:S1488`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:101`
- **Effort**: 2min
- **Created**: 2023-04-27T01:12:09+0000
- **Assignee**: Unassigned
- **Message**:
  Immediately return this expression instead of assigning it to the temporary variable "clazz".

## 317. AYfARO0rvldWr60ea5AL

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/ThreadLocalisedExceptionHandler.java:92`
- **Effort**: 5min
- **Created**: 2023-04-27T01:12:09+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Add the "@Override" annotation above this method signature

## 318. AYdInyEyL6sMsoM6j2B8

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/ValidatableUtil.java:23`
- **Effort**: 5min
- **Created**: 2023-03-23T11:56:13+0000
- **Assignee**: Unassigned
- **Message**:
  Add a private constructor to hide the implicit public one.

## 319. AY6GxECNV8YPjYp1PvG0

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/ExceptionHandler.java:83`
- **Effort**: 20min
- **Created**: 2023-01-11T11:36:06+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 320. AY6GxECNV8YPjYp1PvG1

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/onoes/ExceptionHandler.java:87`
- **Effort**: 20min
- **Created**: 2023-01-11T11:36:06+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 321. AYJoSpKpBfdiaV3EkN-L

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/IOTools.java:512`
- **Effort**: 10min
- **Created**: 2022-08-04T09:57:56+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.out by a logger.

## 322. AZJrgM8xR0DF1Q0VW8Bu

- **Rule**: `java:S1220`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/PerformanceTuningTest.java`
- **Effort**: 10min
- **Created**: 2022-08-03T13:27:53+0000
- **Assignee**: Unassigned
- **Message**:
  Move this file to a named package.

## 323. AZJrgM-vR0DF1Q0VW8B7

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryTest.java:48`
- **Effort**: 5min
- **Created**: 2022-07-29T10:48:57+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "name" private field.

## 324. AY-YQ94sRn1OMLrPtwm6

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IOToolsTest.java:293`
- **Effort**: 5min
- **Created**: 2022-07-28T09:21:09+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 325. AY-YQ94sRn1OMLrPtwm7

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IOToolsTest.java:328`
- **Effort**: 5min
- **Created**: 2022-07-28T09:21:09+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 326. AY-YQ94sRn1OMLrPtwm8

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IOToolsTest.java:358`
- **Effort**: 5min
- **Created**: 2022-07-28T09:21:09+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 327. AY-YQ94sRn1OMLrPtwm9

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/IOToolsTest.java:394`
- **Effort**: 5min
- **Created**: 2022-07-28T09:21:09+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 328. AYGwLDKEVfY0kn3-ZGHP

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/GenericReflectionTest.java:66`
- **Effort**: 2min
- **Created**: 2022-06-29T15:54:36+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 329. AYGwFu2Cs3HCEZt8Ott9

- **Rule**: `java:S2160`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/RecordingHistogram.java:34`
- **Effort**: 30min
- **Created**: 2022-06-23T08:58:28+0000
- **Assignee**: Unassigned
- **Message**:
  Override the "equals" method in this class.

## 330. AYGwFu2Cs3HCEZt8Ott-

- **Rule**: `java:S1450`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/RecordingHistogram.java:38`
- **Effort**: 5min
- **Created**: 2022-06-23T08:58:28+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "sampleCount" field and declare it as a local variable in the relevant methods.

## 331. AYGwFu2Cs3HCEZt8Ott_

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/RecordingHistogram.java:38`
- **Effort**: 5min
- **Created**: 2022-06-23T08:58:28+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused "sampleCount" private field.

## 332. AYGwFu2Cs3HCEZt8Ott8

- **Rule**: `java:S2184`
- **Severity**: MINOR
- **Type**: BUG
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/RecordingHistogram.java:164`
- **Effort**: 5min
- **Created**: 2022-06-23T08:58:28+0000
- **Assignee**: keiran-lawrey@github
- **Message**:
  Cast one of the operands of this subtraction operation to a "double".

## 333. AYFI6L9f8rtuHYNlamZA

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java:68`
- **Effort**: 2min
- **Created**: 2022-06-09T14:40:00+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 334. AYFI6L548rtuHYNlamY7

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractReferenceCounted.java:51`
- **Effort**: 2min
- **Created**: 2022-06-09T14:40:00+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 335. AY_Fhe97QoTCYS6v6szm

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryIntTest.java:19`
- **Effort**: 1min
- **Created**: 2022-06-07T10:22:28+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused import 'net.openhft.chronicle.core.util.Ints'.

## 336. AY_FhetYQoTCYS6v6szH

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryLongTest.java:19`
- **Effort**: 1min
- **Created**: 2022-06-07T10:22:28+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused import 'net.openhft.chronicle.core.util.Ints'.

## 337. AY_Fhe8AQoTCYS6v6szl

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryShortTest.java:19`
- **Effort**: 1min
- **Created**: 2022-06-07T10:22:28+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused import 'net.openhft.chronicle.core.util.Ints'.

## 338. AZJrgM-HR0DF1Q0VW8B4

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryTestMixin.java:146`
- **Effort**: 5min
- **Created**: 2022-06-07T10:22:28+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 339. AZJrgM-HR0DF1Q0VW8B5

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemoryTestMixin.java:202`
- **Effort**: 5min
- **Created**: 2022-06-07T10:22:28+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 340. AYCUJruIP08HSLlbzoWO

- **Rule**: `java:S4032`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/domestic/package-info.java`
- **Effort**: 2min
- **Created**: 2022-05-05T12:05:22+0000
- **Assignee**: minborg@github
- **Message**:
  Remove this package.

## 341. AX_8eirYIMHMA8dh4JbQ

- **Rule**: `java:S5778`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/ClassAliasPoolTest.java:132`
- **Effort**: 5min
- **Created**: 2022-04-06T00:53:30+0000
- **Assignee**: nicktindall@github
- **Message**:
  Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception.

## 342. AZMw6T_d3s4tVU50tJs2

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/Longs.java:19`
- **Effort**: 1min
- **Created**: 2022-04-01T14:25:05+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused import 'net.openhft.chronicle.assertions.AssertUtil'.

## 343. AX-h0PEE8MRnigXE9oV5

- **Rule**: `java:S3398`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:684`
- **Effort**: 5min
- **Created**: 2022-03-19T10:15:25+0000
- **Assignee**: Unassigned
- **Message**:
  Move this method into "HostnameHolder".

## 344. AX-h0PEE8MRnigXE9oV2

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:688`
- **Effort**: 5min
- **Created**: 2022-03-19T10:15:25+0000
- **Assignee**: Unassigned
- **Message**:
  Add a private constructor to hide the implicit public one.

## 345. AY6GxEDyV8YPjYp1PvG4

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:712`
- **Effort**: 20min
- **Created**: 2022-03-19T10:15:25+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 346. AY6GxEDyV8YPjYp1PvG7

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:723`
- **Effort**: 20min
- **Created**: 2022-03-19T10:15:25+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 347. AY6GxEDyV8YPjYp1PvG6

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:734`
- **Effort**: 20min
- **Created**: 2022-03-19T10:15:25+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 348. AX-h0PEE8MRnigXE9oV3

- **Rule**: `java:S1118`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:740`
- **Effort**: 5min
- **Created**: 2022-03-19T10:15:25+0000
- **Assignee**: Unassigned
- **Message**:
  Add a private constructor to hide the implicit public one.

## 349. AY6GxEDyV8YPjYp1PvG5

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:751`
- **Effort**: 20min
- **Created**: 2022-03-19T10:15:25+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 350. AZJrgM7yR0DF1Q0VW8Bb

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 351. AZJrgM7yR0DF1Q0VW8Bc

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 352. AZJrgM7yR0DF1Q0VW8Bd

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 353. AZJrgM7yR0DF1Q0VW8Be

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 354. AZJrgM7yR0DF1Q0VW8Bf

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 355. AZJrgM7yR0DF1Q0VW8Bg

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 356. AZJrgM7yR0DF1Q0VW8Bh

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 357. AZJrgM7yR0DF1Q0VW8Bi

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 358. AZJrgM7yR0DF1Q0VW8Bj

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 359. AZJrgM7yR0DF1Q0VW8Bk

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 360. AZJrgM7yR0DF1Q0VW8Bl

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 361. AZJrgM7yR0DF1Q0VW8Bm

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 362. AZJrgM7yR0DF1Q0VW8Bn

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 363. AZJrgM7yR0DF1Q0VW8Bo

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 364. AZJrgM7yR0DF1Q0VW8Bp

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 365. AZJrgM7yR0DF1Q0VW8Bq

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 366. AZJrgM7yR0DF1Q0VW8Br

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/EnumInternerTest.java:70`
- **Effort**: 2min
- **Created**: 2022-03-14T09:43:53+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 367. AX9P7FMI5TP95Vauv3Ry

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/GenericReflectionTest.java:48`
- **Effort**: 2min
- **Created**: 2022-03-02T07:51:16+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 368. AX9P7FMI5TP95Vauv3Rz

- **Rule**: `java:S5786`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/GenericReflectionTest.java:57`
- **Effort**: 2min
- **Created**: 2022-03-02T07:51:16+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this 'public' modifier.

## 369. AX9P7FAE5TP95Vauv3Rt

- **Rule**: `java:S2326`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/TypeOf.java:34`
- **Effort**: 5min
- **Created**: 2022-02-25T14:37:30+0000
- **Assignee**: Unassigned
- **Message**:
  T is not used in the class.

## 370. AX9P7FAE5TP95Vauv3Rr

- **Rule**: `java:S112`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/TypeOf.java:56`
- **Effort**: 20min
- **Created**: 2022-02-25T14:37:30+0000
- **Assignee**: Unassigned
- **Message**:
  Replace generic exceptions with specific library exceptions or a custom exception.

## 371. AX9P7FAE5TP95Vauv3Rs

- **Rule**: `java:S112`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/TypeOf.java:60`
- **Effort**: 20min
- **Created**: 2022-02-25T14:37:30+0000
- **Assignee**: Unassigned
- **Message**:
  Replace generic exceptions with specific library exceptions or a custom exception.

## 372. AY-YQ-DRRn1OMLrPtwpv

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cleaner/impl/jdk9/Jdk9ByteBufferCleanerServiceTest.java:29`
- **Effort**: 5min
- **Created**: 2022-01-11T09:52:26+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.ClassNotFoundException', as it cannot be
  thrown from method's body.

## 373. AY-YQ-DRRn1OMLrPtwpw

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cleaner/impl/jdk9/Jdk9ByteBufferCleanerServiceTest.java:29`
- **Effort**: 5min
- **Created**: 2022-01-11T09:52:26+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.NoSuchFieldException', as it cannot be thrown
  from method's body.

## 374. AY-YQ-DRRn1OMLrPtwpx

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/cleaner/impl/jdk9/Jdk9ByteBufferCleanerServiceTest.java:29`
- **Effort**: 5min
- **Created**: 2022-01-11T09:52:26+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.IllegalAccessException', as it cannot be
  thrown from method's body.

## 375. AY-YQ-ECRn1OMLrPtwp0

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/benchmarks/Randomness.java:53`
- **Effort**: 5min
- **Created**: 2021-12-16T08:59:15+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 376. AX3JPKoFmvao3kIhvfxL

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCounted.java:38`
- **Effort**: 2min
- **Created**: 2021-12-15T12:20:37+0000
- **Assignee**: minborg@github
- **Message**:
  Remove the "transient" modifier from this field.

## 377. AZJrgM3sR0DF1Q0VW8BN

- **Rule**: `java:S131`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/BackgroundResourceReleaserMain.java:31`
- **Effort**: 5min
- **Created**: 2021-11-19T15:19:51+0000
- **Assignee**: Unassigned
- **Message**:
  Add a default case to this switch.

## 378. AZo0trlRI-95tLi6OoTj

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/BackgroundResourceReleaserTest.java:25`
- **Effort**: 1min
- **Created**: 2021-11-19T15:19:51+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused import 'java.io.IOException'.

## 379. AX0AR1IRrREJKED430y4

- **Rule**: `java:S135`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/Histogram.java:105`
- **Effort**: 20min
- **Created**: 2021-11-08T10:06:52+0000
- **Assignee**: Unassigned
- **Message**:
  Reduce the total number of break and continue statements in this loop to use at most one.

## 380. AXzV8zKoUrJF4IGTspcL

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java:64`
- **Effort**: 2min
- **Created**: 2021-10-28T14:49:53+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 381. AX1XQsl_PpSI9FH07-oW

- **Rule**: `java:S5777`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:527`
- **Effort**: 5min
- **Created**: 2021-10-26T07:01:16+0000
- **Assignee**: Unassigned
- **Message**:
  Move assertions into separate method or use assertThrows or try-catch instead.

## 382. AXzV8zQqUrJF4IGTspfH

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:501`
- **Effort**: 10min
- **Created**: 2021-10-06T09:22:21+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.err by a logger.

## 383. AY6GxD4kV8YPjYp1PvGs

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/CleaningThread.java:166`
- **Effort**: 20min
- **Created**: 2021-09-23T16:06:05+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 384. AXzV8zKCUrJF4IGTspb0

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCounted.java:37`
- **Effort**: 2min
- **Created**: 2021-09-20T08:31:20+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 385. AZS37rYbkNzmsEMbAjrB

- **Rule**: `java:S1858`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/StringInternerTest.java:58`
- **Effort**: 2min
- **Created**: 2021-08-27T11:30:15+0000
- **Assignee**: Unassigned
- **Message**:
  "lowerCaseString" is already a string, there's no need to call "toString()" on it.

## 386. AZJrgM7eR0DF1Q0VW8Ba

- **Rule**: `java:S1110`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/StringInternerTest.java:71`
- **Effort**: 1min
- **Created**: 2021-08-27T11:30:15+0000
- **Assignee**: Unassigned
- **Message**:
  Remove these useless parentheses.

## 387. AZS37rYbkNzmsEMbAjrC

- **Rule**: `java:S2140`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/StringInternerTest.java:71`
- **Effort**: 5min
- **Created**: 2021-08-27T11:30:15+0000
- **Assignee**: Unassigned
- **Message**:
  Use "java.util.Random.nextInt()" instead.

## 388. AZS37rYbkNzmsEMbAjrD

- **Rule**: `java:S2140`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/StringInternerTest.java:73`
- **Effort**: 5min
- **Created**: 2021-08-27T11:30:15+0000
- **Assignee**: Unassigned
- **Message**:
  Use "java.util.Random.nextInt()" instead.

## 389. AXzV8zI5UrJF4IGTspap

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/ThreadDump.java:43`
- **Effort**: 2min
- **Created**: 2021-08-26T15:23:52+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 390. AXzV8zLeUrJF4IGTspcv

- **Rule**: `java:S1191`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/util/DirectBufferUtil.java:19`
- **Effort**: 40min
- **Created**: 2021-08-24T13:11:28+0000
- **Assignee**: minborg@github
- **Message**:
  Use classes from the Java API instead of Sun classes.

## 391. AXzV8zKoUrJF4IGTspcP

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java:63`
- **Effort**: 2min
- **Created**: 2021-08-23T15:13:22+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 392. AZJrgM5vR0DF1Q0VW8BT

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/ObjectUtilsConvertToTest.java:67`
- **Effort**: 5min
- **Created**: 2021-07-16T09:08:25+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "ordinal" private field.

## 393. AY-YQ-FaRn1OMLrPtwqA

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:440`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 394. AY-YQ-FaRn1OMLrPtwqB

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:448`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 395. AY-YQ-FaRn1OMLrPtwqC

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:456`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 396. AY-YQ-FaRn1OMLrPtwqD

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:464`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 397. AY-YQ-FaRn1OMLrPtwqE

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:474`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 398. AY-YQ-FaRn1OMLrPtwqF

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:484`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 399. AY-YQ-FaRn1OMLrPtwqG

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:492`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 400. AY-YQ-FaRn1OMLrPtwqH

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:502`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 401. AY-YQ-FaRn1OMLrPtwqI

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:512`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 402. AY-YQ-FaRn1OMLrPtwqJ

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:520`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 403. AY-YQ-FaRn1OMLrPtwqK

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:529`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 404. AY-YQ-FaRn1OMLrPtwqL

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:541`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 405. AY-YQ-FaRn1OMLrPtwqM

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:552`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 406. AY-YQ-FaRn1OMLrPtwqN

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:560`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 407. AY-YQ-FaRn1OMLrPtwqO

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:568`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 408. AY-YQ-FaRn1OMLrPtwqP

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:576`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 409. AY-YQ-FaRn1OMLrPtwqQ

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:584`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 410. AY-YQ-FaRn1OMLrPtwqR

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:592`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 411. AY-YQ-FaRn1OMLrPtwqS

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:600`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 412. AY-YQ-FaRn1OMLrPtwqT

- **Rule**: `java:S1117`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafeMemory2Test.java:608`
- **Effort**: 5min
- **Created**: 2021-07-05T14:13:23+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Rename "memory" which hides the field declared at line 37.

## 413. AXzV8zQgUrJF4IGTspe9

- **Rule**: `java:S1191`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:32`
- **Effort**: 2h20min
- **Created**: 2021-06-17T11:12:50+0000
- **Assignee**: Unassigned
- **Message**:
  Use classes from the Java API instead of Sun classes.

## 414. AY6GxEFyV8YPjYp1PvHB

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:1686`
- **Effort**: 20min
- **Created**: 2021-06-17T11:12:50+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Catch Exception instead of Throwable.

## 415. AXzV8zJgUrJF4IGTspbk

- **Rule**: `java:S1191`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/IOTools.java:25`
- **Effort**: 20min
- **Created**: 2021-06-17T10:21:28+0000
- **Assignee**: Unassigned
- **Message**:
  Use classes from the Java API instead of Sun classes.

## 416. AXzV8zM4UrJF4IGTspdA

- **Rule**: `java:S1191`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Memory.java:22`
- **Effort**: 20min
- **Created**: 2021-06-17T09:05:06+0000
- **Assignee**: Unassigned
- **Message**:
  Use classes from the Java API instead of Sun classes.

## 417. AXzV8zM4UrJF4IGTspc9

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Memory.java:891`
- **Effort**: 5min
- **Created**: 2021-06-17T09:05:06+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 418. AXzV8zM4UrJF4IGTspc-

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Memory.java:892`
- **Effort**: 5min
- **Created**: 2021-06-17T09:05:06+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 419. AXzV8zM4UrJF4IGTspc_

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Memory.java:893`
- **Effort**: 5min
- **Created**: 2021-06-17T09:05:06+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 420. AXzV8zQgUrJF4IGTspeX

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:364`
- **Effort**: 10min
- **Created**: 2021-03-24T12:16:55+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.err by a logger.

## 421. AXzV8zRLUrJF4IGTspfo

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:63`
- **Effort**: 5min
- **Created**: 2021-03-18T10:23:59+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 422. AXzV8zRXUrJF4IGTspgJ

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/UnsafeMemory.java:74`
- **Effort**: 0min
- **Created**: 2021-03-09T10:43:47+0000
- **Assignee**: Unassigned
- **Message**:
  Complete the task associated to this TODO comment.

## 423. AXzV8zJ5UrJF4IGTspbr

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractReferenceCounted.java:48`
- **Effort**: 2min
- **Created**: 2021-03-07T22:33:20+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 424. AXzV8zQ3UrJF4IGTspfj

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/cooler/CoolerTester.java:249`
- **Effort**: 10min
- **Created**: 2021-02-12T14:02:53+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.out by a logger.

## 425. AXzV8zQgUrJF4IGTspeS

- **Rule**: `java:S3457`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:157`
- **Effort**: 1min
- **Created**: 2021-02-04T16:02:02+0000
- **Assignee**: Unassigned
- **Message**:
  Format specifiers should be used instead of string concatenation.

## 426. AXzV8zQgUrJF4IGTspeQ

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:1720`
- **Effort**: 10min
- **Created**: 2021-02-04T16:02:02+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.err by a logger.

## 427. AXzV8zHJUrJF4IGTspZd

- **Rule**: `java:S3776`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/ObjectUtils.java:398`
- **Effort**: 8min
- **Created**: 2021-02-04T15:57:21+0000
- **Assignee**: Unassigned
- **Message**:
  Refactor this method to reduce its Cognitive Complexity from 18 to the 15 allowed.

## 428. AXzV8zQgUrJF4IGTspeZ

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:517`
- **Effort**: 0min
- **Created**: 2021-02-03T14:53:18+0000
- **Assignee**: minborg@github
- **Message**:
  Complete the task associated to this TODO comment.

## 429. AXzV8zQgUrJF4IGTspeY

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:552`
- **Effort**: 0min
- **Created**: 2021-02-03T14:53:18+0000
- **Assignee**: minborg@github
- **Message**:
  Complete the task associated to this TODO comment.

## 430. AXzV8zH8UrJF4IGTspaM

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/CleaningThread.java:110`
- **Effort**: 30min
- **Created**: 2021-01-26T16:52:14+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility update should be removed.

## 431. AXzV8zH8UrJF4IGTspaT

- **Rule**: `java:S135`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/CleaningThread.java:148`
- **Effort**: 40min
- **Created**: 2021-01-26T16:52:14+0000
- **Assignee**: Unassigned
- **Message**:
  Reduce the total number of break and continue statements in this loop to use at most one.

## 432. AY-YQ-IwRn1OMLrPtwq_

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/ClassLocalTest.java:30`
- **Effort**: 5min
- **Created**: 2020-12-10T11:37:59+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 433. AY-YQ-CxRn1OMLrPtwpo

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/time/SystemTimeProviderTest.java:80`
- **Effort**: 5min
- **Created**: 2020-12-10T11:37:59+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 434. AY-YQ-CxRn1OMLrPtwpp

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/time/SystemTimeProviderTest.java:84`
- **Effort**: 5min
- **Created**: 2020-12-10T11:37:59+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 435. AXzV8zQgUrJF4IGTspeh

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:831`
- **Effort**: 0min
- **Created**: 2020-12-07T11:39:52+0000
- **Assignee**: Unassigned
- **Message**:
  Complete the task associated to this TODO comment.

## 436. AXzV8zQgUrJF4IGTspee

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:737`
- **Effort**: 5min
- **Created**: 2020-11-26T14:34:35+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 437. AXzV8zQgUrJF4IGTspef

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:740`
- **Effort**: 5min
- **Created**: 2020-11-26T14:34:35+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 438. AXzV8zQgUrJF4IGTspet

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:1302`
- **Effort**: 30min
- **Created**: 2020-11-26T14:34:35+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility bypass should be removed.

## 439. AXzV8zOrUrJF4IGTspdZ

- **Rule**: `java:S1121`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/pool/DynamicEnumClass.java:186`
- **Effort**: 5min
- **Created**: 2020-11-24T01:52:47+0000
- **Assignee**: Unassigned
- **Message**:
  Extract the assignment out of this expression.

## 440. AZJrgM9dR0DF1Q0VW8Bz

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:241`
- **Effort**: 5min
- **Created**: 2020-11-18T00:58:58+0000
- **Assignee**: JerryShea@github
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 441. AZJrgM9dR0DF1Q0VW8B0

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:277`
- **Effort**: 5min
- **Created**: 2020-11-18T00:58:58+0000
- **Assignee**: JerryShea@github
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 442. AY-YQ92VRn1OMLrPtwml

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/internal/analytics/AnalyticsFacadeTest.java:80`
- **Effort**: 5min
- **Created**: 2020-11-17T09:46:12+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused "dummyFacade" local variable.

## 443. AXzV8zLxUrJF4IGTspcy

- **Rule**: `java:S135`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/announcer/InternalAnnouncer.java:98`
- **Effort**: 20min
- **Created**: 2020-11-12T14:09:44+0000
- **Assignee**: minborg@github
- **Message**:
  Reduce the total number of break and continue statements in this loop to use at most one.

## 444. AY6GxEFyV8YPjYp1PvG_

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:1342`
- **Effort**: 20min
- **Created**: 2020-11-05T05:17:38+0000
- **Assignee**: JerryShea@github
- **Message**:
  Catch Exception instead of Throwable.

## 445. AXzV8zRXUrJF4IGTspgI

- **Rule**: `java:S4144`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/UnsafeMemory.java:556`
- **Effort**: 15min
- **Created**: 2020-11-02T10:30:37+0000
- **Assignee**: Unassigned
- **Message**:
  Update this method so that its implementation is not identical to "unsafeObjectFieldOffset" on line
  526.

## 446. AXzV8zKoUrJF4IGTspcK

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java:61`
- **Effort**: 2min
- **Created**: 2020-11-02T10:30:37+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 447. AXzV8zEgUrJF4IGTspYC

- **Rule**: `java:S2160`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/WeakIdentityHashMap.java:28`
- **Effort**: 30min
- **Created**: 2020-11-01T17:54:45+0000
- **Assignee**: Unassigned
- **Message**:
  Override the "equals" method in this class.

## 448. AY6GxD-LV8YPjYp1PvGu

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java:298`
- **Effort**: 20min
- **Created**: 2020-10-19T16:20:43+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 449. AXzV8zCUUrJF4IGTspXh

- **Rule**: `java:S2629`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/internal/cleaner/ReflectionBasedByteBufferCleanerService.java:71`
- **Effort**: 5min
- **Created**: 2020-09-25T13:02:33+0000
- **Assignee**: minborg@github
- **Message**:
  Invoke method(s) only conditionally. Use the built-in formatting to construct this argument.

## 450. AY6GxEFyV8YPjYp1PvHA

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:1678`
- **Effort**: 20min
- **Created**: 2020-09-24T08:56:40+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 451. AXzV8zOrUrJF4IGTspdW

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/pool/DynamicEnumClass.java:95`
- **Effort**: 30min
- **Created**: 2020-09-08T13:29:06+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility update should be removed.

## 452. AXzV8zPgUrJF4IGTspd-

- **Rule**: `java:S1444`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/time/SystemTimeProvider.java:46`
- **Effort**: 20min
- **Created**: 2020-08-11T07:48:05+0000
- **Assignee**: Unassigned
- **Message**:
  Make this "public static CLOCK" field final

## 453. AXzV8zPgUrJF4IGTspd8

- **Rule**: `java:S3008`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/time/SystemTimeProvider.java:46`
- **Effort**: 2min
- **Created**: 2020-08-11T07:48:05+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this field "CLOCK" to match the regular expression '^[a-z][a-zA-Z0-9]*$'.

## 454. AXzV8zPgUrJF4IGTspd9

- **Rule**: `java:S1104`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/time/SystemTimeProvider.java:46`
- **Effort**: 10min
- **Created**: 2020-08-11T07:48:05+0000
- **Assignee**: Unassigned
- **Message**:
  Make CLOCK a static final constant or non-public and provide accessors if needed.

## 455. AXzV8zIGUrJF4IGTspaZ

- **Rule**: `java:S4276`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/threads/CleaningThreadLocal.java:216`
- **Effort**: 5min
- **Created**: 2020-07-13T23:33:34+0000
- **Assignee**: Unassigned
- **Message**:
  Refactor this code to use the more specialised Functional Interface 'UnaryOperator<T>'

## 456. AXzV8zK8UrJF4IGTspcl

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/SimpleCloseable.java:23`
- **Effort**: 2min
- **Created**: 2020-07-06T14:52:59+0000
- **Assignee**: minborg@github
- **Message**:
  Remove the "transient" modifier from this field.

## 457. AY6GxEFyV8YPjYp1PvG-

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:1310`
- **Effort**: 20min
- **Created**: 2020-07-03T12:12:44+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 458. AY_FhesbQoTCYS6v6szG

- **Rule**: `java:S4144`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/UnsafeMemory.java:271`
- **Effort**: 15min
- **Created**: 2020-07-03T12:12:44+0000
- **Assignee**: Unassigned
- **Message**:
  Update this method so that its implementation is not identical to "putInt" on line 161.

## 459. AXzV8zRXUrJF4IGTspgH

- **Rule**: `java:S4144`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/UnsafeMemory.java:762`
- **Effort**: 15min
- **Created**: 2020-07-03T12:12:44+0000
- **Assignee**: Unassigned
- **Message**:
  Update this method so that its implementation is not identical to "unsafeGetByte" on line 213.

## 460. AXzV8zRXUrJF4IGTspgG

- **Rule**: `java:S4144`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/UnsafeMemory.java:874`
- **Effort**: 15min
- **Created**: 2020-07-03T12:12:44+0000
- **Assignee**: Unassigned
- **Message**:
  Update this method so that its implementation is not identical to "unsafeGetInt" on line 202.

## 461. AXzV8zRXUrJF4IGTspgF

- **Rule**: `java:S4144`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/UnsafeMemory.java:924`
- **Effort**: 15min
- **Created**: 2020-07-03T12:12:44+0000
- **Assignee**: Unassigned
- **Message**:
  Update this method so that its implementation is not identical to "unsafeGetLong" on line 191.

## 462. AXzV8zKTUrJF4IGTspb-

- **Rule**: `java:S1191`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/UnsafeCloseable.java:20`
- **Effort**: 20min
- **Created**: 2020-07-03T12:12:44+0000
- **Assignee**: Unassigned
- **Message**:
  Use classes from the Java API instead of Sun classes.

## 463. AXzV8zKoUrJF4IGTspcN

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java:67`
- **Effort**: 2min
- **Created**: 2020-07-02T07:49:14+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 464. AXzV8zFtUrJF4IGTspYU

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/Histogram.java:420`
- **Effort**: 5min
- **Created**: 2020-06-30T13:28:50+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 465. AXzV8zFtUrJF4IGTspYV

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/Histogram.java:421`
- **Effort**: 5min
- **Created**: 2020-06-30T13:28:50+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 466. AXzV8zFtUrJF4IGTspYW

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/Histogram.java:422`
- **Effort**: 5min
- **Created**: 2020-06-30T13:28:50+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 467. AY6GxD_bV8YPjYp1PvGv

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/BackgroundResourceReleaser.java:197`
- **Effort**: 20min
- **Created**: 2020-06-26T13:58:09+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 468. AXzV8zKoUrJF4IGTspcM

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java:66`
- **Effort**: 2min
- **Created**: 2020-06-15T17:03:30+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 469. AXzV8zJ5UrJF4IGTspbq

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractReferenceCounted.java:50`
- **Effort**: 2min
- **Created**: 2020-06-15T17:03:30+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 470. AXzV8zKCUrJF4IGTspb1

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCounted.java:39`
- **Effort**: 2min
- **Created**: 2020-06-11T18:51:48+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 471. AZJrgM2YR0DF1Q0VW8BH

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCountedTest.java:52`
- **Effort**: 5min
- **Created**: 2020-06-11T18:51:48+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 472. AZJrgM2YR0DF1Q0VW8BI

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCountedTest.java:84`
- **Effort**: 5min
- **Created**: 2020-06-11T18:51:48+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 473. AZJrgM2YR0DF1Q0VW8BJ

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCountedTest.java:98`
- **Effort**: 5min
- **Created**: 2020-06-11T18:51:48+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 474. AZJrgM2YR0DF1Q0VW8BM

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractCloseableReferenceCountedTest.java:130`
- **Effort**: 5min
- **Created**: 2020-06-11T18:51:48+0000
- **Assignee**: Unassigned
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 475. AZJrgM4NR0DF1Q0VW8BQ

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/threads/OnDemandEventLoopTest.java:69`
- **Effort**: 5min
- **Created**: 2020-06-01T13:01:22+0000
- **Assignee**: Unassigned
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 476. AXzV8zEgUrJF4IGTspYD

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/WeakIdentityHashMap.java:30`
- **Effort**: 2min
- **Created**: 2020-06-01T06:48:37+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 477. AZJrgM4CR0DF1Q0VW8BO

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractReferenceCountedTest.java:45`
- **Effort**: 5min
- **Created**: 2020-05-27T15:17:51+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 478. AZJrgM4CR0DF1Q0VW8BP

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/io/AbstractReferenceCountedTest.java:69`
- **Effort**: 5min
- **Created**: 2020-05-27T15:17:51+0000
- **Assignee**: Unassigned
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 479. AY6GxEAVV8YPjYp1PvGz

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/TracingReferenceCounted.java:244`
- **Effort**: 20min
- **Created**: 2020-05-27T13:20:45+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 480. AXzV8zKoUrJF4IGTspcJ

- **Rule**: `java:S2065`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/AbstractCloseable.java:65`
- **Effort**: 2min
- **Created**: 2020-05-26T14:02:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the "transient" modifier from this field.

## 481. AY-YQ-HLRn1OMLrPtwqa

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/CleaningRandomAccessFileTest.java:34`
- **Effort**: 5min
- **Created**: 2020-05-07T10:33:10+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 482. AY-YQ-G9Rn1OMLrPtwqZ

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/RandomAccessFileCleanupMain.java:26`
- **Effort**: 5min
- **Created**: 2020-05-07T09:41:50+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 483. AXzV8zQqUrJF4IGTspfI

- **Rule**: `java:S1135`
- **Severity**: INFO
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:516`
- **Effort**: 0min
- **Created**: 2020-04-10T18:45:55+0000
- **Assignee**: Unassigned
- **Message**:
  Complete the task associated to this TODO comment.

## 484. AY6GxEE7V8YPjYp1PvG8

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/LicenceCheck.java:88`
- **Effort**: 20min
- **Created**: 2020-01-29T13:39:16+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 485. AY_FhenLQoTCYS6v6szD

- **Rule**: `java:S1068`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/pool/StringBuilderPool.java:38`
- **Effort**: 5min
- **Created**: 2019-10-13T22:16:30+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused "sbtl" private field.

## 486. AXzV8zHZUrJF4IGTspZ8

- **Rule**: `java:S1659`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/StringUtils.java:648`
- **Effort**: 2min
- **Created**: 2019-03-19T19:51:44+0000
- **Assignee**: Unassigned
- **Message**:
  Declare "len" on a separate line.

## 487. AXzV8zHZUrJF4IGTspZ6

- **Rule**: `java:S1659`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/StringUtils.java:564`
- **Effort**: 2min
- **Created**: 2019-03-18T17:51:51+0000
- **Assignee**: Unassigned
- **Message**:
  Declare "len" on a separate line.

## 488. AXzV8zRLUrJF4IGTspf5

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:825`
- **Effort**: 5min
- **Created**: 2019-03-11T10:51:49+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 489. AXzV8zQ3UrJF4IGTspfk

- **Rule**: `java:S107`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/cooler/CoolerTester.java:105`
- **Effort**: 20min
- **Created**: 2019-03-11T10:51:49+0000
- **Assignee**: Unassigned
- **Message**:
  Method has 8 parameters, which is greater than 7 authorized.

## 490. AXzV8zQ3UrJF4IGTspfc

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/cooler/CoolerTester.java:118`
- **Effort**: 5min
- **Created**: 2019-03-11T10:51:49+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 491. AXzV8zQ3UrJF4IGTspfh

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/cooler/CoolerTester.java:247`
- **Effort**: 10min
- **Created**: 2019-02-25T16:10:45+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.out by a logger.

## 492. AXzV8zQ3UrJF4IGTspfi

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/cooler/CoolerTester.java:248`
- **Effort**: 10min
- **Created**: 2019-02-25T16:10:45+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.out by a logger.

## 493. AXzV8zQ3UrJF4IGTspfe

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/cooler/CoolerTester.java:214`
- **Effort**: 10min
- **Created**: 2019-02-25T07:49:05+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.out by a logger.

## 494. AXzV8zQ3UrJF4IGTspff

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/cooler/CoolerTester.java:222`
- **Effort**: 10min
- **Created**: 2019-02-25T07:49:05+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.out by a logger.

## 495. AXzV8zQ3UrJF4IGTspfg

- **Rule**: `java:S106`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/cooler/CoolerTester.java:224`
- **Effort**: 10min
- **Created**: 2019-02-25T07:49:05+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this use of System.out by a logger.

## 496. AXzV8zQ3UrJF4IGTspfU

- **Rule**: `java:S112`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/cooler/CoolerTester.java:230`
- **Effort**: 20min
- **Created**: 2019-02-25T07:49:05+0000
- **Assignee**: Unassigned
- **Message**:
  Replace generic exceptions with specific library exceptions or a custom exception.

## 497. AXzV8zRXUrJF4IGTspgK

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/UnsafeMemory.java:88`
- **Effort**: 30min
- **Created**: 2018-07-23T15:37:10+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility update should be removed.

## 498. AY-YQ-I9Rn1OMLrPtwrB

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/jitter/LongPingPongMain.java:28`
- **Effort**: 1min
- **Created**: 2018-06-07T16:50:53+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "h".

## 499. AY-YQ-I9Rn1OMLrPtwrC

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/jitter/LongPingPongMain.java:28`
- **Effort**: 5min
- **Created**: 2018-06-07T16:50:53+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "h" local variable.

## 500. AY-YQ-I9Rn1OMLrPtwrA

- **Rule**: `java:S1854`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/jitter/LongPingPongMain.java:29`
- **Effort**: 1min
- **Created**: 2018-06-07T16:50:53+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this useless assignment to local variable "pong".

## 501. AY-YQ-I9Rn1OMLrPtwrD

- **Rule**: `java:S1481`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/jitter/LongPingPongMain.java:29`
- **Effort**: 5min
- **Created**: 2018-06-07T16:50:53+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this unused "pong" local variable.

## 502. AZJrgNAAR0DF1Q0VW8CB

- **Rule**: `java:S108`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/jitter/LongPingPongMain.java:30`
- **Effort**: 5min
- **Created**: 2018-06-07T16:50:53+0000
- **Assignee**: peter-lawrey@github
- **Message**:
  Remove this block of code, fill it in, or add a comment explaining why it is empty.

## 503. AZJrgM-cR0DF1Q0VW8B6

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmSafepointTest.java:30`
- **Effort**: 5min
- **Created**: 2018-05-17T16:58:20+0000
- **Assignee**: Unassigned
- **Message**:
  Add the "@Override" annotation above this method signature

## 504. AZJrgM7NR0DF1Q0VW8BY

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/ClassAliasPoolTest.java:155`
- **Effort**: 5min
- **Created**: 2018-04-21T09:49:25+0000
- **Assignee**: Unassigned
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 505. AZJrgM8KR0DF1Q0VW8Bs

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/YesNo.java:22`
- **Effort**: 2min
- **Created**: 2018-01-28T23:08:22+0000
- **Assignee**: JerryShea@github
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 506. AZJrgM8KR0DF1Q0VW8Bt

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/YesNo.java:22`
- **Effort**: 2min
- **Created**: 2018-01-28T23:08:22+0000
- **Assignee**: JerryShea@github
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 507. AYjj_zFqofuPqSq8nRca

- **Rule**: `java:S1144`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:340`
- **Effort**: 2min
- **Created**: 2017-11-22T16:58:05+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused private "getProcessId0" method.

## 508. AXzV8zT3UrJF4IGTspkE

- **Rule**: `java:S2925`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/SleepTesterMain.java:52`
- **Effort**: 20min
- **Created**: 2017-11-02T16:07:29+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this use of "Thread.sleep()".

## 509. AXzV8zHZUrJF4IGTspZz

- **Rule**: `java:S2129`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/StringUtils.java:374`
- **Effort**: 5min
- **Created**: 2017-08-30T13:59:24+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this "String" constructor

## 510. AXzV8zHZUrJF4IGTspZ0

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/StringUtils.java:376`
- **Effort**: 30min
- **Created**: 2017-08-30T13:59:24+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility bypass should be removed.

## 511. AZS37rVakNzmsEMbAjrA

- **Rule**: `java:S1612`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/util/AbstractInvocationHandlerTest.java:82`
- **Effort**: 2min
- **Created**: 2017-08-21T01:57:43+0000
- **Assignee**: Unassigned
- **Message**:
  Replace this lambda with method reference 'messages::add'.

## 512. AXzV8zOrUrJF4IGTspdX

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/pool/DynamicEnumClass.java:142`
- **Effort**: 30min
- **Created**: 2017-07-28T16:41:02+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility bypass should be removed.

## 513. AXzV8zOrUrJF4IGTspdY

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/pool/DynamicEnumClass.java:144`
- **Effort**: 30min
- **Created**: 2017-07-28T16:41:02+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility bypass should be removed.

## 514. AXzV8zRLUrJF4IGTspfp

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:161`
- **Effort**: 5min
- **Created**: 2017-07-20T20:00:34+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 515. AXzV8zRLUrJF4IGTspfr

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:231`
- **Effort**: 5min
- **Created**: 2017-07-20T20:00:34+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 516. AXzV8zRLUrJF4IGTspft

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:268`
- **Effort**: 5min
- **Created**: 2017-07-20T20:00:34+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 517. AXzV8zRLUrJF4IGTspfv

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:305`
- **Effort**: 5min
- **Created**: 2017-07-20T20:00:34+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 518. AXzV8zRLUrJF4IGTspfx

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:342`
- **Effort**: 5min
- **Created**: 2017-07-20T20:00:34+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 519. AXzV8zRLUrJF4IGTspfz

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:380`
- **Effort**: 5min
- **Created**: 2017-07-20T20:00:34+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 520. AXzV8zRLUrJF4IGTspf1

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:417`
- **Effort**: 5min
- **Created**: 2017-07-20T20:00:34+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 521. AXzV8zRLUrJF4IGTspf3

- **Rule**: `java:S3358`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:454`
- **Effort**: 5min
- **Created**: 2017-07-20T20:00:34+0000
- **Assignee**: Unassigned
- **Message**:
  Extract this nested ternary operation into an independent statement.

## 522. AZJrgM_TR0DF1Q0VW8B8

- **Rule**: `java:S115`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:43`
- **Effort**: 2min
- **Created**: 2017-07-18T11:26:49+0000
- **Assignee**: Unassigned
- **Message**:
  Rename this constant name to match the regular expression '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.

## 523. AY-YQ-H_Rn1OMLrPtwqi

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:209`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 524. AY-YQ-H_Rn1OMLrPtwqj

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:219`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 525. AY-YQ-H_Rn1OMLrPtwqk

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:229`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 526. AY-YQ-H_Rn1OMLrPtwql

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:235`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 527. AY-YQ-H_Rn1OMLrPtwqm

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:241`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 528. AY-YQ-H_Rn1OMLrPtwqn

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:247`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 529. AY-YQ-H_Rn1OMLrPtwqo

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:253`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 530. AY-YQ-H_Rn1OMLrPtwqp

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:259`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 531. AY-YQ-H_Rn1OMLrPtwqq

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:265`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 532. AY-YQ-H_Rn1OMLrPtwqr

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:271`
- **Effort**: 5min
- **Created**: 2017-07-17T17:03:04+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 533. AZfUXX-Lk0RInRIJB3jc

- **Rule**: `java:S3398`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Jvm.java:877`
- **Effort**: 5min
- **Created**: 2017-07-04T14:47:24+0000
- **Assignee**: Unassigned
- **Message**:
  Move this method into "MaxMemoryHolder".

## 534. AY-YQ-IaRn1OMLrPtwq7

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:151`
- **Effort**: 5min
- **Created**: 2017-04-27T21:34:48+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 535. AXzV8zHZUrJF4IGTspZx

- **Rule**: `java:S2129`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/StringUtils.java:350`
- **Effort**: 5min
- **Created**: 2016-12-28T18:20:54+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this "String" constructor

## 536. AXzV8zHZUrJF4IGTspZ4

- **Rule**: `java:S3776`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/StringUtils.java:495`
- **Effort**: 7min
- **Created**: 2016-12-28T18:20:54+0000
- **Assignee**: Unassigned
- **Message**:
  Refactor this method to reduce its Cognitive Complexity from 17 to the 15 allowed.

## 537. AY-YQ-B9Rn1OMLrPtwpm

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/ParsingCacheTest.java:30`
- **Effort**: 5min
- **Created**: 2016-12-20T14:16:46+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from
  method's body.

## 538. AY-YQ-DiRn1OMLrPtwpy

- **Rule**: `java:S125`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/UnsafePingPointMain.java:69`
- **Effort**: 5min
- **Created**: 2016-07-14T19:08:18+0000
- **Assignee**: Unassigned
- **Message**:
  This block of commented-out lines of code should be removed.

## 539. AXzV8zHZUrJF4IGTspZv

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/StringUtils.java:148`
- **Effort**: 30min
- **Created**: 2016-05-14T07:28:49+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility bypass should be removed.

## 540. AY-YQ-IaRn1OMLrPtwq1

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:123`
- **Effort**: 5min
- **Created**: 2016-04-12T11:08:14+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.NoSuchMethodException', as it cannot be thrown
  from method's body.

## 541. AY-YQ-IaRn1OMLrPtwq2

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:123`
- **Effort**: 5min
- **Created**: 2016-04-12T11:08:14+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.IllegalAccessException', as it cannot be
  thrown from method's body.

## 542. AY-YQ-IaRn1OMLrPtwq3

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:123`
- **Effort**: 5min
- **Created**: 2016-04-12T11:08:14+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.reflect.InvocationTargetException', as it
  cannot be thrown from method's body.

## 543. AY-YQ-IaRn1OMLrPtwq4

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:144`
- **Effort**: 5min
- **Created**: 2016-04-12T11:08:14+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.NoSuchMethodException', as it cannot be thrown
  from method's body.

## 544. AY-YQ-IaRn1OMLrPtwq5

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:144`
- **Effort**: 5min
- **Created**: 2016-04-12T11:08:14+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.IllegalAccessException', as it cannot be
  thrown from method's body.

## 545. AY-YQ-IaRn1OMLrPtwq6

- **Rule**: `java:S1130`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:144`
- **Effort**: 5min
- **Created**: 2016-04-12T11:08:14+0000
- **Assignee**: Unassigned
- **Message**:
  Remove the declaration of thrown exception 'java.lang.reflect.InvocationTargetException', as it
  cannot be thrown from method's body.

## 546. AY6GxDxMV8YPjYp1PvGm

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/ThrowingFunction.java:43`
- **Effort**: 20min
- **Created**: 2016-04-11T17:37:27+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 547. AY6GxDxzV8YPjYp1PvGn

- **Rule**: `java:S1181`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/ThrowingSupplier.java:48`
- **Effort**: 20min
- **Created**: 2016-04-11T17:37:27+0000
- **Assignee**: Unassigned
- **Message**:
  Catch Exception instead of Throwable.

## 548. AZJrgM9dR0DF1Q0VW8Bx

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:54`
- **Effort**: 5min
- **Created**: 2016-04-09T21:59:29+0000
- **Assignee**: Unassigned
- **Message**:
  Add the "@Override" annotation above this method signature

## 549. AZJrgM9dR0DF1Q0VW8By

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/JvmTest.java:59`
- **Effort**: 5min
- **Created**: 2016-04-09T21:59:29+0000
- **Assignee**: Unassigned
- **Message**:
  Add the "@Override" annotation above this method signature

## 550. AZJrgM_TR0DF1Q0VW8B9

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:285`
- **Effort**: 5min
- **Created**: 2016-04-09T21:59:29+0000
- **Assignee**: Unassigned
- **Message**:
  Add the "@Override" annotation above this method signature

## 551. AZJrgM_TR0DF1Q0VW8B-

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/MathsTest.java:290`
- **Effort**: 5min
- **Created**: 2016-04-09T21:59:29+0000
- **Assignee**: Unassigned
- **Message**:
  Add the "@Override" annotation above this method signature

## 552. AZJrgM_uR0DF1Q0VW8B_

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:87`
- **Effort**: 5min
- **Created**: 2016-04-09T21:59:29+0000
- **Assignee**: Unassigned
- **Message**:
  Add the "@Override" annotation above this method signature

## 553. AZJrgM_uR0DF1Q0VW8CA

- **Rule**: `java:S1161`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/OSTest.java:92`
- **Effort**: 5min
- **Created**: 2016-04-09T21:59:29+0000
- **Assignee**: Unassigned
- **Message**:
  Add the "@Override" annotation above this method signature

## 554. AY-YQ-BeRn1OMLrPtwpe

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/ClassAliasPoolTest.java:20`
- **Effort**: 1min
- **Created**: 2016-04-09T21:59:29+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused import 'net.openhft.chronicle.core.threads.ThreadDump'.

## 555. AY-YQ-BeRn1OMLrPtwpf

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/ClassAliasPoolTest.java:22`
- **Effort**: 1min
- **Created**: 2016-04-09T21:59:29+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused import 'org.junit.After'.

## 556. AY-YQ-BeRn1OMLrPtwpg

- **Rule**: `java:S1128`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/ClassAliasPoolTest.java:23`
- **Effort**: 1min
- **Created**: 2016-04-09T21:59:29+0000
- **Assignee**: Unassigned
- **Message**:
  Remove this unused import 'org.junit.Before'.

## 557. AXzV8zN1UrJF4IGTspdM

- **Rule**: `java:S135`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/values/LongValue.java:160`
- **Effort**: 20min
- **Created**: 2016-03-07T17:11:36+0000
- **Assignee**: Unassigned
- **Message**:
  Reduce the total number of break and continue statements in this loop to use at most one.

## 558. AXzV8zN1UrJF4IGTspdN

- **Rule**: `java:S135`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/values/LongValue.java:178`
- **Effort**: 20min
- **Created**: 2016-03-07T17:11:36+0000
- **Assignee**: Unassigned
- **Message**:
  Reduce the total number of break and continue statements in this loop to use at most one.

## 559. AXzV8zHZUrJF4IGTspZy

- **Rule**: `java:S3011`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/StringUtils.java:352`
- **Effort**: 30min
- **Created**: 2016-03-02T14:00:44+0000
- **Assignee**: Unassigned
- **Message**:
  This accessibility bypass should be removed.

## 560. AXzV8zJgUrJF4IGTspbd

- **Rule**: `java:S4042`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/io/IOTools.java:204`
- **Effort**: 10min
- **Created**: 2016-03-02T12:11:23+0000
- **Assignee**: Unassigned
- **Message**:
  Use "java.nio.file.Files#delete" here for better messages on error conditions.

## 561. AXzV8zHZUrJF4IGTspaB

- **Rule**: `java:S135`
- **Severity**: MINOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/util/StringUtils.java:420`
- **Effort**: 20min
- **Created**: 2015-11-09T19:26:14+0000
- **Assignee**: Unassigned
- **Message**:
  Reduce the total number of break and continue statements in this loop to use at most one.

## 562. AZJrgM7NR0DF1Q0VW8BZ

- **Rule**: `java:S1186`
- **Severity**: CRITICAL
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/test/java/net/openhft/chronicle/core/pool/ClassAliasPoolTest.java:149`
- **Effort**: 5min
- **Created**: 2015-10-10T17:25:48+0000
- **Assignee**: Unassigned
- **Message**:
  Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or
  complete the implementation.

## 563. AZo0trhOI-95tLi6OoTg

- **Rule**: `javabugs:S3518`
- **Severity**: CRITICAL
- **Type**: BUG
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/Maths.java:796`
- **Effort**: 5min
- **Created**: 2015-09-22T18:49:32+0000
- **Assignee**: Unassigned
- **Message**:
  Fix this division by zero.

## 564. AXzV8zQqUrJF4IGTspfT

- **Rule**: `java:S1191`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/OS.java:26`
- **Effort**: 20min
- **Created**: 2015-02-24T13:58:17+0000
- **Assignee**: Unassigned
- **Message**:
  Use classes from the Java API instead of Sun classes.

## 565. AXzV8zRYUrJF4IGTspjz

- **Rule**: `java:S1191`
- **Severity**: MAJOR
- **Type**: CODE_SMELL
- **Location**: `OpenHFT_Chronicle-Core:src/main/java/net/openhft/chronicle/core/UnsafeMemory.java:24`
- **Effort**: 20min
- **Created**: 2015-02-24T13:58:17+0000
- **Assignee**: Unassigned
- **Message**:
  Use classes from the Java API instead of Sun classes.
