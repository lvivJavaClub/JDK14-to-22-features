
# Good morning, JavaClub 👋

# JDK26 Features ☕️

## Prepare ⚙️

To begin exploring JDK 26, install it using `sdkman`:

```bash
sdk install java 26-oracle
sdk use java 26-oracle
```

For a complete list of all new features, visit the [OpenJDK JDK 26 Project Page](https://openjdk.java.net/projects/jdk/26/).

Also check: https://javaalmanac.io/jdk/26/apidiff/25/

---

# `😎 Finalized Features:`

## 📍 **HTTP/3 for the HTTP Client API (JEP 517)**

- #### `java.net.http.HttpClient` (introduced in JDK 11) now supports **HTTP/3**, which runs over **QUIC/UDP** instead of TCP.
- #### Default version remains HTTP/2 — HTTP/3 must be explicitly requested at client or request level.
- #### Falls back transparently to HTTP/2 if the server does not support HTTP/3.
- #### New `HttpClient.Version.HTTP_3` enum constant.

```java
// Set at client level
HttpClient client = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_3)
    .build();

// Or per-request
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://example.com/"))
    .version(HttpClient.Version.HTTP_3) // falls back to HTTP/2 if unsupported
    .build();
```

> **Non-JEP bonus:** New `HttpRequest.BodyPublishers.ofFileChannel(FileChannel, long, long)` — stream large files without loading them entirely into heap memory.

> **Non-JEP bonus:** `HttpRequest.Builder::timeout` now covers the **entire request/response cycle** including body consumption (previously only until headers arrived).

## 📍 **Ahead-of-Time Object Caching with Any GC (JEP 516)**

- #### Extends the HotSpot AOT cache (from JDK 24 AOT Class Loading & Linking, JEP 483) to work with **any garbage collector**, including ZGC.
- #### Previously, the GC-specific memory format (Compressed OOPs, ZGC metadata bits) prevented a cache built with G1 from being used with ZGC — and vice versa.
- #### Objects in the GC-independent cache are stored by **logical index** and streamed to the heap at load time in the GC-specific format ("Streamable Objects").
- #### The GC-independent format is used automatically when training with ZGC, heaps > 32 GB, or `-XX:-CompressedOops`, or when explicitly requested via `-XX:+AOTStreamableObjects`.

```bash
# Step 1: Training run (record)
java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -cp app.jar com.example.App

# Step 2: Create AOT cache
java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -cp app.jar

# Step 3: Run with cache + ZGC (now works!)
java -XX:AOTCache=app.aot -XX:+UseZGC -cp app.jar com.example.App
```

## 📍 **G1 GC: Improve Throughput by Reducing Synchronization (JEP 522)**

- #### G1 uses a Card Table to track cross-region references. Application write barriers and the GC optimizer thread both accessed the same Card Table, requiring constant synchronization.
- #### **Fix:** A **second Card Table** is introduced. Application threads write to one; the optimizer thread uses the other. When the active table degrades, they swap.
- #### Result: **5–15% throughput improvement** for most workloads, no code changes needed.
- #### Memory cost: both card tables together consume only **0.2% of the Java heap** (~2 MB per GB).
- #### Takes effect automatically on upgrade — no JVM flags needed.

---

# `😉 Further Features Iterations (Preview / Incubator):`

## 📍 **Lazy Constants (Second Preview) (JEP 526)**

Previously known as **Stable Values** in Java 25 — significantly **renamed and simplified** in Java 26.

- #### Thread-safe lazy initialization without Double-Checked Locking or Initialization-on-Demand Holder idioms.
- #### After first initialization, the JVM treats the value as a **true constant** (enables Constant Folding / JIT optimizations — same as `final` fields).
- #### `null` values are **not allowed** — throws `NullPointerException` if the supplier returns null.

```java
// Lazy scalar
private final LazyConstant<Settings> settings =
    LazyConstant.of(this::loadSettingsFromDatabase);

public Locale getLocale() {
    return settings.get().getLocale(); // loaded on first call, cached forever
}

// Lazy List — each element initialized independently on first access
private final List<Double> squareRoots = List.ofLazy(100, Math::sqrt);

// Lazy Map — value per key initialized on first access
Map<Locale, ResourceBundle> bundles = Map.ofLazy(supportedLocales, this::loadBundle);
```

**Changes from Java 25 (Stable Values):**
- `StableValue` → renamed to `LazyConstant`
- `StableValue.list(...)` / `StableValue.map(...)` → moved to `List.ofLazy(...)` / `Map.ofLazy(...)`
- Low-level `orElseSet()`, `setOrThrow()`, `trySet()` methods **removed**
- `StableValue.function(...)` / `StableValue.intFunction(...)` **removed**

## 📍 **Structured Concurrency (Sixth Preview) (JEP 525)**

- #### Treats groups of related concurrent tasks as a single unit of work with clean lexical scopes, automatic cancellation, and no orphaned threads.
- #### `scope.join()` is the single convergence point — no zombie threads after that line.

**Changes in Java 26:**
- `Joiner.allSuccessfulOrThrow()` now returns a **`List`** (not a `Stream`) of results — simpler ergonomics.
- `Joiner.allUntil(...)` now returns a `List` of subtasks (not a stream).
- `anySuccessfulResultOrThrow()` renamed to **`anySuccessfulOrThrow()`**.
- `Joiner` gains a new **`onTimeout()`** method — called when `join(timeout)` expires; throws `TimeoutException` by default but can be overridden.
- `StructuredTaskScope.open(...)` config type changed from `Function<Configuration, Configuration>` to `UnaryOperator<Configuration>`.

```java
// All must succeed — join() returns void; get results from subtask handles
try (var scope = StructuredTaskScope.open()) {
    var weatherTask = scope.fork(() -> fetchWeather("Lviv"));
    var newsTask    = scope.fork(() -> fetchNews("Technology"));
    scope.join();  // waits; cancels all and throws if any fail
    System.out.println(weatherTask.get() + " / " + newsTask.get());
}

// First success wins — join() returns the result directly
try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulOrThrow())) {
    scope.fork(() -> fetchFromServiceA());
    scope.fork(() -> fetchFromServiceB());
    String result = scope.join(); // returns the first successful result
}
```

## 📍 **PEM Encodings of Cryptographic Objects (Second Preview) (JEP 524)**

- #### New `PEMDecoder` and `PEMEncoder` API for reading/writing PEM-encoded cryptographic objects: keys, certificates, CRLs.
- #### PEM is the ubiquitous format used by TLS/SSL tooling (`.pem`, `.crt`, `.key` files).
- #### Reduces 15+ lines of boilerplate to a single readable call.

```java
// Before (Java 25 and earlier) — 15+ lines to decode an encrypted private key
// ... string manipulation, Base64.getDecoder(), Cipher.init(), KeyFactory.generatePrivate() ...

// After (JEP 524 preview)
PrivateKey key = PEMDecoder.of()
    .withDecryption(passphrase.toCharArray())
    .decode(encryptedPrivateKeyPem, PrivateKey.class);

// Encoding
String pem = PEMEncoder.of().encodeToString(keyPair.getPublic());

// Encrypted encoding
String encryptedPem = PEMEncoder.of()
    .withEncryption(passphrase.toCharArray())
    .encodeToString(keyPair.getPrivate());
```

**Changes from Java 25:**
- `PEMRecord` class renamed to `PEM`; gains a `decode()` method returning raw Base64 bytes.
- `EncryptedPrivateKeyInfo.encryptKey(...)` renamed to `encrypt(...)`; now accepts `DEREncodable` objects (not just `PrivateKey`).
- Key pairs and PKCS#8-encoded keys can now be encoded and decoded.

## 📍 **Primitive Types in Patterns, instanceof, and switch (Fourth Preview) (JEP 530)**

- #### Allows **all primitive types** in pattern contexts: `instanceof`, `switch` case labels, and guard clauses (`when`).
- #### Previously, `switch` only accepted `byte`/`short`/`char`/`int` with constants; patterns required reference types.
- #### A pattern like `case byte b` on a `double` matches if the value can be represented exactly as a byte.
- #### Dominance rules apply — order of `case` labels matters and is enforced at compile time.

```java
// Range patterns on primitives
int code = 404;
switch (code) {
    case int i when i >= 100 && i < 200 -> System.out.println("1xx Informational");
    case int i when i >= 200 && i < 300 -> System.out.println("2xx Success");
    case int i when i >= 300 && i < 400 -> System.out.println("3xx Redirection");
    case int i when i >= 400 && i < 500 -> System.out.println("4xx Client Error");
    case int i when i >= 500 && i < 600 -> System.out.println("5xx Server Error");
    default -> throw new IllegalArgumentException();
}

// Precision-fitting patterns
double value = 42.0;
switch (value) {
    case byte   b -> System.out.println("fits in byte: "   + b);
    case short  s -> System.out.println("fits in short: "  + s);
    case int    i -> System.out.println("fits in int: "    + i);
    case long   l -> System.out.println("fits in long: "   + l);
    case float  f -> System.out.println("fits in float: "  + f);
    case double d -> System.out.println("double only: "    + d);
}
```

**Changes in Java 26:** Stricter dominance checks — some constructs that compiled in Java 25 now produce compiler errors:
```java
switch (i) {
    case float f  -> {}
    case 16_777_216 -> {} // ERROR: dominated by float f (16_777_216 fits exactly in float)
}
```

## 📍 **Vector API (Eleventh Incubator) (JEP 529)**

- #### Expresses **SIMD** vector computations that compile to optimal CPU instructions (x64 SSE/AVX, ARM NEON/SVE).
- #### Still incubating while waiting for **Project Valhalla** value types — `Vector<E>` is intended to be a value class from the start. First Project Valhalla Early-Access Build released October 10, 2025.
- #### **No API changes** from Java 25.

```java
static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

void addVectors(float[] a, float[] b, float[] c) {
    int i = 0;
    int upperBound = SPECIES.loopBound(a.length);
    for (; i < upperBound; i += SPECIES.length()) {
        FloatVector.fromArray(SPECIES, a, i)
                   .add(FloatVector.fromArray(SPECIES, b, i))
                   .intoArray(c, i);
    }
    for (; i < a.length; i++) c[i] = a[i] + b[i]; // tail
}
```

---

# `🔐 Security & Integrity:`

## 📍 **Prepare to Make Final Mean Final (JEP 500)**

- #### `final` fields can be mutated at runtime via deep reflection (`Field.setAccessible(true)` + `Field.set()`).
- #### In Java 26, this now emits a **JVM warning** by default — a first step toward closing this loophole.
- #### New JFR event `jdk.FinalFieldMutation` records every violation with a full stack trace.
- #### Using `--add-opens` alone does **NOT** suppress the warning.

| VM flag | Behavior |
|---|---|
| `--illegal-final-field-mutation=allow` | Old behavior — no warning (pre-JDK-26) |
| `--illegal-final-field-mutation=warn` | **Default in JDK 26** — warn on first mutation |
| `--illegal-final-field-mutation=debug` | Warn on every mutation (with stack trace) |
| `--illegal-final-field-mutation=deny` | Throw `IllegalAccessException` — will be default in a future JDK |

> Enable per-module deep reflection: `--enable-final-field-mutation=ALL-UNNAMED`

```java
Box box = new Box("Rubik's Cube");
Field f = Box.class.getDeclaredField("value");
f.setAccessible(true);
f.set(box, "Magic Wand"); // ← WARNING in Java 26; exception in a future JDK
```

---

# `🚫🙅‍♂️ Removals & Deprecations:`

## 📍 **Remove the Applet API (JEP 504)**

- #### `java.applet` package **fully removed** — deprecated since Java 9, deprecated-for-removal since Java 17.
- #### `javax.swing.JApplet`, `java.beans.AppletInitializer`, and related javadoc removed.
- #### Security Manager (required to sandbox untrusted applets) was permanently disabled in JDK 24 (JEP 486), making applet security impossible anyway.

> For audio playback via `AudioClip`: use `javax.sound.SoundClip` introduced in JDK 25.

## 📍 **Thread.stop() is Removed**

- #### `Thread.stop()` marked deprecated in JDK 1.2 (December 1998).
- #### Marked deprecated-for-removal in Java 18.
- #### Throws `UnsupportedOperationException` since Java 20.
- #### **Java 26: method completely removed — 27+ years after initial deprecation.**
- #### No JEP; tracked under JDK-8368226.

## 📍 **Deprecated JVM Flags**

| Flag | Notes |
|---|---|
| `-Xmaxjitcodesize` | Obsolete since JDK 1.4.0 (2002) |
| `AlwaysActAsServerClassMachine` | Deprecated for removal |
| `NeverActAsServerClassMachine` | Deprecated for removal |
| `AggressiveHeap` | Deprecated for removal |
| `MaxRAM` | JVM now accurately detects physical RAM automatically |

---

# `🆕 Other Notable Changes:`

## 📍 **Default Initial Heap Size Reduced**

- #### Previously: **1/64 (1.5625%) of physical RAM** — a Hello World on a 64 GB machine started with a ~1 GB heap!
- #### **Java 26:** Changed to **0.2% (1/500) of physical RAM** — 128 MB on a 64 GB machine.
- #### Benefits: reduced RAM waste and faster startup (GC data structures scale with heap size).
- #### No JEP; tracked under JDK-8348278.

## 📍 **Virtual Threads + Class Initializers**

- #### A virtual thread waiting for another thread to finish **class initialization** was previously pinned to its carrier thread.
- #### **Java 26:** Now the virtual thread **unmounts** (releases the platform thread) while waiting.
- #### Complements JDK 24's fix for pinning inside `synchronized` blocks (JEP 491).
- #### No JEP; tracked under JDK-8369238.

## 📍 **Hybrid Public Key Encryption (HPKE)**

- #### New `"HPKE"` Cipher algorithm implementing **RFC 9180**.
- #### Combines KEM (Key Encapsulation Mechanism), KDF (Key Derivation Function), and AEAD cipher.
- #### New `HPKEParameterSpec` class for selecting algorithm identifiers and configuring modes.

## 📍 **Unicode 17.0 Support**

- #### Java 26 upgrades to **Unicode 17.0**.
- #### All character-processing classes (`String`, `Character`, regex, etc.) gain new characters and code blocks.
- #### No JEP; tracked under JDK-8346944.

## 📍 **Dark Theme for Javadoc**

- #### Javadoc now offers a built-in **dark mode**.
- #### Toggle via the sun/moon icon in the Javadoc menu bar.
- #### No JEP; tracked under JDK-8342705.

---

# Don't wait, try out `JDK 26` now! 🎉 ☕️

# 🇺🇦🇺🇦🇺🇦 СЛАВА УКРАЇНІ 🇺🇦🇺🇦🇺🇦 СЛАВА ЗСУ 🇺🇦🇺🇦🇺🇦