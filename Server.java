import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;

public class Server {
    private static final int PORT = 7796;
    private static final String EXIT_COMMAND = "exit";

    private Socket sk;
    private BufferedReader in;
    private PrintWriter out;
    private ChatCrypto.RsaKeyPair keyPair;
    private BigInteger peerExponent;
    private BigInteger peerModulus;
    private volatile boolean running;

    public Server() throws IOException {
        System.out.println("Waiting for client connection...");
        try (ServerSocket ss = new ServerSocket(PORT)) {
            sk = ss.accept();
        }
        System.out.println("Client connected.");

        in = new BufferedReader(new InputStreamReader(sk.getInputStream()));
        out = new PrintWriter(sk.getOutputStream(), true);

        initRSAKeys();
        exchangeKeys();
        readMsg();
        writeMsg();
    }

    private void initRSAKeys() {
        keyPair = ChatCrypto.generateKeyPair(new SecureRandom());
    }

    private void exchangeKeys() throws IOException {
        String[] cKey = in.readLine().split(",");
        peerExponent = new BigInteger(cKey[0]);
        peerModulus = new BigInteger(cKey[1]);

        out.println(keyPair.getPublicExponent() + "," + keyPair.getModulus());
        System.out.println("Received Client Key: (e, n) = (" + peerExponent + ", " + peerModulus + ")");
        System.out.println("Client fingerprint: " + ChatCrypto.fingerprint(peerExponent, peerModulus));
        System.out.println("Your fingerprint:   " + ChatCrypto.fingerprint(keyPair.getPublicExponent(), keyPair.getModulus()));
    }

    private void readMsg() {
        running = true;
        new Thread(() -> {
            System.out.println("Reader ready.");
            while (running) {
                try {
                    String encMsg = in.readLine();
                    if (encMsg == null || encMsg.equalsIgnoreCase("exit")) {
                        System.out.println("Chat closed.");
                        shutdown();
                        break;
                    }
                    String decMsg = decrypt(encMsg);
                    System.out.println("Client: " + decMsg);
                } catch (IOException ex) {
                    if (running) {
                        ex.printStackTrace();
                    }
                    shutdown();
                    break;
                }
            }
        }, "chat-server-reader").start();
    }

    private void writeMsg() {
        new Thread(() -> {
            System.out.println("Writer ready.");
            try (BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {
                while (running) {
                    String msg = userIn.readLine();
                    if (msg == null || EXIT_COMMAND.equalsIgnoreCase(msg.trim())) {
                        out.println(EXIT_COMMAND);
                        shutdown();
                        break;
                    }
                    String encMsg = encrypt(msg);
                    out.println(encMsg);
                    out.flush();
                }
            } catch (IOException ex) {
                if (running) {
                    ex.printStackTrace();
                }
                shutdown();
            }
        }, "chat-server-writer").start();
    }

    private String encrypt(String msg) {
        return ChatCrypto.encrypt(msg, peerExponent, peerModulus);
    }

    private String decrypt(String encMsg) {
        return ChatCrypto.decrypt(encMsg, keyPair.getPrivateExponent(), keyPair.getModulus());
    }

    private void shutdown() {
        running = false;
        try {
            if (sk != null && !sk.isClosed()) {
                sk.close();
            }
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        System.out.println("Server started...");
        try {
            new Server();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
