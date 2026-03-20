package features.jep517_http3;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpOption;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * JEP 517: HTTP/3 for the HTTP Client API
 * <p>
 * java.net.http.HttpClient (JDK 11+) now supports HTTP/3, which runs over QUIC/UDP.
 * Default remains HTTP/2 — HTTP/3 must be explicitly requested.
 * Falls back transparently to HTTP/2 if the server does not support HTTP/3.
 * <p>
 * New: HttpClient.Version.HTTP_3 enum constant.
 * <p>
 * Non-JEP change: HttpRequest.BodyPublishers.ofFileChannel(FileChannel, long, long)
 *   — stream large file uploads without loading the entire file into heap memory.
 * <p>
 * Non-JEP change: HttpRequest.Builder::timeout now covers the full request/response
 *   lifecycle including body consumption (not just until headers arrive).
 */
public class Http3App {

  void main() throws Exception {
    demonstrateDefaultHttp2();
    demonstrateHttp3Request();
    demonstrateFileChannelPublisher();
  }

  // Default behavior — HTTP/2 (unchanged from earlier JDKs)
  static void demonstrateDefaultHttp2() throws Exception {
    System.out.println("=== Default: HTTP/2 ===");
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://httpbin.org/get"))
          .build();

      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      System.out.println("Status: " + response.statusCode() + " | Version: " + response.version());
    }
  }

  // Explicitly opt in to HTTP/3 (falls back to HTTP/2 if server doesn't support it)
  static void demonstrateHttp3Request() throws Exception {
    System.out.println("\n=== HTTP/3 request (with automatic fallback) ===");

    // Option 1: set version at client level
    // Note: HTTP/3 requires UDP port 443 — blocked on some networks → falls back to HTTP/2
    try (HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_3)
        .build()) {

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://cloudflare.com"))
          .version(HttpClient.Version.HTTP_3)
          .build();

      try {
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode() + " | Version: " + response.version());
        // Server advertises HTTP/3 support via Alt-Svc header — JVM caches this for next request
        response.headers().firstValue("alt-svc").ifPresent(v ->
            System.out.println("Alt-Svc: " + v + "  ← JVM caches this, next request uses HTTP/3"));
      } catch (Exception e) {
        System.out.println("HTTP/3 failed (UDP 443 likely blocked): " + e.getMessage());
        System.out.println("→ This is expected on restricted networks. HTTP/3 needs QUIC/UDP port 443.");
      }
    }

    // Option 2: set version per request — falls back gracefully to HTTP/2 if HTTP/3 unavailable
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://cloudflare.com"))
          .version(HttpClient.Version.HTTP_3)
          //.setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ALT_SVC)
          //.setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.HTTP_3_URI_ONLY)
          .build();

      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      System.out.println("Per-request HTTP/3 — version used: " + response.version());
    }
  }

  // New non-JEP: stream a file via FileChannel (no full heap load)
  static void demonstrateFileChannelPublisher() throws Exception {
    System.out.println("\n=== FileChannel BodyPublisher (no full heap load) ===");

    Path tempFile = Files.createTempFile("jdk26-demo-", ".bin");
    Files.write(tempFile, "Hello from FileChannel!".getBytes());

    try (FileChannel fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ);
         HttpClient client = HttpClient.newHttpClient()) {

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://httpbin.org/post"))
          .POST(HttpRequest.BodyPublishers.ofFileChannel(fileChannel, 0, fileChannel.size()))
          .build();

      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      System.out.println("Upload status: " + response.statusCode() + " (streamed via FileChannel, no heap copy)");
    }

    Files.deleteIfExists(tempFile);
  }
}
