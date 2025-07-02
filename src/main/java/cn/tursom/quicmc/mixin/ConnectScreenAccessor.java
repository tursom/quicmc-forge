package cn.tursom.quicmc.mixin;

import io.netty.channel.ChannelFuture;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@OnlyIn(Dist.CLIENT)
@Mixin(ConnectScreen.class)
public interface ConnectScreenAccessor {
    @Accessor("LOGGER")
    static Logger getLogger() {
        throw new AssertionError();
    }

    @Accessor("ABORT_CONNECTION")
    static Component getAbortConnection() {
        throw new AssertionError();
    }

    @Accessor("aborted")
    boolean isAborted();

    @Accessor("parent")
    Screen getParent();

    @Accessor("connectFailedTitle")
    Component getConnectFailedTitle();

    @Accessor("channelFuture")
    void setChannelFuture(ChannelFuture channelFuture);

    @Accessor("connection")
    Connection getConnection();

    @Accessor("connection")
    void setConnection(Connection connection);

    @Invoker("updateStatus")
    void invokeUpdateStatus(Component p_95718_);
}
