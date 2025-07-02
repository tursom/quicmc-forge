package cn.tursom.netmix.mixin;

import cn.tursom.netmix.network.ClientProtocol;
import cn.tursom.netmix.network.ProtocolManager;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@OnlyIn(Dist.CLIENT)
@Mixin(ServerAddress.class)
public class ServerAddressMixin {
    @ModifyVariable(
            method = {"parseString", "isValidAddress"},
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private static String removeHeader(String address) {
        ClientProtocol protocol = ProtocolManager.findClientProtocol(address);
        if (protocol == null) {
            return address;
        } else {
            return protocol.getRawAddress(address);
        }
    }
}
