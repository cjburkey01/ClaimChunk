package com.cjburkey.claimchunk.config.access;

import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.ccconfig.ICCConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class AccessWrapper implements ICCConfigSerializable {

    public final ICCConfigSerializable access;
    public final String key;

    public AccessWrapper(String key, @Nullable EntityAccess entityAccess) {
        access = entityAccess;
        this.key = key;
    }

    public AccessWrapper(String key, @Nullable BlockAccess blockAccess) {
        access = blockAccess;
        this.key = key;
    }

    public AccessWrapper(String key) {
        access = null;
        this.key = key;
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
    public void fromCCConfig(@NotNull CCConfig config, @NotNull String key) {
        if (access != null) access.fromCCConfig(config, key);
    }

    @Override
    public void toCCConfig(@NotNull CCConfig config, @NotNull String key) {
        if (access != null) access.toCCConfig(config, key);
    }

}
