package features.jep525_structured_concurrency;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * Demo 1: Joiner.allSuccessfulOrThrow()
 * <p>
 * All subtasks must succeed. One failure cancels everything and propagates the exception.
 * join() returns List<T> directly (changed from Stream<Subtask<T>> in JDK 25).
 * <p>
 * Requires: --enable-preview
 */
public class AllMustSucceedDemo {

  static void main() throws Exception {
    System.out.println("=== allSuccessfulOrThrow — all must succeed ===");

    try (var scope = StructuredTaskScope.open()) {
      Subtask<String> weatherTask = scope.fork(() -> fetchWeather("Lviv"));
      Subtask<String> newsTask    = scope.fork(() -> fetchNews("Technology"));
      Subtask<String> rateTask    = scope.fork(() -> fetchExchangeRate("USD/UAH"));

      scope.join(); // waits for all; cancels all and propagates exception if any fails

      System.out.println("Weather: " + weatherTask.get());
      System.out.println("News:    " + newsTask.get());
      System.out.println("Rate:    " + rateTask.get());
    }
  }

  static String fetchWeather(String city) throws InterruptedException {
    Thread.sleep(800);
    return "Sunny, 5°C in " + city;
  }

  static String fetchNews(String category) throws InterruptedException {
    Thread.sleep(600);
    return "Latest " + category + " news: Java 26 released!";
  }

  static String fetchExchangeRate(String pair) throws InterruptedException {
    Thread.sleep(400);
    return pair + " = 41.5";
  }
}
