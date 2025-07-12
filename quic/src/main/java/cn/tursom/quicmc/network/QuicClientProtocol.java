package cn.tursom.quicmc.network;

import cn.tursom.netmix.network.ClientProtocol;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class QuicClientProtocol implements ClientProtocol {
    public static final QuicClientProtocol INSTANCE = new QuicClientProtocol();

    @NotNull
    @Override
    public Thread newConnector(@NotNull String name, ConnectScreen connectScreen, Minecraft minecraft, ServerAddress serverAddress, ServerData serverData) {
        return new QuicConnector(name, connectScreen, minecraft, serverAddress, serverData);
    }

    @NotNull
    @Override
    public Connection connectToServer(ServerData serverData, SocketAddress remote, boolean useNativeTransport) {
        return QuicConnector.connectToServer(remote, useNativeTransport);
    }

    @Override
    public boolean isValidProtocol(String address) {
        return address != null && address.startsWith("quic://");
    }

    @NotNull
    @Override
    public String getRawAddress(String address) {
        if (!isValidProtocol(address)) {
            throw new IllegalArgumentException("Invalid QUIC protocol address: " + address);
        }
        return address.substring("quic://".length());
    }
}
