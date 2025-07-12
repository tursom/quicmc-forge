package cn.tursom.netmix.mixin;

import cn.tursom.netmix.network.ClientProtocol;
import cn.tursom.netmix.network.ProtocolManager;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetSocketAddress;

@Mixin(ServerStatusPinger.class)
public class ServerStatusPingerMixin {
    @Redirect(
            method = "pingServer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/Connection;"
            )
    )
    private Connection redirectConnectToServer(InetSocketAddress address, boolean epoll, @Local(argsOnly = true) ServerData serverData) {
        if (serverData == null) {
            return Connection.connectToServer(address, epoll);
        }

        ClientProtocol protocol = ProtocolManager.findClientProtocol(serverData.ip);
        if (protocol == null) {
            return Connection.connectToServer(address, epoll);
        }

        return protocol.connectToServer(serverData, address, epoll);
    }
}