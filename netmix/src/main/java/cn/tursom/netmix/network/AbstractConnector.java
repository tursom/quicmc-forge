package cn.tursom.netmix.network;

import cn.tursom.netmix.mixin.ConnectScreenAccessor;
import cn.tursom.netmix.mixin.ConnectionAccessor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.util.LazyLoadedValue;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

public abstract class AbstractConnector extends Thread {
    protected final ConnectScreen connectScreen;
    protected final Minecraft minecraft;
    protected final ServerAddress serverAddress;
    protected final ServerData serverData;

    public AbstractConnector(@NotNull String name,
                             ConnectScreen connectScreen,
                             Minecraft minecraft,
                             ServerAddress serverAddress,
                             ServerData serverData) {
        super(name);
        this.connectScreen = connectScreen;
        this.minecraft = minecraft;
        this.serverAddress = serverAddress;
        this.serverData = serverData;
    }


    @Override
    public final void run() {
        ConnectScreenAccessor accessor = (ConnectScreenAccessor) connectScreen;

        InetSocketAddress inetsocketaddress = null;

        try {
            if (accessor.isAborted()) {
                return;
            }

            Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
            if (accessor.isAborted()) {
                return;
            }

            if (optional.isEmpty()) {
                ConnectScreenAccessor.getLogger().error("Couldn't connect to server: Unknown host \"{}\"", serverAddress.getHost());
                net.minecraftforge.network.DualStackUtils.logInitialPreferences();
                minecraft.execute(() -> {
                    minecraft.setScreen(new DisconnectedScreen(accessor.getParent(), accessor.getConnectFailedTitle(), ConnectScreen.UNKNOWN_HOST_MESSAGE));
                });
                return;
            }

            inetsocketaddress = optional.get();
            Connection connection;
            Future<? extends Channel> channelFuture;
            synchronized (connectScreen) {
                if (accessor.isAborted()) {
                    return;
                }

                // same as Connection.connect
                connection = new Connection(PacketFlow.CLIENTBOUND);
                ((ConnectionAccessor) connection).setActivationHandler(NetworkHooks::registerClientLoginChannel);
                channelFuture = doConnect(inetsocketaddress, connection, minecraft.options.useNativeTransport());
            }

            Channel channel = channelFuture.syncUninterruptibly().get();
            accessor.setChannelFuture(channel.newSucceededFuture());

            synchronized (connectScreen) {
                if (accessor.isAborted()) {
                    connection.disconnect(ConnectScreenAccessor.getAbortConnection());
                    return;
                }

                accessor.setConnection(connection);
            }

            accessor.getConnection().setListener(new ClientHandshakePacketListenerImpl(
                    accessor.getConnection(),
                    minecraft,
                    serverData,
                    accessor.getParent(),
                    false,
                    null,
                    accessor::invokeUpdateStatus));
            //ConnectScreen.this::updateStatus));
            accessor.getConnection().send(new ClientIntentionPacket(inetsocketaddress.getHostName(), inetsocketaddress.getPort(), ConnectionProtocol.LOGIN));
            accessor.getConnection().send(new ServerboundHelloPacket(minecraft.getUser().getName(), Optional.ofNullable(minecraft.getUser().getProfileId())));
        } catch (Exception exception2) {
            if (accessor.isAborted()) {
                return;
            }

            Throwable throwable = exception2.getCause();
            Exception exception;
            if (throwable instanceof Exception exception1) {
                exception = exception1;
            } else {
                exception = exception2;
            }

            ConnectScreenAccessor.getLogger().error("Couldn't connect to server", exception2);
            String s = inetsocketaddress == null ? exception.getMessage() : exception.getMessage().replaceAll(inetsocketaddress.getHostName() + ":" + inetsocketaddress.getPort(), "").replaceAll(inetsocketaddress.toString(), "");
            minecraft.execute(() -> {
                minecraft.setScreen(new DisconnectedScreen(accessor.getParent(), accessor.getConnectFailedTitle(), Component.translatable("disconnect.genericReason", s)));
            });
        }
    }

    public abstract Future<? extends Channel> doConnect(SocketAddress remote, Connection connection, boolean useNativeTransport);

    /**
     * 快速创建 TCP Bootstrap
     */
    protected static Bootstrap socketBootstrap(boolean useNativeTransport) {
        Class<? extends SocketChannel> oclass;
        @SuppressWarnings("deprecation")
        LazyLoadedValue<? extends EventLoopGroup> lazyloadedvalue;
        if (Epoll.isAvailable() && useNativeTransport) {
            oclass = EpollSocketChannel.class;
            lazyloadedvalue = Connection.NETWORK_EPOLL_WORKER_GROUP;
        } else {
            oclass = NioSocketChannel.class;
            lazyloadedvalue = Connection.NETWORK_WORKER_GROUP;
        }

        return new Bootstrap().group(lazyloadedvalue.get())
                .channel(oclass);
    }

    /**
     * 快速创建 UDP Bootstrap
     */
    protected static Bootstrap datagramBootstrap(boolean useNativeTransport) {
        Class<? extends DatagramChannel> oclass;
        @SuppressWarnings("deprecation")
        LazyLoadedValue<? extends EventLoopGroup> lazyloadedvalue;
        if (Epoll.isAvailable() && useNativeTransport) {
            oclass = EpollDatagramChannel.class;
            lazyloadedvalue = Connection.NETWORK_EPOLL_WORKER_GROUP;
        } else {
            oclass = NioDatagramChannel.class;
            lazyloadedvalue = Connection.NETWORK_WORKER_GROUP;
        }

        return new Bootstrap().group(lazyloadedvalue.get())
                .channel(oclass);
    }

    protected static void initConnectionChannel(Connection connection, Channel ch) {
        ChannelPipeline channelpipeline = ch.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
        Connection.configureSerialization(channelpipeline, PacketFlow.CLIENTBOUND);
        channelpipeline.addLast("packet_handler", connection);
    }

    @Slf4j
    @RequiredArgsConstructor
    protected static class ClientChannelInitializer extends ChannelInitializer<Channel> {
        private final Connection connection;

        @Override
        protected void initChannel(Channel ch) {
            initConnectionChannel(connection, ch);
        }
    }
}
