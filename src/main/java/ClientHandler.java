import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Lobby clientLobby;
    private String message;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            System.out.println("Connected to " + socket.getInetAddress());
            sendMessage("Input lobby number\n");

            String input = reader.readLine(); // Читаем единственную строку
            while (input == null || input.trim().isEmpty()) {
                input = reader.readLine();
            }
            int lobbyNumber = Integer.parseInt(input);

            CopyOnWriteArrayList<Lobby> lobbies = Server.getLobbies();
            boolean inLobby = false;

            for (Lobby lobby : lobbies) {
                if (lobby.getId() == lobbyNumber) {
                    lobby.addClient(this);
                    sendMessage("You have joined the lobby");
                    clientLobby = lobby;
                    inLobby = true;
                    break;
                }
            }

            if (!inLobby) {
                Lobby newLobby = new Lobby(lobbyNumber);
                lobbies.add(newLobby);
                newLobby.addClient(this);
                sendMessage("You have joined the lobby");
                clientLobby = newLobby;
            }
            if (clientLobby.getClients().size() == 2) {
                clientLobby.startLobby();
            }

//            while (true) {
//                message = reader.readLine(); // Читаем строку напрямую
//                if (message == null || message.trim().isEmpty()) {
//                    continue;
//                }
//                if (message.equalsIgnoreCase("start")) {
//                    clientLobby.startLobby();
//                } else {
//                    clientLobby.sendMessage("Player: " + message); // Отправка сообщения всем игрокам
//                }
//            }
        } catch (IOException e) {
            System.err.println("Connection error: " + socket.getInetAddress() + " - " + e.getMessage());
        } finally {
//            closeResources();
        }
    }

    public void sendMessage(String message) {
        try {
            if (writer != null && !socket.isClosed() && !socket.isOutputShutdown()) {
                writer.write(message + "\r\n");
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + socket.getInetAddress() + " - " + e.getMessage());
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            Server.removeClient(this);
            if (clientLobby != null) {
                clientLobby.removeClient(this);
                if (clientLobby.isEmpty()) {
                    Server.getLobbies().remove(clientLobby);
                }
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public String getMessage() throws IOException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 5000) {  // Тайм-аут 5 секунд
            String message = reader.readLine();
            if (message != null && !message.trim().isEmpty()) {
                return message;
            }
        }
        return null;  // Если тайм-аут истек
    }
}
