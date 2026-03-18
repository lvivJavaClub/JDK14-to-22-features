package features.jep526_lazy_constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Demo 2: Map.ofLazy
 * <p>
 * DateTimeFormatter per locale — building a formatter is non-trivial,
 * and most apps only ever use 2-3 locales out of many supported ones.
 * Formatters are built on first access per key; unused keys are never built.
 * <p>
 * Requires: --enable-preview
 */
public class LazyMapDemo {

  private final Map<Locale, DateTimeFormatter> dateFormatters = Map.ofLazy(
      Set.of(Locale.US, Locale.GERMANY, Locale.of("uk", "UA")),
      locale -> {
        System.out.println("  [LazyMap] Building DateTimeFormatter for " + locale);
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale);
      }
  );

  static void main() {
    var demo = new LazyMapDemo();
    var now = LocalDateTime.now();

    System.out.println("=== Map.ofLazy — DateTimeFormatter per locale ===");
    System.out.println("Map created — no formatters built yet.\n");

    // Only US and UA formatters are built — GERMANY is never touched
    System.out.println("US: "  + demo.dateFormatters.get(Locale.US).format(now));
    System.out.println("UA: "  + demo.dateFormatters.get(Locale.of("uk", "UA")).format(now));
    System.out.println("US again: " + demo.dateFormatters.get(Locale.US).format(now)); // cached

    System.out.println("\nGERMANY formatter was never needed → never built.");
  }
}
