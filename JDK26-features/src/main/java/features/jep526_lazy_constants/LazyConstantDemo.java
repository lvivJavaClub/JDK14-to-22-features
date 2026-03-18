package features.jep526_lazy_constants;

/**
 * Demo 1: LazyConstant<T>
 * <p>
 * App config loaded from environment once, on first use.
 * Classic singleton pattern — without LazyConstant requires Double-Checked Locking boilerplate.
 * <p>
 * Requires: --enable-preview
 */
public class LazyConstantDemo {

  private final LazyConstant<AppConfig> config = LazyConstant.of(AppConfig::load);

  static void main() {
    var demo = new LazyConstantDemo();

    System.out.println("=== LazyConstant<T> — app config ===");
    System.out.println("App started — config not loaded yet.");

    System.out.println("Checking feature flag...");
    System.out.println("darkModeEnabled: " + demo.config.get().darkModeEnabled()); // triggers load

    System.out.println("Checking max connections...");
    System.out.println("maxConnections: " + demo.config.get().maxConnections()); // cached, no reload
  }

  record AppConfig(boolean darkModeEnabled, int maxConnections) {
    static AppConfig load() {
      System.out.println("  [LazyConstant] Loading config from environment...");
      boolean dark = Boolean.parseBoolean(System.getenv().getOrDefault("DARK_MODE", "true"));
      int maxConn  = Integer.parseInt(System.getenv().getOrDefault("MAX_CONNECTIONS", "10"));
      return new AppConfig(dark, maxConn);
    }
  }
}
