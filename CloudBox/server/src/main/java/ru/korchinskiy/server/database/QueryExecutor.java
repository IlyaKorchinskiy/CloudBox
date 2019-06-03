package ru.korchinskiy.server.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryExecutor {

    public static int execUpdate(Connection connection, String update) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(update);
        int updated = statement.getUpdateCount();
        statement.close();
        return updated;
    }

    public static <T> T execQuery(Connection connection, String query, ResultHandler<T> handler) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(query);
        ResultSet resultSet = statement.getResultSet();
        T value = handler.handle(resultSet);
        resultSet.close();
        statement.close();
        return value;
    }
}
