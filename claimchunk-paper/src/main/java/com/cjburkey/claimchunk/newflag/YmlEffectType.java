package com.cjburkey.claimchunk.newflag;

/**
 * @since 1.0.0
 */
public enum YmlEffectType {
    // BLOCKS only
    BREAK,
    PLACE,
    SPREAD_INTO,
    SPREAD_OUT_OF,
    SPREAD_WITHIN,
    GROW,
    // BLOCKS and ENTITIES
    INTERACT,
    EXPLODE,
    // ENTITIES only
    DAMAGE,
    // PLAYERS only
    ATTACK_OTHERS,
    ATTACK_OWNER,
    PEARL_INTO,
    PEARL_OUT_OF,
    PEARL_WITHIN
}
