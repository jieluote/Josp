package com.jieluote.josp.glide;

import java.util.List;
import androidx.annotation.NonNull;

public class Registry {
    private final ModelLoaderRegistry modelLoaderRegistry;

    public Registry() {
        modelLoaderRegistry = new ModelLoaderRegistry();
    }

    public void append(IModelLoader loader) {
        modelLoaderRegistry.append(loader);
    }

    @NonNull
    public List<IModelLoader> getModelLoaders(@NonNull Object model) {
        List<IModelLoader> result = modelLoaderRegistry.getModelLoaders(model);
        return result;
    }
}
