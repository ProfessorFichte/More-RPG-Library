package com.mrpg_lib.client.render;

import com.mrpg_lib.entity.CustomCloudEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class CustomCloudRenderer extends EntityRenderer<CustomCloudEntity> {
    public CustomCloudRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(CustomCloudEntity entity) {
        return null;
    }
}
