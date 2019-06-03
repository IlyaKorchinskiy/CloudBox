package ru.korchinskiy.server.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultHandler<T> {
    T handle(ResultSet result) throws SQLException;
}
