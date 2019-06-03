package ru.korchinskiy.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAO {
    private static final String URL = "jdbc:mysql://192.168.0.104:3306/cloudbox?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "123123";

    private static Connection connection;

    public static void connect() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkLoginPassword(String login, String pass) throws SQLException {
        String request = "SELECT `id` FROM `user` WHERE `login` = '" + login + "' AND `password` = '" + pass + "'";
        return QueryExecutor.execQuery(connection, request, result -> result.next());
    }

    public static boolean addNewUser(String login, String pass) throws SQLException {
        String request = "INSERT INTO `user` (`login`, `password`) VALUES ('" + login + "', '" + pass + "')";
        int result = QueryExecutor.execUpdate(connection, request);
        return (result != 0);
    }
}
