package server;

import files.FileListRequest;
import files.FileMessage;
import files.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    FileListRequest flr = new FileListRequest();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                FileRequest fileRequest = (FileRequest) msg;
                if (Files.exists(Paths.get("cloud_storage/" + fileRequest.getFilename()))) {
                    FileMessage message = new FileMessage(Paths.get("cloud_storage/" + fileRequest.getFilename()));
                    ctx.writeAndFlush(message);
                }
            }
            if (msg instanceof FileMessage){
                Files.createFile(Paths.get("cloud_storage/" + ((FileMessage) msg).getFilename()));
                Files.write(Paths.get("cloud_storage/" + ((FileMessage) msg).getFilename()), ((FileMessage) msg).getData());
            }
            if (msg instanceof FileListRequest){
                ctx.writeAndFlush(flr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
