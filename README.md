# ChatGuard

ChatGuard is a simple two-terminal Java chat application. One program runs as the server and the other runs as the client. After both sides connect, they exchange public keys and send messages as encrypted text over a socket connection.

## What It Does

- Starts a local chat session between a server and a client
- Exchanges public keys after the connection is established
- Encrypts outgoing messages before sending them across the socket
- Decrypts incoming messages before printing them in the terminal
- Supports a clean exit with `/exit`

## Project Files

- `Server.java` - waits for a client connection and manages the server side of the chat
- `Client.java` - connects to the server and manages the client side of the chat
- `ChatCrypto.java` - shared helper for key generation, encryption, decryption, and key fingerprints

## Build

Compile all Java files from the project directory:

```bash
javac *.java
```

## Run

Open two terminals in the project folder.

In the first terminal, start the server:

```bash
java Server
```

In the second terminal, start the client:

```bash
java Client
```

## How to Use

1. Start the server first.
2. Start the client after the server is waiting for a connection.
3. Type a message and press Enter to send it.
4. Use `/exit` in either terminal to close the chat session.

## Notes

- This project is intended for learning and demonstration.
- The chat flow is designed for a local client-server example, not production messaging.
