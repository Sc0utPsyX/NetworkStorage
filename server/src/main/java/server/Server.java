package server;

import files.FileListRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {
    public Server() {
    }

    public void run() {
        try (EventLoopGroup masterGroup = new NioEventLoopGroup();
             EventLoopGroup workerGroup = new NioEventLoopGroup())
        {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(masterGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler()
                            );
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = serverBootstrap.bind(8189).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        try {
        new Server().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
