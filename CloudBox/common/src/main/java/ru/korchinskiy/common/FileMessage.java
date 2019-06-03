package ru.korchinskiy.common;

public class FileMessage extends Message {
    public static final int MAX_BYTE_AMOUNT = 1024 * 1024 * 10;

    private String name;
    private byte[] bytes;

    public FileMessage(String name, byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
    }

    public FileMessage() {

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getName() {
        return name;
    }

    public byte[] getBytes() {
        return bytes;
    }


}
