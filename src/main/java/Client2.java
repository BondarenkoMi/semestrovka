import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client2 {
    public static void main(String[] args) {
        try (Socket clientSocket = new Socket("127.0.0.1", 50000);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to server");

            // Поток для чтения сообщений от сервера
            Thread readerThread = new Thread(() -> {
                try {
                    while (true) {
                        String serverMessage = in.readLine();
                        if (serverMessage == null) break; // Сервер отключился

                        System.out.println("\n[Server]: " + serverMessage);
//                        System.out.print("Enter message: "); // Повторяем приглашение для ввода
                    }
                } catch (IOException e) {
                    System.out.println("\nDisconnected from server.");
                }
            });
            readerThread.setDaemon(true); // Завершить поток, если основной поток завершится
            readerThread.start();

            // Отправка сообщений серверу
            while (true) {
                if (Server.gameStopped){
                    clientSocket.close();
                    break;
                }
                System.out.print("Enter message: ");
                String message = scanner.nextLine();

                out.write(message + "\n");
                out.flush();

                if ("exit".equalsIgnoreCase(message)) {
                    System.out.println("Exiting...");
                    clientSocket.close();
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("\nDisconnected from server.");
        }
    }
}
