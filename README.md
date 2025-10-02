# Commons Utils Library

![Maven Central](https://img.shields.io/maven-central/v/com.ancevt/commons-utils)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Java](https://img.shields.io/badge/Java-8%2B-brightgreen)
![Build](https://img.shields.io/badge/build-passing-success)

A lightweight collection of **utility classes** for everyday Java development. Includes helpers for:

* Runtime utilities (sleeping, safe execution, exception handling)
* Platform detection and environment variables
* Configuration management with change listeners
* String utilities for rendering text tables

The library is designed to reduce boilerplate and speed up prototyping, while still being flexible enough for production use (with caution).

---

## 🚀 Features

### Runtime Utilities (`com.ancevt.util.runtime`)

* **`Silent`** – wrap operations and swallow exceptions safely.
* **`ExceptionUtils`** – convert stack traces to strings (normal or unsquashed).
* **`EnvUtils`** – type-safe access to environment variables with defaults.
* **`PlatformUtils`** – detect OS, architecture, JVM info, paths, locale, timezone.
* **`MainClassNameExtractor`** – try to detect the main application class.

### Configuration (`com.ancevt.util.config`)

* **`ObservableConfig`** – lightweight key-value store with change listeners.
* **`ObservableConfigValue`** – type-safe accessors for config values (int, boolean, UUID, Path, etc.).

### String Helpers (`com.ancevt.util.string`)

* **`TextTable`** – render tables in ASCII or Unicode, with borders and alignment.

---

## 📦 Installation

```xml
<dependency>
  <groupId>com.ancevt</groupId>
  <artifactId>commons-utils</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## 🔧 Usage Examples

### Silent

```java
import com.ancevt.util.runtime.Silent;

// Sleep without checked exception
Silent.sleep(100);

// Run and swallow exception
Silent.runQuietly(() -> {
    throw new RuntimeException("ignored");
});

// Get result or null
String result = Silent.getOrNull(() -> riskyOperation());
```

### ExceptionUtils

```java
try {
    throw new IllegalStateException("boom");
} catch (Exception e) {
    System.out.println(ExceptionUtils.stackTraceToString(e));
    System.out.println(ExceptionUtils.unsquashedStackTraceToString(e));
}
```

### EnvUtils

```java
int port = EnvUtils.getInt("APP_PORT", 8080);
boolean debug = EnvUtils.getBoolean("DEBUG", false);
Mode mode = EnvUtils.getEnum("MODE", Mode.class, Mode.RELEASE);
```

### PlatformUtils

```java
System.out.println("Running on: " + PlatformUtils.getOperatingSystem());
System.out.println("AppData path: " + PlatformUtils.getApplicationDataPath());
System.out.println("Temp dir: " + PlatformUtils.getTempPath());
```

### ObservableConfig

```java
ObservableConfig config = new ObservableConfig();
config.addChangeListener((key, oldVal, newVal) ->
    System.out.printf("Changed %s: %s -> %s%n", key, oldVal, newVal));

config.put("host", "localhost");
config.put("port", "8080");

int port = config.get("port").asInt(80);
```

### TextTable

```java
import com.ancevt.util.string.TextTable;

TextTable table = TextTable.builder()
    .headers("ID", "Name", "Score")
    .align(0, TextTable.Alignment.RIGHT)
    .align(2, TextTable.Alignment.CENTER)
    .style(TextTable.Style.BORDERED)
    .row(1, "Alice", 95)
    .row(2, "Bob", 87)
    .row(3, "Charlie", 100)
    .build();

System.out.println(table.render());
```

Output:

```
+----+---------+-------+
| ID | Name    | Score |
+----+---------+-------+
|  1 | Alice   |  95   |
|  2 | Bob     |  87   |
|  3 | Charlie | 100   |
+----+---------+-------+
```

---

## ⚠️ Notes

* `Silent` should not be overused in production code – it may hide critical errors.
* `MainClassNameExtractor` relies on JVM internals and may not work in all environments (e.g., Spring Boot fat JARs).

---

## 📝 License

Apache License 2.0 – see [LICENSE](LICENSE).
