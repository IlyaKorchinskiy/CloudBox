package ru.korchinskiy.server;

import io.netty.channel.ChannelHandlerContext;
import ru.korchinskiy.common.AuthMessage;
import ru.korchinskiy.server.database.DAO;

import java.io.File;
import java.sql.SQLException;

public class AuthProcessor {

    public File makeAuth(ChannelHandlerContext ctx, AuthMessage msg) {
        try {
            if (msg.getType().equals(AuthMessage.SIGN_IN_TYPE)) {
                return logIn(ctx, msg);
            }
            if (msg.getType().equals(AuthMessage.REGISTER_TYPE)) {
                return register(ctx, msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File register(ChannelHandlerContext ctx, AuthMessage msg) throws SQLException {
        String user = msg.getLogin();
        File dir = null;
        if (DAO.addNewUser(user, msg.getPass())) {
            ctx.write(msg);
            dir = new File("server/repo/" + user);
            dir.mkdir();
        } else {
            msg.setLogin("");
            ctx.write(msg);
        }
        return dir;
    }

    private File logIn(ChannelHandlerContext ctx, AuthMessage msg) throws SQLException {
        String user = msg.getLogin();
        File dir = null;
        if (DAO.checkLoginPassword(user, msg.getPass())) {
            System.out.println(user + " logged in");
            ctx.write(msg);
            dir = new File("server/repo/" + user);
        } else {
            msg.setLogin("");
            ctx.write(msg);
        }
        return dir;
    }
}
