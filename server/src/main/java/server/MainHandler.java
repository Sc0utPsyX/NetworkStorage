package server;

import files.FileListRequest;
import files.FileMessage;
import files.FileRequest;
import files.LoginMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    LoginMessage login = new LoginMessage();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                FileRequest fileRequest = (FileRequest) msg;
                if (Files.exists(Paths.get(System.getProperty("user.dir") + File.separator + "server_storage" + File.separator + fileRequest.getFilename()))) {
                    FileMessage message = new FileMessage(Paths.get(System.getProperty("user.dir") + File.separator + "server_storage" + File.separator + fileRequest.getFilename()));
                    ctx.writeAndFlush(message);
                }
            }
            if (msg instanceof FileMessage){
                Files.createFile(Paths.get(System.getProperty("user.dir") + File.separator + "server_storage" + File.separator + ((FileMessage) msg).getFilename()));
                Files.write(Paths.get(System.getProperty("user.dir") + File.separator + "server_storage" + File.separator + ((FileMessage) msg).getFilename()), ((FileMessage) msg).getData());
            }
            if (msg instanceof FileListRequest){
                FileListRequest fileListRequest = new FileListRequest(((FileListRequest) msg).getDirectory());
                ctx.writeAndFlush(fileListRequest);
            }
            if (msg instanceof LoginMessage){
                String s = DatabaseHandler.readUserDatabase(((LoginMessage) msg).login, ((LoginMessage) msg).getPassword());
                if (s != null){
                    login.setDirectory(s);
                    ctx.writeAndFlush(login);
                }
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
