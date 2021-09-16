package com.jieluote.josp.glide;

public class GlideContext {
    private final Registry registry;
    private final Engine engine;

    public Registry getRegistry() {
        return registry;
    }

    public Engine getEngine() {
        return engine;
    }

    public GlideContext(Registry registry, Engine engine) {
        this.registry = registry;
        this.engine = engine;
    }
}
