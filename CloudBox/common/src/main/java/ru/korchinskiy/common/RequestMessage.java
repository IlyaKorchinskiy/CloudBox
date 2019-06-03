package ru.korchinskiy.common;

public class RequestMessage extends Message {
    public static final String REQUEST_FILE = "file";
    public static final String REQUEST_UPDATE = "update";
    public static final String REQUEST_DELETE = "delete";
    public static final String REQUEST_RENAME = "rename";

    private String type;
    private String[] files;

    public String getType() {
        return type;
    }

    public String[] getFiles() {
        return files;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }
}
