package org.waste.of.time;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class LoaderInfo {
    @Contract(pure = true)
    public static @NotNull String getVersion() {
        return "DEV";
    }
}
