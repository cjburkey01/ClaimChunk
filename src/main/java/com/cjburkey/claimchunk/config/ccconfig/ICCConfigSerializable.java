package com.cjburkey.claimchunk.config.ccconfig;

import javax.annotation.Nonnull;

public interface ICCConfigSerializable {

    void fromCCConfig(@Nonnull CCConfig config, @Nonnull String key);

    void toCCConfig(@Nonnull CCConfig config, @Nonnull String key);

}
