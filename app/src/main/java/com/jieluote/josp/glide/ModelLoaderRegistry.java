package com.jieluote.josp.glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelLoaderRegistry {
    private final List<IModelLoader> entries = new ArrayList<>();

    public synchronized void append(IModelLoader modelClass) {
        entries.add(modelClass);
    }

    public List<IModelLoader> getModelLoaders(Object model) {
        List<IModelLoader> filteredLoaders = new ArrayList<>();
        if (entries == null) {
            return filteredLoaders;
        }
        for (int i = 0; i < entries.size(); i++) {
            IModelLoader IModelLoader = entries.get(i);
            if (IModelLoader.handles(model)) {
                filteredLoaders.add(IModelLoader);
            }
        }
        return filteredLoaders;
    }
}
