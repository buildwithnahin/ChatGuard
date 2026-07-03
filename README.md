# ChatGuard

ChatGuard is a small Java socket chat application that demonstrates encrypted messaging between two peers. The project now uses a shared crypto helper to keep the codebase organized and easier to maintain.

## Project Structure

- `Client.java` - interactive chat client
- `Server.java` - socket chat server
- `ChatCrypto.java` - shared RSA key generation and message encryption helpers
- `SHA256.java` - standalone SHA-256 implementation

## Build

```bash
javac *.java
```

## Run

Start the server in one terminal:

```bash
java Server
```

Start the client in another terminal:

```bash
java Client
```

## Usage

- Type messages normally to send encrypted chat content.
- Type `/exit` in either terminal to close the session cleanly.
- Each side prints a SHA-256 public-key fingerprint during setup so the handshake can be checked manually.

## Notes

- The chat flow uses RSA-style encryption with per-message key exchange between the client and server.
- `SHA256.java` is available as a utility class, but it is not required by the chat flow.
- This is a learning project, not a production-secure messaging system.
