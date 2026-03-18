package features.jep500_make_final_mean_final;

import java.lang.reflect.Field;

/**
 * JEP 500: Prepare to Make Final Mean Final
 * <p>
 * In Java 26, mutating a `final` field via deep reflection now emits a JVM warning by default:
 *   WARNING: Final field value in class ...Box has been mutated reflectively by ...
 * <p>
 * VM options (--illegal-final-field-mutation=<value>):
 *   allow  — old behavior, no warning (pre-Java 26)
 *   warn   — DEFAULT in Java 26, warns on first mutation per field
 *   debug  — warns on every mutation (with stack trace)
 *   deny   — throws IllegalAccessException (will become default in a future JDK)
 * <p>
 * To suppress per module: --enable-final-field-mutation=ALL-UNNAMED
 * <p>
 * New JFR event: jdk.FinalFieldMutation — captures every violation with a stack trace.
 */
public class FinalMutationApp {

  static class Box {
    private final Object value;
    Box(Object value) { this.value = value; }
    public String toString() { return "Box{value=" + value + "}"; }
  }

  static void main() throws Exception {
    var box = new Box("Rubik's Cube");
    System.out.println("Before: " + box);

    // Mutate a final field via deep reflection.
    // Java 26 emits a WARNING here (warn mode is default).
    // A future JDK will throw IllegalAccessException when deny becomes default.
    Field valueField = Box.class.getDeclaredField("value");
    valueField.setAccessible(true);
    valueField.set(box, "Magic");

    System.out.println("After:  " + box);
  }
}
