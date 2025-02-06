import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lobby implements Runnable {
    private final int id;
    private final List<ClientHandler> clients;
    private volatile boolean started;

    public Lobby(int id) {
        this.id = id;
        this.clients = new ArrayList<>();
        this.started = false;
    }

    public int getId() {
        return id;
    }

    public void addClient(ClientHandler client) {
        if (clients.size() < 2) {
            clients.add(client);
            client.sendMessage("Waiting for another player...");
//            if (clients.size() == 2) {
//                startLobby();
//            }
        } else {
            throw new RuntimeException("Lobby is full.");
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public boolean isEmpty() {
        return clients.isEmpty();
    }

    @Override
    public void run() {
        if (started) {
            sendMessage("Game started!");
            int curClientIndex = 0;
            String message;
            while (clients.size() > 0) {
                sendMessage("Очередь игрока " + (curClientIndex + 1));
                sendMessageToCurrentPlayer(curClientIndex, "Ваш ход");
                try {
                    message = clients.get(curClientIndex).getMessage();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (message == null || message.trim().isEmpty() || message.equalsIgnoreCase("exit")) {
                    // Обработка выхода игрока
                    clients.get(curClientIndex).sendMessage("Вы покинули игру.");
                    removeClient(clients.get(curClientIndex));  // Убираем игрока из лобби
                    if (clients.size() == 0) {
                        sendMessage("Игра завершена. Все игроки покинули лобби.");
                        break;
                    } else {
                        sendMessage("Игрок " + (curClientIndex + 1) + " покинул игру. Осталось " + clients.size() + " игроков.");
                    }
                } else {
                    sendMessage("Игрок " + (curClientIndex + 1) + ": " + message);
                    curClientIndex = (curClientIndex + 1) % clients.size();  // Переводим очередь на следующего игрока
                }
            }
        }
    }

    public void startLobby() {
        if (!started) {
            started = true;
            new Thread(this).start();
        }
    }

    public void sendMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void sendMessageToCurrentPlayer(int playerIndex, String message) {
        clients.get(playerIndex).sendMessage(message);
    }
    public List<ClientHandler> getClients() {
        return clients;
    }
}
