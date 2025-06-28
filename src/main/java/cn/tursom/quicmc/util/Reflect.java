package cn.tursom.quicmc.util;

import com.esotericsoftware.reflectasm.FieldAccess;
import lombok.experimental.UtilityClass;
import org.spongepowered.asm.mixin.injection.points.BeforeFieldAccess;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@UtilityClass
public class Reflect {
    private final ConcurrentMap<Class, FieldAccess> fieldAccessCache = new ConcurrentHashMap<>();

    public FieldAccess getFieldAccess(Class<?> clazz) {
        return fieldAccessCache.computeIfAbsent(clazz, FieldAccess::get);
    }
}
