package me.noobilybridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import me.noobilybridge.StaminaDisplay;
import me.noobilybridge.config.NumberPosition;
import me.noobilybridge.config.StaminaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;

import static me.noobilybridge.StaminaDisplay.*;

@Mixin(EntityRenderer.class)
public abstract class EntityRenderMixin<T extends Entity> {
    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getTextRenderer()Lnet/minecraft/client/font/TextRenderer;"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    void ough(T entity, Text name, MatrixStack ms, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci, double d, boolean bl, float f, int i, Matrix4f matrix4f, float g, int j) {
        if (!(entity instanceof AbstractClientPlayerEntity)) {
            return;
        }
        //TODO: make this animate the bar out
        if (entity.getVehicle() != null) {
            return;
        }
        if (!entity.equals(MinecraftClient.getInstance().player)) {
            updateStaminaForEntity(entity);
        }
        if (entity.isSpectator()) {
            return;
        }
        if (getAnimationProgress(entity) < 0.01) {
            return;
        }
        if (((AbstractClientPlayerEntity) entity).getOffHandStack().isOf(Items.LEATHER_BOOTS)) {
            return;
        }
        updateTeam(entity);
        String text = String.valueOf(Math.round(getStamina(entity)));
        if (StaminaConfig.INSTANCE.getConfig().numberPosition == NumberPosition.BEFORE_NAME) {
            text = StaminaConfig.INSTANCE.getConfig().outerText + text + StaminaConfig.INSTANCE.getConfig().innerText;
        } else if (StaminaConfig.INSTANCE.getConfig().numberPosition == NumberPosition.AFTER_NAME) {
            text = StaminaConfig.INSTANCE.getConfig().innerText + text + StaminaConfig.INSTANCE.getConfig().outerText;
        }
        float h = -(textRenderer.getWidth(name) + (textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale)) / 2;
        if (StaminaConfig.INSTANCE.getConfig().numberPosition == NumberPosition.BEFORE_NAME) {
            h = -(textRenderer.getWidth(name) - (textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale)) / 2;
        }
        float yeah = getAnimationProgress(entity);
        float animatedWidth = MathHelper.lerp(yeah, -textRenderer.getWidth(name) / 2, h);
        int yeahhh = getColor(getLerpedColor(Color.WHITE, getTeamColor((AbstractClientPlayerEntity) entity), yeah));
        int bgCol = getColor(getLerpedColor(new Color(0, 0, 0, 0.125F), getTeamColor((AbstractClientPlayerEntity) entity), yeah));
        StaminaDisplay.fillFloat(ms, (animatedWidth) - 1, 9, -animatedWidth + 2, -1, convertToShadow(bgCol, 1, g));
        ms.translate(0, 0, -0.1F);
        textRenderer.draw(
                name, animatedWidth + yeah, (float) i + yeah, convertToShadow(yeahhh, 0.25F, 0.125F), false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, light
        );
        ms.translate(0, 0, -0.1F);
        textRenderer.draw(
                name, animatedWidth, (float) i, convertToShadow(yeahhh, 1, 0.125F), false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, light
        );
        if (bl) {
            ms.push();
            textRenderer.draw(name, animatedWidth + yeah, (float) i + yeah, convertToShadow(yeahhh, 0.25F, 1), false, ms.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x00000080, light);
            ms.translate(0, 0, -0.1F);
            textRenderer.draw(name, animatedWidth, (float) i, yeahhh, false, ms.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            ms.pop();
        }
        setupRender(ms);
        ms.scale(1, 1, -1);
        ms.translate(0, 0, 0.01F);
        if (StaminaConfig.INSTANCE.getConfig().drawNametagTeam) {
            //TODO: this currently scales the outline both inside the nametag and out, but i like how it looks
            teams.forEach((uuid, team) -> {
                drawBorder(ms, (animatedWidth - 1), -1, -animatedWidth * 2 + 3, 10, getColor(getColorFromTeam(teams.get(entity), false)), StaminaConfig.INSTANCE.getConfig().outlineThickness * getAnimationProgress(entity), light, StaminaConfig.INSTANCE.getConfig().outlineIsTheBar ? getScaledStamina(entity) : 1);
            });
            ms.translate(0, 0, 0.01F);
        }
        if (StaminaConfig.INSTANCE.getConfig().renderBar) {
            ms.push();
            ms.translate(0, -StaminaConfig.INSTANCE.getConfig().verticalOffset, 0);
            ms.scale(1, getAnimationProgress(entity), 1);
            float p = StaminaConfig.INSTANCE.getConfig().paddingAmount;
            float width = StaminaConfig.INSTANCE.getConfig().width;
            if (StaminaConfig.INSTANCE.getConfig().fitToName) {
                width = -animatedWidth * 2;
            }
            renderRoundedQuad(ms.peek().getPositionMatrix(), (-width / 2F) - p, -StaminaConfig.INSTANCE.getConfig().height / 2F - p, (width / 2F) + p, StaminaConfig.INSTANCE.getConfig().height / 2F + p, 10, light, true, getColor(StaminaConfig.INSTANCE.getConfig().outerStrokeColor), getColor(StaminaConfig.INSTANCE.getConfig().outerStrokeColor), true, 0.25F, false);

            ms.translate(0, 0, 0.01F);

            renderRoundedQuad(ms.peek().getPositionMatrix(), (-width / 2F) - p, -StaminaConfig.INSTANCE.getConfig().height / 2F - p, (width / 2F) + p, StaminaConfig.INSTANCE.getConfig().height / 2F + p, 10, light, false, getColor(StaminaConfig.INSTANCE.getConfig().outlineColor.brighter()), getColor(StaminaConfig.INSTANCE.getConfig().secondaryOutlineColor), true, 0, false);

            ms.translate(0, 0, 0.01F);
            if (StaminaConfig.INSTANCE.getConfig().useTeamForMain) {
                renderRoundedQuad(ms.peek().getPositionMatrix(), -width / 2, -StaminaConfig.INSTANCE.getConfig().height / 2F, MathHelper.lerp(getScaledStamina(entity), -width / 2, width / 2), StaminaConfig.INSTANCE.getConfig().height / 2F, 10, light, false, getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor, getColorFromTeam(teams.get(entity), false), getScaledStamina(entity))), getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor.darker(), getColorFromTeam(teams.get(entity), true), getScaledStamina(entity))), StaminaConfig.INSTANCE.getConfig().mainColorGradientDirection, 0, StaminaConfig.INSTANCE.getConfig().evilMainColor);
            } else {
                renderRoundedQuad(ms.peek().getPositionMatrix(), -width / 2, -StaminaConfig.INSTANCE.getConfig().height / 2F, MathHelper.lerp(getScaledStamina(entity), -width / 2, width / 2), StaminaConfig.INSTANCE.getConfig().height / 2F, 10, light, false, getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor, StaminaConfig.INSTANCE.getConfig().mainColor, getScaledStamina(entity))), getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor.darker(), StaminaConfig.INSTANCE.getConfig().secondaryMainColor, getScaledStamina(entity))), StaminaConfig.INSTANCE.getConfig().mainColorGradientDirection, 0, StaminaConfig.INSTANCE.getConfig().evilMainColor);
            }

            ms.translate(0, 0, 0.01F);

            renderRoundedQuad(ms.peek().getPositionMatrix(), -width / 2, -StaminaConfig.INSTANCE.getConfig().height / 2F, MathHelper.lerp(getScaledStamina(entity), -width / 2, width / 2), StaminaConfig.INSTANCE.getConfig().height / 2F, 10, light, true, getColor(StaminaConfig.INSTANCE.getConfig().strokeColor), getColor(StaminaConfig.INSTANCE.getConfig().strokeColor.darker()), true, p, false);
            ms.pop();
        }
//        NativeImage t = ((LightmapTextureManagerAccessor) MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager()).getImage();
//        t.getColor();
        if (StaminaConfig.INSTANCE.getConfig().showNumber) {
            ms.translate(0, 0, 0.01F);
//            ms.translate(textRenderer.getWidth(name) / 2 - textRenderer.getWidth(text) / 2 + (3 * StaminaConfig.INSTANCE.getConfig().numberScale) + (2 * (1 - StaminaConfig.INSTANCE.getConfig().numberScale)), 0, 0);
//            ms.push();
//            ms.scale(StaminaConfig.INSTANCE.getConfig().numberScale, StaminaConfig.INSTANCE.getConfig().numberScale, 1);
//            textRenderer.draw(text, 0, 0, getColor(StaminaConfig.INSTANCE.getConfig().numberColor), false, ms.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x20000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
//            ms.pop();
            ms.push();
//                String text = String.valueOf(Math.round(getStamina(entity)));

            Vec2f pos = getPosition(text, getScaledStamina(entity), entity.getDisplayName().getString());
            ms.translate(pos.x, 0, 0);
//            fillFloat(ms, MathHelper.lerp(yeah, 1 + textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale, 10), 9, 1 + textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale, -1F, convertToShadow(bgCol, 1, g));
            ms.translate(0, pos.y, 0);
            ms.push();
            ms.scale(StaminaConfig.INSTANCE.getConfig().numberScale, StaminaConfig.INSTANCE.getConfig().numberScale, 1);
            ms.scale(1, yeah, 1);

            if (StaminaConfig.INSTANCE.getConfig().numberShadow) {
                //x - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F
                ms.translate(0, 0, 0.01F);
                textRenderer.draw(ms, text, yeah, yeah, convertToShadow(yeahhh, 0.25F, 1));
                ms.translate(0, 0, 0.01F);
                textRenderer.draw(text, 0, 0, yeahhh, false, ms.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x00000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            } else if (StaminaConfig.INSTANCE.getConfig().numberOutline) {
                //70x9 0-9
                textRenderer.draw(text, 0, 0, getColor(StaminaConfig.INSTANCE.getConfig().numberColor), false, ms.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x20000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                drawCustomOutline(ms, light, text);
            }
            ms.pop();
            ms.pop();
        }
        postRender(ms);
        ms.pop();
        ci.cancel();
    }

    @Unique
    private int convertToShadow(int color, float amount, float alpha) {
        return ColorHelper.Argb.getArgb(alpha == -1 ? ColorHelper.Argb.getAlpha(color) : (int) (alpha * 255), (int) (ColorHelper.Argb.getRed(color) * amount), (int) (ColorHelper.Argb.getGreen(color) * amount), (int) (ColorHelper.Argb.getBlue(color) * amount));
    }

    @Shadow
    @Final
    private TextRenderer textRenderer;

    /// /        NativeImage t = ((LightmapTextureManagerAccessor) MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager()).getImage();
    /// /        t.getColor();
    @Unique
    private Color getColorFromTeam(Pair<Integer, Color> yeah, boolean secondary) {
        if(yeah == null){
            return Color.GRAY;
        }
        switch (yeah.getLeft()) {
            case 0 -> {
                if (StaminaConfig.INSTANCE.getConfig().overrideHomeColor) {
                    return !secondary ? StaminaConfig.INSTANCE.getConfig().homeTeamColorOverride : StaminaConfig.INSTANCE.getConfig().secondaryHomeTeamColorOverride;
                } else {
                    return yeah.getRight();
                }
            }
            case 1 -> {
                if (StaminaConfig.INSTANCE.getConfig().overrideAwayColor) {
                    return !secondary ? StaminaConfig.INSTANCE.getConfig().awayTeamColorOverride : StaminaConfig.INSTANCE.getConfig().secondaryAwayTeamColorOverride;
                } else {
                    return yeah.getRight();
                }
            }
        }
        return Color.decode("#00000000");
    }

    @Unique
    private static void drawCustomOutline(MatrixStack ms, int light, String text) {
        RenderSystem.setShaderTexture(0, new Identifier("stamina-display:textures/outlined.png"));
        for (int i = 0; i < text.length(); i++) {
            yeah(ms, i * 6 - 1, 7 + i * 6 - 1, -1, 8, 0, 7, 9, Character.getNumericValue(text.charAt(i)) * 7, 0, 70, 9, light, getColor(StaminaConfig.INSTANCE.getConfig().numberBGColor));
        }
    }

    @Unique
    private void updateTeam(Entity entity) {
        ((BossBarAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().forEach((uuid, clientBossBar) -> {
            try {
                Color home = new Color(clientBossBar.getName().getSiblings().get(1).getSiblings().get(0).getStyle().getColor().getRgb());
                Color away = new Color(clientBossBar.getName().getSiblings().get(3).getSiblings().get(0).getStyle().getColor().getRgb());
                int team = getTeamFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()));
                updateTeam((AbstractClientPlayerEntity) entity, team, team == 0 ? away : home);
            } catch (Exception ignored) {
            }
        });
    }

    @Unique
    private void updateTeam(AbstractClientPlayerEntity entity, int team, Color c) {
        teams.put(entity, new Pair<>(team, c));
    }

    @Unique
    private int getTeam(AbstractClientPlayerEntity entity) {
        return teams.get(entity).getLeft();
    }

    @Unique
    private Color getTeamColor(AbstractClientPlayerEntity entity) {
        if(!teams.containsKey(entity)){
            return Color.WHITE;
        }
        return teams.get(entity).getRight();
    }

    @Unique
    private static void yeah(MatrixStack matrices, float x0, float x1, float y0, float y1, float z, float regionWidth, float regionHeight, float u, float v, float textureWidth, float textureHeight, int light, int col) {
        drawGoodTexture(matrices.peek().getPositionMatrix(), x0, x1, y0, y1, z, (u + 0.0F) / textureWidth, (u + regionWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + regionHeight) / textureHeight, light, col);
    }

    @Unique
    private Vec2f getPosition(String text, float scaledStamina, String entityName) {
        switch (StaminaConfig.INSTANCE.getConfig().numberPosition) {
            case BAR_CENTER -> {
                return new Vec2f(-textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale / 2F, -textRenderer.fontHeight / 2 * StaminaConfig.INSTANCE.getConfig().numberScale + 0.5F);
            }
            case FOLLOW_BAR -> {
                if (StaminaConfig.INSTANCE.getConfig().numberOutline) {
                    return new Vec2f(MathHelper.clamp(MathHelper.lerp(scaledStamina, -StaminaConfig.INSTANCE.getConfig().width / 2, StaminaConfig.INSTANCE.getConfig().width / 2) - (textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale) - (StaminaConfig.INSTANCE.getConfig().paddingAmount * 2), -StaminaConfig.INSTANCE.getConfig().width / 2, 1000), -((textRenderer.fontHeight / 2F - 1) * (StaminaConfig.INSTANCE.getConfig().numberScale)));
                } else {
                    return new Vec2f(MathHelper.clamp(MathHelper.lerp(scaledStamina, -StaminaConfig.INSTANCE.getConfig().width / 2, StaminaConfig.INSTANCE.getConfig().width / 2) - (textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale) - (StaminaConfig.INSTANCE.getConfig().paddingAmount * 2), -StaminaConfig.INSTANCE.getConfig().width / 2, 1000),
                            -((textRenderer.fontHeight / 2F - 0.5F) * (StaminaConfig.INSTANCE.getConfig().numberScale)));
                }
            }
            case AFTER_NAME -> {
                //3 isn't a magic number. that's the number of padding pixels around text
                //dude i don't even know what i'm doing anymore
                return new Vec2f(textRenderer.getWidth(entityName) / 2 - textRenderer.getWidth(text) / 2 + (3 * StaminaConfig.INSTANCE.getConfig().numberScale) + (3 * (1 - StaminaConfig.INSTANCE.getConfig().numberScale)) - 2 * StaminaConfig.INSTANCE.getConfig().numberScale - 0.5F, (1 - StaminaConfig.INSTANCE.getConfig().numberScale) * textRenderer.fontHeight / 2);
            }
            case BEFORE_NAME -> {
                return new Vec2f((-textRenderer.getWidth(entityName) - textRenderer.getWidth(text)) / 2F + textRenderer.getWidth(text) / 2 - 1.5F, 0);
            }
//                case OVER_NAME ->pos = new Vec2f(0, 0);
        }
        return null;
    }

    @Unique
    private static void postRender(MatrixStack ms) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        ms.pop();
    }

    @Unique
    private static void setupRender(MatrixStack ms) {
        ms.push();
        MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
    }


    @Unique
    private static void drawGoodTexture(Matrix4f matrix, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1, int light, int color) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
        bufferBuilder.vertex(matrix, x0, y0, z).color(color).texture(u0, v0).light(light).next();
        bufferBuilder.vertex(matrix, x0, y1, z).color(color).texture(u0, v1).light(light).next();
        bufferBuilder.vertex(matrix, x1, y1, z).color(color).texture(u1, v1).light(light).next();
        bufferBuilder.vertex(matrix, x1, y0, z).color(color).texture(u1, v0).light(light).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }


    @Unique
    private static void drawBorder(MatrixStack matrices, float x, float y, float width, int height, int color, float thickness, int light, float percentage) {
        float tempWidth = (width + thickness) * percentage;
        //TODO: this is horribly slow.
        //top
        fillFloat(matrices, x - thickness, y - thickness, x + tempWidth, y + thickness, color);
        //bottom
        fillFloat(matrices, x - thickness, y + height - thickness, x + tempWidth, y + height + thickness, color);
        //left
        fillFloat(matrices, x - thickness, y + thickness, x + thickness, y + height - thickness, color);
        //right
        if (x + tempWidth >= x + width - thickness) {
            fillFloat(matrices, x + width - thickness, y - thickness, x + tempWidth, y + height + thickness, color);
        }
    }

    @Unique
    private int getTeamFromTablist(PlayerListEntry info) {
        /*
        0 - home
        1 - away
        -1 - neither
         */
        Text displayName = info.getDisplayName();
        if (displayName != null) {
            if (displayName.getSiblings().get(2).getStyle().getColor().getName().equals("yellow")) {
                return 1;
            } else if (displayName.getSiblings().get(2).getStyle().getColor().getName().equals("blue")) {
                return 0;
            }
        }
        return -1;
    }


}
