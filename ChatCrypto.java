import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.StringJoiner;

public final class ChatCrypto {
    private static final int PRIME_BITS = 256;
    private static final BigInteger PUBLIC_EXPONENT = BigInteger.valueOf(65537);
    private static final int FINGERPRINT_LENGTH = 16;

    private ChatCrypto() {
    }

    public static final class RsaKeyPair {
        private final BigInteger publicExponent;
        private final BigInteger privateExponent;
        private final BigInteger modulus;

        private RsaKeyPair(BigInteger publicExponent, BigInteger privateExponent, BigInteger modulus) {
            this.publicExponent = publicExponent;
            this.privateExponent = privateExponent;
            this.modulus = modulus;
        }

        public BigInteger getPublicExponent() {
            return publicExponent;
        }

        public BigInteger getPrivateExponent() {
            return privateExponent;
        }

        public BigInteger getModulus() {
            return modulus;
        }
    }

    public static RsaKeyPair generateKeyPair(SecureRandom random) {
        while (true) {
            BigInteger p = BigInteger.probablePrime(PRIME_BITS, random);
            BigInteger q = BigInteger.probablePrime(PRIME_BITS, random);
            BigInteger modulus = p.multiply(q);
            BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

            if (PUBLIC_EXPONENT.gcd(phi).equals(BigInteger.ONE)) {
                BigInteger privateExponent = PUBLIC_EXPONENT.modInverse(phi);
                return new RsaKeyPair(PUBLIC_EXPONENT, privateExponent, modulus);
            }
        }
    }

    public static String encrypt(String message, BigInteger exponent, BigInteger modulus) {
        StringJoiner joiner = new StringJoiner(" ");
        message.codePoints()
                .mapToObj(codePoint -> BigInteger.valueOf(codePoint).modPow(exponent, modulus).toString())
                .forEach(joiner::add);
        return joiner.toString();
    }

    public static String decrypt(String encryptedMessage, BigInteger exponent, BigInteger modulus) {
        if (encryptedMessage == null || encryptedMessage.isBlank()) {
            return "";
        }

        StringBuilder plainText = new StringBuilder();
        for (String token : encryptedMessage.trim().split("\\s+")) {
            int codePoint = new BigInteger(token).modPow(exponent, modulus).intValue();
            plainText.appendCodePoint(codePoint);
        }
        return plainText.toString();
    }

    public static String fingerprint(BigInteger exponent, BigInteger modulus) {
        String material = exponent + ":" + modulus;
        String digest = SHA256.sha256(material);
        return digest.substring(0, Math.min(FINGERPRINT_LENGTH, digest.length()));
    }
}