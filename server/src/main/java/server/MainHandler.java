package server;

import files.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    LoginMessage login = new LoginMessage();
    StringBuilder serverDir = new StringBuilder();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                FileRequest fileRequest = (FileRequest) msg;
                if (Files.exists(Paths.get(serverDir.toString() + fileRequest.getFilename()))) {
                    FileMessage message = new FileMessage(Paths.get(serverDir.toString() + fileRequest.getFilename()));
                    ctx.writeAndFlush(message);
                }
            }
            if (msg instanceof FileDeleteRequest){
                FileDeleteResponse delete = new FileDeleteResponse(Files.deleteIfExists(Path.of(serverDir + ((FileDeleteRequest) msg).getFilename())));
                ctx.writeAndFlush(delete);

            }
            if (msg instanceof FileMessage){
                Files.createFile(Paths.get(serverDir.toString() + ((FileMessage) msg).getFilename()));
                Files.write(Paths.get(serverDir.toString() + ((FileMessage) msg).getFilename()), ((FileMessage) msg).getData());
            }
            if (msg instanceof FileListRequest){
                if (Files.isDirectory(Path.of(((FileListRequest) msg).getDirectory()))){
                serverDir.delete(0, serverDir.capacity() - 1);
                serverDir.append(((FileListRequest) msg).getDirectory());
                }
                    FileListResponse fileListResponse = new FileListResponse(serverDir.toString(), login.getDirectory());
                    ctx.writeAndFlush(fileListResponse);
            }
            if (msg instanceof LoginMessage){
                String s = DatabaseHandler.readUserDatabase(((LoginMessage) msg).login, ((LoginMessage) msg).getPassword());
                if (s != null){
                    serverDir.append(System.getProperty("user.dir"))
                            .append(File.separator)
                            .append("server_storage")
                            .append(File.separator)
                            .append(((LoginMessage) msg).login)
                            .append(File.separator);
                    login.setDirectory(serverDir.toString());
                    ctx.writeAndFlush(login);
                }
                if (s == null) {
                    login.setDirectory(null);
                    ctx.writeAndFlush(login);
                }
            }
            if (msg instanceof RegistrationMessage){
                RegistrationMessage regMsg;
                if (DatabaseHandler.registerUser((RegistrationMessage) msg)){
                    regMsg = new RegistrationMessage(true);
                    Files.createDirectory(Path.of(serverDir.append(regMsg.getName()).toString()));
                } else {
                    regMsg = new RegistrationMessage(false);
                }
                ctx.writeAndFlush(regMsg);
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
