import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;
    private BufferedWriter out;
    private BufferedReader in;

    public void connect() {
        try {
            clientSocket = new Socket("localhost", 50000);
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            System.out.println("Connected to server");

            // Запуск потока для чтения сообщений от сервера
            startReadingMessages();

            // Основной цикл для отправки сообщений
            startSendingMessages();
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private void startReadingMessages() {
        Thread readerThread = new Thread(() -> {
            try {
                while (true) {
                    String serverMessage = in.readLine();
                    if (serverMessage == null) {
                        break; // Сервер отключился
                    }
                    System.out.println("\n[Server]: " + serverMessage);
                }
            } catch (IOException e) {
                System.out.println("\nDisconnected from server.");
            } finally {
                closeResources();
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void startSendingMessages() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            if (message.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                break;
            }
            sendMessage(message);
        }
        scanner.close();
    }

    private void sendMessage(String message) {
        try {
            if (out != null && !clientSocket.isClosed() && !clientSocket.isOutputShutdown()) {
                out.write(message + "\n");
                out.flush();
            } else {
                System.out.println("Cannot send message. Connection is closed.");
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.connect();
    }
}
