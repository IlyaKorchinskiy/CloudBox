package ru.korchinskiy.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.korchinskiy.common.AuthMessage;
import ru.korchinskiy.common.FileMessage;
import ru.korchinskiy.common.RequestMessage;

import java.io.File;

public class CloudServerHandler extends ChannelInboundHandlerAdapter {
    private File dir;
    private AuthProcessor authProcessor;
    private RequestProcessor requestProcessor;
    private FileProcessor fileProcessor;

    public CloudServerHandler() {
        this.authProcessor = new AuthProcessor();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null)
                return;
            if (msg instanceof AuthMessage) {
                dir = authProcessor.makeAuth(ctx, (AuthMessage) msg);
                fileProcessor = new FileProcessor(dir);
                requestProcessor = new RequestProcessor(dir, fileProcessor);
                return;
            }
            if (msg instanceof RequestMessage) {
                requestProcessor.processRequest(ctx, (RequestMessage) msg);
                return;
            }
            if (msg instanceof FileMessage) {
                fileProcessor.writeFile((FileMessage) msg);
                requestProcessor.sendUpdateList(ctx);
                return;
            }
            System.out.println("Server received wrong object!");
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
