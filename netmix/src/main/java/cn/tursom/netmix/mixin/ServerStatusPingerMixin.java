package cn.tursom.netmix.mixin;

import cn.tursom.netmix.network.ClientProtocol;
import cn.tursom.netmix.network.ProtocolManager;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

@Mixin(ServerStatusPinger.class)
public class ServerStatusPingerMixin {
    @Unique
    private final ThreadLocal<ServerData> CURRENT_SERVER = new ThreadLocal<>();

    @Redirect(
            method = "pingServer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/Connection;"
            )
    )
    private Connection redirectConnectToServer(InetSocketAddress address, boolean epoll) {
        ServerData serverData = CURRENT_SERVER.get();
        if (serverData == null) {
            return Connection.connectToServer(address, epoll);
        }

        ClientProtocol protocol = ProtocolManager.findClientProtocol(serverData.ip);
        if (protocol == null) {
            return Connection.connectToServer(address, epoll);
        }

        return protocol.connectToServer(serverData, address, epoll);
    }

    @Inject(method = "pingServer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/Connection;",
                    shift = At.Shift.BEFORE)
    )
    private void beforeConnectToServer(ServerData serverData, Runnable runnable, CallbackInfo ci) {
        CURRENT_SERVER.set(serverData);
    }

    @Inject(method = "pingServer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/Connection;",
                    shift = At.Shift.AFTER)
    )
    private void afterConnectToServer(ServerData serverData, Runnable runnable, CallbackInfo ci) {
        CURRENT_SERVER.remove();
    }
}