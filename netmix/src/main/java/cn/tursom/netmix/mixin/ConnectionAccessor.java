package cn.tursom.netmix.mixin;

import net.minecraft.network.Connection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@Mixin(Connection.class)
public interface ConnectionAccessor {
    @Accessor("activationHandler")
    void setActivationHandler(Consumer<Connection> activationHandler);
}
