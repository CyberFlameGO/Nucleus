package io.github.nucleuspowered.nucleus.bootstrap;

import io.github.nucleuspowered.nucleus.core.IPropertyHolder;

public class NucleusPropertyHolder implements IPropertyHolder {

    private final boolean debugMode = NucleusPropertyHolder.isPropertyActive("nucleus.debug-mode");
    private final boolean shutdownOnError = System.getProperty("nucleus.shutdownOnError") != null;

    @Override
    public boolean shutdownOnError() {
        return this.shutdownOnError;
    }

    @Override
    public boolean debugMode() {
        return this.debugMode;
    }

    private static boolean isPropertyActive(final String property) {
        return System.getProperty(property, "false").equals("true");
    }
}
