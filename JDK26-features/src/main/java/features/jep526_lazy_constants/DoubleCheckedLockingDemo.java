package features.jep526_lazy_constants;

/**
 * Before LazyConstant: Double-Checked Locking pattern
 * <p>
 * Classic thread-safe lazy initialization boilerplate.
 * Problems:
 *   - volatile required to prevent CPU reordering
 *   - two null-checks needed (outer without lock, inner with lock)
 *   - synchronized block is easy to forget or get wrong
 *   - hard to read and reason about
 * <p>
 * JEP 526 (LazyConstant) replaces all of this with a single line:
 *   private final LazyConstant&lt;AppConfig&gt; config = LazyConstant.of(AppConfig::load);
 */
public class DoubleCheckedLockingDemo {

  // volatile is mandatory — without it, another thread can see a
  // partially constructed object due to CPU/compiler reordering
  private volatile AppConfig config;

  AppConfig getConfig() {
    if (config == null) {                          // 1st check — no lock (fast path)
      synchronized (this) {
        if (config == null) {                      // 2nd check — with lock (slow path)
          config = AppConfig.load();
        }
      }
    }
    return config;
  }

  static void main() {
    var demo = new DoubleCheckedLockingDemo();

    System.out.println("=== Double-Checked Locking (before LazyConstant) ===");
    System.out.println("Object created — config not loaded yet.");

    System.out.println("Checking feature flag...");
    System.out.println("darkModeEnabled: " + demo.getConfig().darkModeEnabled()); // triggers load

    System.out.println("Checking max connections...");
    System.out.println("maxConnections: " + demo.getConfig().maxConnections()); // cached

    System.out.println("\nSame result as LazyConstant — but requires volatile + synchronized + 2 null-checks.");
  }

  record AppConfig(boolean darkModeEnabled, int maxConnections) {
    static AppConfig load() {
      System.out.println("  [DCL] Loading config from environment...");
      boolean dark = Boolean.parseBoolean(System.getenv().getOrDefault("DARK_MODE", "true"));
      int maxConn  = Integer.parseInt(System.getenv().getOrDefault("MAX_CONNECTIONS", "10"));
      return new AppConfig(dark, maxConn);
    }
  }
}