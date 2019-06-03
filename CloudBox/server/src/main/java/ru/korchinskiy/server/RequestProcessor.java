package ru.korchinskiy.server;

import io.netty.channel.ChannelHandlerContext;
import ru.korchinskiy.common.RequestMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RequestProcessor {
    private File dir;
    private RequestMessage request;
    private FileProcessor fileProcessor;

    public RequestProcessor(File dir, FileProcessor fileProcessor) {
        this.fileProcessor = fileProcessor;
        this.request = new RequestMessage();
        this.dir = dir;
    }

    public void processRequest(ChannelHandlerContext ctx, RequestMessage msg) {
        try {
            if (msg.getType().equals(RequestMessage.REQUEST_UPDATE)) {
                sendUpdateList(ctx);
            }
            if (msg.getType().equals(RequestMessage.REQUEST_FILE)) {
                fileProcessor.sendFiles(ctx, msg.getFiles());
            }
            if (msg.getType().equals(RequestMessage.REQUEST_DELETE)) {
                fileProcessor.deleteFiles(msg.getFiles());
                sendUpdateList(ctx);
            }
            if (msg.getType().equals(RequestMessage.REQUEST_RENAME)) {
                renameFile(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void renameFile(RequestMessage msg) throws IOException {
        Path source = Paths.get(dir + "/" + msg.getFiles()[0]);
        Files.move(source, source.resolveSibling(msg.getFiles()[1]));
    }

    public void sendUpdateList(ChannelHandlerContext ctx) {
        request.setType(RequestMessage.REQUEST_UPDATE);
        request.setFiles(dir.list());
        ctx.write(request);
    }
}
