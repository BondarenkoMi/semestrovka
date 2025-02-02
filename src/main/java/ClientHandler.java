import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedWriter writer;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            System.out.println("Connected to " + socket.getInetAddress());

            String message;
            while ((message = reader.readLine()) != null) {
                if (Server.gameStopped) {
                    sendMessage("Game stopped. Disconnecting...");
                    socket.close();
                    Server.removeClient(this);
                    break;
                }

                System.out.println("Received: " + message);
                Server.broadcast(message);
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + socket.getInetAddress());
        } finally {
            Server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Client disconnected: " + socket.getInetAddress());
        }
    }

    public void sendMessage(String message) {
        try {
            if (writer != null) {
                writer.write(message + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + socket.getInetAddress());
        }
    }
}
