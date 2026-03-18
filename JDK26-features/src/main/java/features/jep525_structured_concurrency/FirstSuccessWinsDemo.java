package features.jep525_structured_concurrency;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

/**
 * Demo 2: Joiner.anySuccessfulOrThrow()
 * <p>
 * First success wins — remaining subtasks are cancelled immediately.
 * Useful for redundant service calls: whoever responds first wins.
 * Renamed from anySuccessfulResultOrThrow() in JDK 25.
 * <p>
 * Requires: --enable-preview
 */
public class FirstSuccessWinsDemo {

  static void main() throws Exception {
    System.out.println("=== anySuccessfulOrThrow — first success wins ===");

    try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulOrThrow())) {
      scope.fork(() -> fetchFromServiceA("data")); // slowest
      scope.fork(() -> fetchFromServiceB("data")); // fastest — wins
      scope.fork(() -> fetchFromServiceC("data"));

      String result = scope.join(); // returns first successful result directly
      System.out.println("Winner: " + result);
    }
  }

  static String fetchFromServiceA(String query) throws InterruptedException {
    Thread.sleep(1200);
    return "Result from Service A for: " + query;
  }

  static String fetchFromServiceB(String query) throws InterruptedException {
    Thread.sleep(400);
    return "Result from Service B for: " + query;
  }

  static String fetchFromServiceC(String query) throws InterruptedException {
    Thread.sleep(800);
    return "Result from Service C for: " + query;
  }
}
