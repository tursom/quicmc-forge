package cn.tursom.netmix.mixin;

import cn.tursom.netmix.network.ClientProtocol;
import cn.tursom.netmix.network.ProtocolManager;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Final
    @Shadow
    private static AtomicInteger UNIQUE_THREAD_ID;

    @Final
    @Shadow
    static Logger LOGGER;

    @Inject(at = @At("HEAD"),
            method = "connect(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;)V",
            cancellable = true)
    private void connect(Minecraft minecraft, ServerAddress serverAddress, ServerData serverData, CallbackInfo ci) {
        ClientProtocol protocol = ProtocolManager.findClientProtocol(serverData.ip);
        if (protocol == null) {
            return;
        }

        Thread thread = protocol.newConnector(
                "Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet(),
                (ConnectScreen) (Object) this,
                minecraft, serverAddress, serverData);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
        ci.cancel();
    }
}
