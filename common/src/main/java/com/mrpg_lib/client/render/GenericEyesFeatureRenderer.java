package com.mrpg_lib.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class GenericEyesFeatureRenderer<T extends MobEntity, M extends EntityModel<T>> extends EyesFeatureRenderer<T, M> {
    private final RenderLayer eyesLayer;

    public GenericEyesFeatureRenderer(FeatureRendererContext<T, M> context, Identifier texture) {
        super(context);
        this.eyesLayer = RenderLayer.getEyes(texture);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return this.eyesLayer;
    }
}
