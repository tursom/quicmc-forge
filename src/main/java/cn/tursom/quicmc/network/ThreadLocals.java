package cn.tursom.quicmc.network;

import lombok.experimental.UtilityClass;
import net.minecraft.client.multiplayer.ServerData;

@UtilityClass
public class ThreadLocals {
    public final ThreadLocal<ServerData> CURRENT_SERVER = new ThreadLocal<>();
}
