package features.jep525_structured_concurrency;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * Demo 3: Joiner.allUntil(predicate)
 * <p>
 * Stop as soon as the predicate on a completed subtask is true.
 * Useful for search: scan multiple sources, stop when a match is found.
 * join() returns List<Subtask<T>> (changed from Stream in JDK 25).
 * <p>
 * Requires: --enable-preview
 */
public class StopWhenFoundDemo {

  // new Joiner strategy scaffold
  class CustomJoinStrategy implements Joiner<String, String>{

    @Override
    public boolean onComplete(Subtask<String> subtask) {
        switch (subtask.state()) {

        }
      return false;
    }

    @Override
    public String result() throws Throwable {
      return "";
    }
  }
  static void main() throws Exception {
    System.out.println("=== allUntil — stop when first match is found ===");

    try (var scope = StructuredTaskScope.open(Joiner.allUntil(StopWhenFoundDemo::foundJava26))) {
      scope.fork(() -> searchSource("Wikipedia",    400));
      scope.fork(() -> searchSource("StackOverflow", 1200));
      scope.fork(() -> searchSource("GitHub",        800)); // finds it first

      List<Subtask<String>> subtasks = scope.join();

      subtasks.stream()
          .filter(t -> t.state() == Subtask.State.SUCCESS)
          .map(Subtask::get)
          .forEach(r -> System.out.println("Found: " + r));
    }
  }

  static boolean foundJava26(Subtask<String> t) {
    return t.state() == Subtask.State.SUCCESS && t.get().contains("Java 26");
  }

  static String searchSource(String source, int delayMs) throws InterruptedException {
    Thread.sleep(delayMs);
    if (source.equals("GitHub")) {
      return source + ": found 'Java 26' in 42 articles";
    }
    return source + ": nothing";
  }
}
