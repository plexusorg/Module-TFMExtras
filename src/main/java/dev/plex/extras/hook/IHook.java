package dev.plex.extras.hook;

import dev.plex.extras.TFMExtras;

/**
 * @author Taah
 * @since 2:16 PM [23-08-2023]
 */
public interface IHook<T> {

    void onEnable(TFMExtras module);

    void onDisable(TFMExtras module);

    T plugin();
}