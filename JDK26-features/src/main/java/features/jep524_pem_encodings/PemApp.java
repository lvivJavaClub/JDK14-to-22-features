package features.jep524_pem_encodings;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * JEP 524: PEM Encodings of Cryptographic Objects (Second Preview)
 * <p>
 * Introduces PEMDecoder and PEMEncoder — a clean API for encoding/decoding
 * cryptographic objects (keys, certificates, CRLs) to/from PEM format.
 * <p>
 * PEM (Privacy-Enhanced Mail) is the ubiquitous format used by TLS/SSL tooling:
 *   -----BEGIN CERTIFICATE-----
 *   MIIDtzCCAz2gAwIBAgIS...
 *   -----END CERTIFICATE-----
 * <p>
 * Requires: --enable-preview
 * <p>
 * Changes from Java 25 (first preview):
 *   - PEMRecord renamed to PEM; gains decode() returning raw Base64 bytes.
 *   - EncryptedPrivateKeyInfo.encryptKey() renamed to encrypt(); accepts DEREncodable.
 *   - Key pairs and PKCS#8-encoded keys can now be encoded/decoded.
 */
public class PemApp {

  static void main() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair keyPair = gen.generateKeyPair();

    encodeAndDecodePublicKey(keyPair.getPublic());
    encodeAndDecodePrivateKey(keyPair.getPrivate());
    encodeAndDecodeKeyPair(keyPair);

    decodeEncryptedKeyBefore();       // show the pain first
    encodeAndDecodeEncryptedPrivateKey(keyPair.getPrivate()); // then the fix
  }

  static void encodeAndDecodePublicKey(PublicKey publicKey) throws Exception {
    System.out.println("=== Public Key ===");

    // Encode to PEM
    var encoder = PEMEncoder.of();
    String pem = encoder.encodeToString(publicKey);
    System.out.println(pem);

    // Decode back
    var decoder = java.security.PEMDecoder.of();
    PublicKey decoded = decoder.decode(pem, PublicKey.class);
    System.out.println("Algorithm: " + decoded.getAlgorithm());
    System.out.println("Round-trip match: " + publicKey.equals(decoded));
  }

  static void encodeAndDecodePrivateKey(PrivateKey privateKey) throws Exception {
    System.out.println("\n=== Private Key ===");

    String pem = PEMEncoder.of().encodeToString(privateKey);
    System.out.println(pem.lines().limit(3).reduce("", (a, b) -> a + b + "\n") + "...");

    PrivateKey decoded = java.security.PEMDecoder.of().decode(pem, PrivateKey.class);
    System.out.println("Algorithm: " + decoded.getAlgorithm());
    System.out.println("Round-trip match: " + privateKey.equals(decoded));
  }

  static void encodeAndDecodeKeyPair(KeyPair keyPair) throws Exception {
    System.out.println("\n=== Key Pair (new in JDK 26 preview) ===");

    // New in JDK 26: encode/decode entire KeyPair
    String pem = PEMEncoder.of().encodeToString(keyPair);
    System.out.println("Encoded KeyPair (first line): " + pem.lines().findFirst().orElse(""));

    KeyPair decoded = java.security.PEMDecoder.of().decode(pem, KeyPair.class);
    System.out.println("Decoded key pair algorithm: " + decoded.getPublic().getAlgorithm());
  }

  // ──────────────────────────────────────────────────────────────────────────
  // BEFORE JEP 524 — decode an encrypted private key from PEM
  // ──────────────────────────────────────────────────────────────────────────
  static void decodeEncryptedKeyBefore() throws Exception {
    System.out.println("\n=== BEFORE JEP 524: decode encrypted private key ===");

    String encryptedPem = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n" +
        "MIIFHDBOBgkqhkiG9w0BBQ0wQTApBgkqhkiG9w0BBQwwHAIIeQju6gV...\n" +
        "-----END ENCRYPTED PRIVATE KEY-----";
    char[] passphrase = "s3cr3t-p@ssphrase".toCharArray();

    try {
      // Step 1: strip PEM headers and decode Base64
      String base64 = encryptedPem
          .replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "")
          .replace("-----END ENCRYPTED PRIVATE KEY-----", "")
          .replaceAll("[\\r\\n]", "");
      byte[] encryptedBytes = Base64.getDecoder().decode(base64);

      // Step 2: parse the EncryptedPrivateKeyInfo structure
      EncryptedPrivateKeyInfo encryptedInfo = new EncryptedPrivateKeyInfo(encryptedBytes);
      String algorithmName = encryptedInfo.getAlgName();

      // Step 3: derive the decryption key from the passphrase
      SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithmName);
      PBEKeySpec pbeKeySpec = new PBEKeySpec(passphrase);
      Key pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

      // Step 4: decrypt
      Cipher cipher = Cipher.getInstance(algorithmName);
      cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptedInfo.getAlgParameters());

      // Step 5: reconstruct the PrivateKey
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      KeySpec keySpec = encryptedInfo.getKeySpec(cipher);
      PrivateKey pk = keyFactory.generatePrivate(keySpec);
      System.out.println("15 lines later... algorithm: " + pk.getAlgorithm());
    } catch (Exception ignored) {
      // Truncated PEM is intentionally invalid — this block shows the structure only.
      System.out.println("15 lines later... (real PEM would yield the PrivateKey here)");
    }
    System.out.println("↓ Now watch the JEP 524 version below ↓");
  }

  // AFTER JEP 524 — same thing in 3 lines
  static void encodeAndDecodeEncryptedPrivateKey(PrivateKey privateKey) throws Exception {
    System.out.println("\n=== AFTER JEP 524: encrypted private key ===");
    char[] passphrase = "s3cr3t-p@ssphrase".toCharArray();

    // Encrypt and encode — replaces 15+ lines of Cipher/KeyFactory boilerplate
    String encryptedPem = PEMEncoder.of()
        .withEncryption(passphrase)
        .encodeToString(privateKey);
    System.out.println(encryptedPem.lines().limit(3).reduce("", (a, b) -> a + b + "\n") + "...");

    // Decode — 1 line vs 15+ lines before JEP 524
    PrivateKey decoded = java.security.PEMDecoder.of()
        .withDecryption(passphrase)
        .decode(encryptedPem, PrivateKey.class);
    System.out.println("Decrypted algorithm: " + decoded.getAlgorithm());
  }
}
