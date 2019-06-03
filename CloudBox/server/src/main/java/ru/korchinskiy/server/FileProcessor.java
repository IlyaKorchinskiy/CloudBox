package ru.korchinskiy.server;

import io.netty.channel.ChannelHandlerContext;
import ru.korchinskiy.common.FileMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileProcessor {
    private File dir;
    private FileMessage fileMessage;

    public FileProcessor(File dir) {
        this.dir = dir;
        fileMessage = new FileMessage();
    }

    public void writeFile(FileMessage msg) {
        try {
            FileOutputStream out = new FileOutputStream(new File(dir, msg.getName()), true);
            out.write(msg.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFiles(ChannelHandlerContext out, String[] files) throws IOException {
        byte[] bytes = new byte[FileMessage.MAX_BYTE_AMOUNT];
        for (String fileName : files) {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(dir.getPath() + "/" + fileName));
            int parts = inputStream.available() / bytes.length;
            System.out.println(inputStream.available());
            for (int i = 0; i < parts + 1; i++) {
                if (inputStream.available() < bytes.length) {
                    byte[] lastBytes = new byte[inputStream.available()];
                    inputStream.read(lastBytes);
                    inputStream.close();
                    fileMessage.setBytes(lastBytes);
                } else {
                    inputStream.read(bytes);
                    fileMessage.setBytes(bytes);
                }
                fileMessage.setName(fileName);
                out.write(fileMessage);
            }
        }
    }

    public void deleteFiles(String[] files) throws IOException {
        for (String file : files) {
            Files.delete(Paths.get(dir.getPath() + "/" + file));
        }
    }
}
