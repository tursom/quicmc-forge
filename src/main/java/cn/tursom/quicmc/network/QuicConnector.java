package cn.tursom.quicmc.network;

import cn.tursom.quicmc.mixin.ConnectionAccessor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.incubator.codec.quic.*;
import io.netty.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.LazyLoadedValue;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class QuicConnector extends AbstractConnector {
    private static final QuicSslContext SSL_CONTEXT = QuicSslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .applicationProtocols("minecraft", "raw", "quic") // 多个协议选项
            .build();

    public QuicConnector(@NotNull String name, ConnectScreen connectScreen, Minecraft minecraft, ServerAddress serverAddress, ServerData serverData) {
        super(name, connectScreen, minecraft, serverAddress, serverData);
    }

    /**
     * 连接到远程服务器
     *
     * @see Connection#connectToServer(InetSocketAddress, boolean)
     */
    public static Connection connectToServer(SocketAddress remote, boolean useNativeTransport) {
        var connection = new Connection(PacketFlow.CLIENTBOUND);
        connect(remote, connection, useNativeTransport).syncUninterruptibly();
        return connection;
    }

    /**
     * 连接到远程服务器
     *
     * @see Connection#connect(InetSocketAddress, boolean, Connection)
     */
    @SneakyThrows
    public static Future<QuicStreamChannel> connect(SocketAddress remote, Connection connection, boolean useNativeTransport) {
        Channel channel = datagramBootstrap(useNativeTransport)
                .handler(new QuicClientCodecBuilder()
                        .sslContext(SSL_CONTEXT)
                        .initialMaxData(33554432L)
                        .initialMaxStreamDataBidirectionalLocal(16777216L)
                        .initialMaxStreamDataBidirectionalRemote(16777216L)
                        .initialMaxStreamDataUnidirectional(16777216L)
                        .initialMaxStreamsBidirectional(100L)
                        .initialMaxStreamsUnidirectional(100L)
                        .activeMigration(true)
                        .build())
                .bind(0).sync().channel();

        // 连接到服务器
        QuicChannel quicChannel = QuicChannel.newBootstrap(channel)
                .handler(new ChannelInitializer<QuicChannel>() {
                    @Override
                    protected void initChannel(QuicChannel ch) {
                        // QUIC 连接处理器
                        ch.pipeline().addLast(new QuicConnectionHandler());
                    }
                })
                .streamHandler(new ClientChannelInitializer(connection))
                .remoteAddress(remote)
                .connect()
                .get();

        // 创建流并发送数据
        return quicChannel.createStream(QuicStreamType.BIDIRECTIONAL, new ClientChannelInitializer(connection));
    }

    @SneakyThrows
    @Override
    public Future<QuicStreamChannel> doConnect(SocketAddress remote, Connection connection, boolean useNativeTransport) {
        return connect(remote, connection, useNativeTransport);
    }

    private static class QuicStreamInitializer extends ClientChannelInitializer {
        public QuicStreamInitializer(Connection connection) {
            super(connection);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ((QuicStreamChannel) ctx.channel()).parent().close();

            super.channelInactive(ctx);
        }
    }

    private static class QuicConnectionHandler extends ChannelInboundHandlerAdapter {
    }
}
