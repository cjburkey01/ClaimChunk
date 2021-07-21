package com.cjburkey.claimchunk.config.access;

import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.ccconfig.ICCConfigSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class AccessesWrapper implements ICCConfigSerializable {

    public final ICCConfigSerializable access;
    public final String key;

    public AccessesWrapper(String key, @Nullable EntityAccess entityAccess) {
        access = entityAccess;
        this.key = key;
    }

    public AccessesWrapper(String key, @Nullable BlockAccess blockAccess) {
        access = blockAccess;
        this.key = key;
    }

    public static @Nullable
    <V> AccessesWrapper createWrapper(String key, V access) {
        if (access instanceof EntityAccess) {
            return new AccessesWrapper(key, (EntityAccess) access);
        } else if (access instanceof BlockAccess) {
            return new AccessesWrapper(key, (BlockAccess) access);
        }
        return null;
    }

    public boolean isNull() {
        return access == null;
    }

    @SuppressWarnings("unused")
    public Optional<EntityAccess> getEntityAccess() {
        return Optional.ofNullable(access instanceof EntityAccess ? (EntityAccess) access : null);
    }

    @SuppressWarnings("unused")
    public Optional<BlockAccess> getBlockAccess() {
        return Optional.ofNullable(access instanceof BlockAccess ? (BlockAccess) access : null);
    }

    @Override
    public void fromCCConfig(@Nonnull CCConfig config, @Nonnull String key) {
        if (access != null) access.fromCCConfig(config, key);
    }

    @Override
    public void toCCConfig(@Nonnull CCConfig config, @Nonnull String key) {
        if (access != null) access.toCCConfig(config, key);
    }

}
