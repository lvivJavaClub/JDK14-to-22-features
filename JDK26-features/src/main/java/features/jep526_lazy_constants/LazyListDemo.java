package features.jep526_lazy_constants;

import java.util.List;

/**
 * Demo 3: List.ofLazy
 * <p>
 * Paginated order history — 50 pages, each loaded from DB only when accessed.
 * User typically views page 0, maybe page 1 — pages 2–49 are never loaded.
 * <p>
 * Requires: --enable-preview
 */
public class LazyListDemo {

  private static final int PAGE_SIZE = 20;

  private final List<List<String>> orderPages = List.ofLazy(50, page -> {
    System.out.println("  [LazyList] Loading page " + page + " from DB (offset " + (page * PAGE_SIZE) + ")...");
    var orders = new java.util.ArrayList<String>();
    for (int i = 0; i < PAGE_SIZE; i++)
      orders.add("Order#" + (page * PAGE_SIZE + i + 1));
    return List.copyOf(orders);
  });

  static void main() {
    var demo = new LazyListDemo();

    System.out.println("=== List.ofLazy — paginated order history (50 pages × 20 orders) ===");
    System.out.println("List created — no DB queries yet.\n");

    System.out.println("User opens page 0:");
    demo.orderPages.get(0).stream().limit(3).forEach(o -> System.out.println("  " + o));
    System.out.println("  ...");

    System.out.println("\nUser opens page 1:");
    demo.orderPages.get(1).stream().limit(3).forEach(o -> System.out.println("  " + o));
    System.out.println("  ...");

    System.out.println("\nUser opens page 0 again — no DB query:");
    demo.orderPages.get(0).stream().limit(3).forEach(o -> System.out.println("  " + o));

    System.out.println("\nPages 2–49 were never accessed → never loaded from DB.");
  }
}
