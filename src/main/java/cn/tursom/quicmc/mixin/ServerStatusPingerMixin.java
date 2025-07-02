package cn.tursom.quicmc.mixin;

import cn.tursom.quicmc.network.QuicConnector;
import cn.tursom.quicmc.network.ThreadLocals;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

@Mixin(ServerStatusPinger.class)
public class ServerStatusPingerMixin {
    @Redirect(
            method = "pingServer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/Connection;"
            )
    )
    private Connection redirectConnectToServer(InetSocketAddress address, boolean epoll) {
        ServerData serverData = ThreadLocals.CURRENT_SERVER.get();
        if (serverData == null) {
            return Connection.connectToServer(address, epoll);
        }

        if (!serverData.ip.startsWith("quic://")) {
            return Connection.connectToServer(address, epoll);
        }

        return QuicConnector.connectToServer(address);
    }

    @Inject(method = "pingServer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/Connection;",
                    shift = At.Shift.BEFORE)
    )
    private void beforeConnectToServer(ServerData serverData, Runnable runnable, CallbackInfo ci) {
        ThreadLocals.CURRENT_SERVER.set(serverData);
    }

    @Inject(method = "pingServer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/Connection;",
                    shift = At.Shift.AFTER)
    )
    private void afterConnectToServer(ServerData serverData, Runnable runnable, CallbackInfo ci) {
        ThreadLocals.CURRENT_SERVER.remove();
    }
}