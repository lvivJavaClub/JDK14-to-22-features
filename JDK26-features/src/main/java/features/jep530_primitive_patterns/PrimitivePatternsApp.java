package features.jep530_primitive_patterns;

/**
 * JEP 530: Primitive Types in Patterns, instanceof, and switch (Fourth Preview)
 * <p>
 * Allows ALL primitive types in pattern contexts:
 *   - instanceof with primitives
 *   - switch case labels with primitive type patterns and guards (when)
 *   - All primitives now allowed in switch: long, double, float, boolean
 * <p>
 * A pattern like `case byte b` on a double matches if the value fits exactly in a byte.
 * Dominance rules enforced at compile time — order of case labels matters.
 * <p>
 * Changes in Java 26 (vs Java 25):
 *   - Stricter dominance checks — some previously-valid constructs now produce compile errors:
 *       case float f → case 16_777_216 → ERROR (16_777_216 fits exactly in float → dominated)
 *       case int _   → case float _    → ERROR (all int values already matched by int _)
 *       case short s → case 42         → ERROR (42 fits in short → dominated by short s)
 * <p>
 * Requires: --enable-preview
 */
public class PrimitivePatternsApp {

  static void main() {
    httpStatusSwitch();
    // precisionFitSwitch();
    // instanceofPrimitives();
    // booleanSwitch();
  }

  // Range-based patterns on int — replaces chains of if-else
  static void httpStatusSwitch() {
    System.out.println("=== HTTP Status Ranges (int pattern with guard) ===");

    for (int code : new int[]{100, 201, 302, 404, 500, 999}) {
      String category = switch (code) {
        case int i when i >= 100 && i < 200 -> "1xx Informational";
        case int i when i >= 200 && i < 300 -> "2xx Success";
        case int i when i >= 300 && i < 400 -> "3xx Redirection";
        case int i when i >= 400 && i < 500 -> "4xx Client Error";
        case int i when i >= 500 && i < 600 -> "5xx Server Error";
        default -> "Unknown (" + code + ")";
      };
      System.out.println(code + " → " + category);
    }
  }

  // Precision-fitting: does a double value fit exactly in smaller types?
  static void precisionFitSwitch() {
    System.out.println("\n=== Precision-Fitting Patterns (double → narrower types) ===");

    for (double value : new double[]{42.0, 50_000.0, 65_000.0, 0.5, 0.1}) {
      String fit = switch (value) {
        case byte   b -> value + " fits in byte: "   + b;
        case short  s -> value + " fits in short: "  + s;
        case char   c -> value + " fits in char: "   + (int) c;
        case int    i -> value + " fits in int: "    + i;
        case long   l -> value + " fits in long: "   + l;
        case float  f -> value + " fits in float: "  + f;
        case double d -> value + " only fits double: " + d;
      };
      System.out.println(fit);
    }
  }

  // instanceof with primitives — no boxing required
  static void instanceofPrimitives() {
    System.out.println("\n=== instanceof with Primitives ===");

    long[] values = {42L, 100_000L, 3_000_000_000L};

    for (long val : values) {
      if (val instanceof byte b) {
        System.out.println(val + " fits in byte: " + b);
      } else if (val instanceof short s) {
        System.out.println(val + " fits in short: " + s);
      } else if (val instanceof int i) {
        System.out.println(val + " fits in int: " + i);
      } else {
        System.out.println(val + " only fits in long");
      }
    }
  }

  // boolean in switch — new in JDK 26 preview
  static void booleanSwitch() {
    System.out.println("\n=== Boolean in Switch ===");

    for (boolean flag : new boolean[]{true, false}) {
      String msg = switch (flag) {
        case true  -> "Feature enabled";
        case false -> "Feature disabled";
      };
      System.out.println(flag + " → " + msg);
    }
  }
}
