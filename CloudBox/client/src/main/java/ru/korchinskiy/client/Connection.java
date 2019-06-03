package ru.korchinskiy.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import ru.korchinskiy.common.Message;

import java.io.IOException;
import java.net.Socket;

public class Connection {
    private static Connection connection = new Connection();

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8189;
    private static final int MAX_OBJ_SIZE = 1024 * 1024 * 30;

    private Socket socket;
    private ObjectDecoderInputStream input;
    private ObjectEncoderOutputStream output;

    private Connection() {
    }

    public static Connection getConnection() {
        return connection;
    }

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            input = new ObjectDecoderInputStream(socket.getInputStream(), MAX_OBJ_SIZE);
            output = new ObjectEncoderOutputStream(socket.getOutputStream(), MAX_OBJ_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            socket.close();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message readMessage() throws IOException, ClassNotFoundException {
        return (Message) input.readObject();
    }

    public void sendMessage(Message message) {
        try {
            output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
