import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private Socket sk;
    private BufferedReader in;
    private PrintWriter out;
    private int e, n; 
    private int d, cn; 

    public Server() throws IOException {
        System.out.println("Waiting for client connection...");
        ServerSocket ss = new ServerSocket(7796);
        sk = ss.accept();
        System.out.println("Client connected.");

        in = new BufferedReader(new InputStreamReader(sk.getInputStream()));
        out = new PrintWriter(sk.getOutputStream(), true);

        initRSAKeys();
        exchangeKeys();
        readMsg();
        writeMsg();
    }

    private void initRSAKeys() {
        Random rnd = new Random();
        int p = genPrime(rnd);
        int q = genPrime(rnd);
        cn = p * q;
        int phi = lcm(p - 1, q - 1);

        List<Integer> coPrimes = new ArrayList<>();
        for (int i = 3; i < phi; i++) {
            if (gcd(i, phi) == 1) coPrimes.add(i);
        }

        e = coPrimes.get(rnd.nextInt(coPrimes.size()));
        d = modInv(e, phi);
    }

    private void exchangeKeys() throws IOException {
        String[] cKey = in.readLine().split(",");
        int clientE = Integer.parseInt(cKey[0]);
        int clientN = Integer.parseInt(cKey[1]);

        out.println(e + "," + cn);

        e = clientE;
        n = clientN;
    }

    private void readMsg() {
        new Thread(() -> {
            System.out.println("Reader ready.");
            while (true) {
                try {
                    String encMsg = in.readLine();
                    if (encMsg == null || encMsg.equalsIgnoreCase("exit")) {
                        System.out.println("Chat closed.");
                        break;
                    }
                    String decMsg = decrypt(encMsg);
                    System.out.println("Client: " + decMsg);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private void writeMsg() {
        new Thread(() -> {
            System.out.println("Writer ready.");
            try (BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    String msg = userIn.readLine();
                    String encMsg = encrypt(msg);
                    out.println(encMsg);
                    out.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private String encrypt(String msg) {
        StringBuilder enc = new StringBuilder();
        for (char c : msg.toCharArray()) {
            enc.append(modExp(c, e, n)).append(" ");
        }
        return enc.toString().trim();
    }

    private String decrypt(String encMsg) {
        StringBuilder dec = new StringBuilder();
        for (String part : encMsg.split(" ")) {
            dec.append((char) modExp(Integer.parseInt(part), d, cn));
        }
        return dec.toString();
    }

    public static void main(String[] args) {
        System.out.println("Server started...");
        try {
            new Server();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private int modExp(int base, int exp, int mod) {
        long res = 1, pow = base % mod;
        while (exp > 0) {
            if ((exp & 1) == 1) res = (res * pow) % mod;
            pow = (pow * pow) % mod;
            exp >>= 1;
        }
        return (int) res;
    }

    private int modInv(int a, int m) {
        int m0 = m, x = 1, y = 0;
        while (a > 1) {
            int q = a / m, t = m;
            m = a % m;
            a = t;
            t = y;
            y = x - q * y;
            x = t;
        }
        return x < 0 ? x + m0 : x;
    }

    private int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    private int lcm(int a, int b) {
        return (a * b) / gcd(a, b);
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private int genPrime(Random rnd) {
        while (true) {
            int num = rnd.nextInt(100) + 50;
            if (isPrime(num)) return num;
        }
    }
}
