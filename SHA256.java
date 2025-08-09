public class SHA256 {

    private static final int[] K = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
        0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
        0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
        0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
        0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
        0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
        0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
        0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
        0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
    };

    private static final int[] H0_INIT = {
        0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
        0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    };

   tatic String sha256(String message) {
        byte[] msg = null;
        try {
            msg = message.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            msg = message.getBytes();
        }

        long bitLen = (long) msg.length * 8L;
        int padLen = (int) ((56 - (msg.length + 1) % 64 + 64) % 64); 
        int totalLen = msg.length + 1 + padLen + 8;
        byte[] padded = new byte[totalLen];

        System.arraycopy(msg, 0, padded, 0, msg.length);
        padded[msg.length] = (byte) 0x80;

        for (int i = 0; i < 8; i++) {
            padded[totalLen - 1 - i] = (byte) ((bitLen >>> (8 * i)) & 0xFF);
        }

        int[] H = new int[8];
        System.arraycopy(H0_INIT, 0, H, 0, 8);

        int numBlocks = padded.length / 64;

        for (int block = 0; block < numBlocks; block++) {
            int[] W = new int[64];

            int base = block * 64;
            for (int t = 0; t < 16; t++) {
                int i = base + t * 4;
                W[t] = ((padded[i] & 0xFF) << 24)
                     | ((padded[i + 1] & 0xFF) << 16)
                     | ((padded[i + 2] & 0xFF) << 8)
                     | ((padded[i + 3] & 0xFF));
            }

            for (int t = 16; t < 64; t++) {
                int s0 = Integer.rotateRight(W[t - 15], 7) ^ Integer.rotateRight(W[t - 15], 18) ^ (W[t - 15] >>> 3);
                int s1 = Integer.rotateRight(W[t - 2], 17) ^ Integer.rotateRight(W[t - 2], 19) ^ (W[t - 2] >>> 10);
                W[t] = addMod32(addMod32(addMod32(W[t - 16], s0), W[t - 7]), s1);
            }

            int a = H[0];
            int b = H[1];
            int c = H[2];
            int d = H[3];
            int eVar = H[4];
            int f = H[5];
            int g = H[6];
            int h = H[7];

            for (int t = 0; t < 64; t++) {
                int S1 = Integer.rotateRight(eVar, 6) ^ Integer.rotateRight(eVar, 11) ^ Integer.rotateRight(eVar, 25);
                int ch = (eVar & f) ^ ((~eVar) & g);
                int temp1 = addMod32(addMod32(addMod32(addMod32(h, S1), ch), K[t]), W[t]);
                int S0 = Integer.rotateRight(a, 2) ^ Integer.rotateRight(a, 13) ^ Integer.rotateRight(a, 22);
                int maj = (a & b) ^ (a & c) ^ (b & c);
                int temp2 = addMod32(S0, maj);

                h = g;
                g = f;
                f = eVar;
                eVar = addMod32(d, temp1);
                d = c;
                c = b;
                b = a;
                a = addMod32(temp1, temp2);
            }

            H[0] = addMod32(H[0], a);
            H[1] = addMod32(H[1], b);
            H[2] = addMod32(H[2], c);
            H[3] = addMod32(H[3], d);
            H[4] = addMod32(H[4], eVar);
            H[5] = addMod32(H[5], f);
            H[6] = addMod32(H[6], g);
            H[7] = addMod32(H[7], h);
        }

        return toHex(H);
    }

    private static int addMod32(int a, int b) {
        return a + b;
    }

    private static String toHex(int[] H) {
        char[] hexChars = "0123456789abcdef".toCharArray();
        StringBuilder sb = new StringBuilder(64);
        for (int h : H) {
            for (int shift = 28; shift >= 0; shift -= 4) {
                int nibble = (h >>> shift) & 0xF;
                sb.append(hexChars[nibble]);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String[] tests = {
            "",
            "abc",
            "The quick brown fox jumps over the lazy dog",
            "The quick brown fox jumps over the lazy dog."
        };

        for (String t : tests) {
            System.out.println("\"" + t + "\" ->");
            System.out.println(sha256(t));
            System.out.println();
        }

        String custom = "hello world";
        System.out.println("sha256(\"" + custom + "\") = " + sha256(custom));
    }
}
