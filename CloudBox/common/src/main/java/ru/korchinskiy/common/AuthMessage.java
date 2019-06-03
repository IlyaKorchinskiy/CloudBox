package ru.korchinskiy.common;

public class AuthMessage extends Message {
    public static final String SIGN_IN_TYPE = "signIn";
    public static final String REGISTER_TYPE = "register";

    private String type;
    private String login;
    private String pass;

    public AuthMessage(String type, String login, String pass) {
        this.type = type;
        this.login = login;
        this.pass = pass;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "AuthMessage{" +
                "login='" + login + '\'' +
                ", pass='" + pass + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
