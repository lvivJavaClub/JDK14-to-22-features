package features.jep529_vector_api;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import java.util.Random;

/**
 * JEP 529: Vector API (Eleventh Incubator)
 * <p>
 * SIMD: process SPECIES.length() pixels per CPU instruction instead of one at a time.
 *   x64: SSE2 / AVX / AVX-512
 *   ARM: NEON / SVE
 * <p>
 * Scenario: image brightness adjustment — clamp(pixel + delta, 0.0, 1.0)
 * Pixels are floats normalized to [0.0, 1.0].
 * <p>
 * Still incubating — waiting for Project Valhalla value types (JEP 401).
 * No API changes from Java 25.
 * <p>
 * Requires: --add-modules=jdk.incubator.vector
 */
public class VectorApp {

  static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

  void main() {
    System.out.println("Floats per CPU instruction: " + SPECIES.length());

    demonstrateBrightness();
    benchmark();
    benchmarkLuminance();
  }

  // Small 5x3 image — easy to verify the math by eye
  static void demonstrateBrightness() {
    System.out.println("\n=== Brightness +30% (5×3 grayscale image) ===");

    float[] pixels = {
        0.10f, 0.20f, 0.50f, 0.80f, 0.90f,
        0.00f, 0.30f, 0.60f, 0.85f, 1.00f,
        0.15f, 0.40f, 0.55f, 0.70f, 0.95f
    };
    float[] result = new float[pixels.length];

    vectorBrightness(pixels, result, +0.30f);

    System.out.println("Original:  " + rowToString(pixels, 0, 5));
    System.out.println("Brightened:" + rowToString(result, 0, 5));
    System.out.println("           (values clamped at 1.0)");
  }

  // Benchmark: scalar vs vector on a full HD image
  static void benchmark() {
    System.out.println("\n=== Benchmark: 1920×1080 image (" + (1920 * 1080) + " pixels) ===");

    int size = 1920 * 1080;
    float[] pixels = new float[size];
    float[] result = new float[size];
    Random rng = new Random(42);
    for (int i = 0; i < size; i++) pixels[i] = rng.nextFloat();

    // Warm up JIT
    for (int w = 0; w < 20; w++) {
      scalarBrightness(pixels, result, 0.2f);
      vectorBrightness(pixels, result, 0.2f);
    }

    int rounds = 20;

    long t0 = System.nanoTime();
    for (int r = 0; r < rounds; r++) scalarBrightness(pixels, result, 0.2f);
    long scalarMs = (System.nanoTime() - t0) / 1_000_000;

    t0 = System.nanoTime();
    for (int r = 0; r < rounds; r++) vectorBrightness(pixels, result, 0.2f);
    long vectorMs = (System.nanoTime() - t0) / 1_000_000;

    System.out.printf("Scalar: %d ms%n", scalarMs);
    System.out.printf("Vector: %d ms  →  %.1fx faster%n", vectorMs, (double) scalarMs / vectorMs);
  }

  static void scalarBrightness(float[] src, float[] dst, float delta) {
    for (int i = 0; i < src.length; i++)
      dst[i] = Math.clamp(src[i] + delta, 0.0f, 1.0f);
  }

  // Same logic — but SPECIES.length() pixels per iteration instead of 1
  static void vectorBrightness(float[] src, float[] dst, float delta) {
    var vDelta = FloatVector.broadcast(SPECIES, delta);
    var vZero  = FloatVector.broadcast(SPECIES, 0.0f);
    var vOne   = FloatVector.broadcast(SPECIES, 1.0f);

    int i = 0;
    for (; i < SPECIES.loopBound(src.length); i += SPECIES.length()) {
      FloatVector.fromArray(SPECIES, src, i)
          .add(vDelta).max(vZero).min(vOne)
          .intoArray(dst, i);
    }
    for (; i < src.length; i++) // scalar tail
      dst[i] = Math.clamp(src[i] + delta, 0.0f, 1.0f);
  }

  // Luminance reads from 3 independent arrays — JIT can't auto-vectorize this as easily.
  // This is where Vector API shows a genuine speedup even before Valhalla.
  //   ITU-R BT.601: luminance = 0.299*R + 0.587*G + 0.114*B
  static void benchmarkLuminance() {
    System.out.println("\n=== Benchmark: luminance from RGB channels (3 sources) ===");

    int size = 1920 * 1080;
    Random rng = new Random(42);
    float[] r   = new float[size];
    float[] g   = new float[size];
    float[] b   = new float[size];
    float[] dst = new float[size];
    for (int i = 0; i < size; i++) { r[i] = rng.nextFloat(); g[i] = rng.nextFloat(); b[i] = rng.nextFloat(); }

    // Warm up JIT
    for (int w = 0; w < 20; w++) {
      scalarLuminance(r, g, b, dst);
      vectorLuminance(r, g, b, dst);
    }

    int rounds = 20;

    long t0 = System.nanoTime();
    for (int round = 0; round < rounds; round++) scalarLuminance(r, g, b, dst);
    long scalarMs = (System.nanoTime() - t0) / 1_000_000;

    t0 = System.nanoTime();
    for (int round = 0; round < rounds; round++) vectorLuminance(r, g, b, dst);
    long vectorMs = (System.nanoTime() - t0) / 1_000_000;

    System.out.printf("Scalar: %d ms%n", scalarMs);
    System.out.printf("Vector: %d ms  →  %.1fx faster%n", vectorMs, (double) scalarMs / vectorMs);
  }

  static void scalarLuminance(float[] r, float[] g, float[] b, float[] dst) {
    for (int i = 0; i < r.length; i++)
      dst[i] = 0.299f * r[i] + 0.587f * g[i] + 0.114f * b[i];
  }

  static void vectorLuminance(float[] r, float[] g, float[] b, float[] dst) {
    var vR = FloatVector.broadcast(SPECIES, 0.299f);
    var vG = FloatVector.broadcast(SPECIES, 0.587f);
    var vB = FloatVector.broadcast(SPECIES, 0.114f);

    int i = 0;
    for (; i < SPECIES.loopBound(r.length); i += SPECIES.length()) {
      FloatVector.fromArray(SPECIES, r, i).mul(vR)
          .add(FloatVector.fromArray(SPECIES, g, i).mul(vG))
          .add(FloatVector.fromArray(SPECIES, b, i).mul(vB))
          .intoArray(dst, i);
    }
    for (; i < r.length; i++) // scalar tail
      dst[i] = 0.299f * r[i] + 0.587f * g[i] + 0.114f * b[i];
  }

  static String rowToString(float[] arr, int from, int count) {
    var sb = new StringBuilder("[");
    for (int i = from; i < from + count; i++) {
      if (i > from) sb.append(", ");
      sb.append(String.format("%.2f", arr[i]));
    }
    return sb.append("]").toString();
  }
}
