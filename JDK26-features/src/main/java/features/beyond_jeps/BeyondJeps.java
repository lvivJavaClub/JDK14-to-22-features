package features.beyond_jeps;

/**
 * Beyond JEPs — Notable Java 26 changes without dedicated JEPs
 * <p>
 * 1. Thread.stop() removed (JDK-8368226)
 *    Deprecated since JDK 1.2 (1998), throws UnsupportedOperationException since JDK 20,
 *    fully removed in Java 26 — 27+ years after initial deprecation.
 * <p>
 * 2. Default initial heap size reduced (JDK-8348278)
 *    Changed from 1/64 (1.5625%) to 0.2% (1/500) of physical RAM.
 *    On a 64 GB machine: 1 GB → 128 MB for a Hello World app.
 * <p>
 * 3. Virtual threads + class initializers (JDK-8369238)
 *    A virtual thread waiting for class initialization now unmounts from its carrier
 *    (releases the platform thread) instead of pinning it.
 *    Complements JDK 24's fix for synchronized blocks (JEP 491).
 * <p>
 * 4. Unicode 17.0 support (JDK-8346944)
 *    String, Character, regex, and all character-processing APIs updated.
 * <p>
 * 5. Dark theme for Javadoc (JDK-8342705)
 *    Toggle via sun/moon icon in the Javadoc menu bar.
 * <p>
 * 6. Compact Object Headers — promoted from experimental (JEP 450 / JDK 25 experimental)
 *    Object headers compressed: 128 bits → 64 bits on 64-bit JVMs.
 *    Significantly reduces memory footprint for allocation-heavy apps.
 *    Enable: -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders
 * <p>
 * 7. Hybrid Public Key Encryption (HPKE — RFC 9180)
 *    New "HPKE" Cipher algorithm combining KEM + KDF + AEAD.
 *    New HPKEParameterSpec for algorithm selection and mode configuration.
 */
public class BeyondJeps {

  static void main() {
    demonstrateThreadStopRemoval();
    printJvmInfo();
  }

  // Thread.stop() was removed — attempting to call it no longer compiles in Java 26.
  // This method documents the timeline:
  //   JDK 1.2 (1998):  Thread.stop() deprecated
  //   JDK 18   (2022): deprecated for removal
  //   JDK 20   (2023): throws UnsupportedOperationException at runtime
  //   JDK 26   (2026): method removed entirely
  static void demonstrateThreadStopRemoval() {
    System.out.println("=== Thread.stop() Removal ===");
    System.out.println("Thread.stop() was removed in Java 26 after 27+ years of deprecation.");
    System.out.println("Use Thread.interrupt() for cooperative thread cancellation instead.");
    System.out.println();

    // Proper alternative: use interrupt()
    Thread worker = new Thread(() -> {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          System.out.println("  Worker running...");
          Thread.sleep(100);
        }
        System.out.println("  Worker stopped gracefully via interrupt.");
      } catch (InterruptedException e) {
        System.out.println("  Worker interrupted.");
        Thread.currentThread().interrupt(); // restore interrupted status
      }
    });

    worker.start();
    try { Thread.sleep(250); } catch (InterruptedException ignored) {}
    worker.interrupt(); // cooperative cancellation — the right way
    try { worker.join(); } catch (InterruptedException ignored) {}
  }

  // Show JVM startup / heap info
  //  java -XX:+PrintFlagsFinal -version 2>&1 | grep InitialHeapSize
  static void printJvmInfo() {
    System.out.println("\n=== JVM Info ===");

    Runtime rt = Runtime.getRuntime();
    long maxHeap  = rt.maxMemory()   / (1024 * 1024);
    long totalHeap = rt.totalMemory() / (1024 * 1024);
    long freeHeap = rt.freeMemory()  / (1024 * 1024);

    System.out.println("Java version:  " + System.getProperty("java.version"));
    System.out.println("Max heap:      " + maxHeap + " MB");
    System.out.println("Initial heap:  " + totalHeap + " MB  ← reduced from 1/64 to 1/500 of RAM in JDK 26");
    System.out.println("Free heap:     " + freeHeap + " MB");
    System.out.println();
    System.out.println("Compact Object Headers (experimental): -XX:+UseCompactObjectHeaders");
    System.out.println("  Compresses Java object headers from 128 bits → 64 bits.");
    System.out.println("  Reduces memory footprint significantly for allocation-heavy apps.");
  }
}
