package me.noobilybridge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.noobilybridge.StaminaDisplay;
import me.noobilybridge.config.NumberPosition;
import me.noobilybridge.config.StaminaConfig;
import me.noobilybridge.config.TextStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
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
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.spi.AbstractResourceBundleProvider;

import static me.noobilybridge.StaminaDisplay.*;


@Mixin(EntityRenderer.class)
public abstract class EntityRenderMixin<T extends Entity> {

    @Unique
    private static Color eviler;


    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getTextRenderer()Lnet/minecraft/client/font/TextRenderer;"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    void ough(T entity, Text name, MatrixStack ms, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci, double d, boolean bl, float f, int i, Matrix4f matrix4f, float g, int j) {
//        String address = MinecraftClient.getInstance().getCurrentServerEntry().address;
//        if (!(address.toLowerCase().endsWith("blockey.net") || address.toLowerCase().endsWith("karlmc.com"))) {
//            return;
//        }
        if (!(entity instanceof AbstractClientPlayerEntity)) {
            return;
        }
        updateTeam(entity);
        if (getTeam((AbstractClientPlayerEntity) entity) == -1) {
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
        updateLightMapColor(light);
        String text = String.valueOf(Math.round(getStamina(entity)));
        if (StaminaConfig.INSTANCE.getConfig().numberPosition == NumberPosition.AFTER_NAME && StaminaConfig.INSTANCE.getConfig().showNumber) {
            text = StaminaConfig.INSTANCE.getConfig().innerText + text + StaminaConfig.INSTANCE.getConfig().outerText;
        }
        float h = -(textRenderer.getWidth(name) + (textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale)) / 2;
        float yeah = getAnimationProgress(entity);
        float animatedWidth = MathHelper.lerp(StaminaConfig.INSTANCE.getConfig().numberPosition == NumberPosition.AFTER_NAME && StaminaConfig.INSTANCE.getConfig().showNumber ? yeah : 0, -textRenderer.getWidth(name) / 2, h);
        int yeahhh = getColor(getLerpedColor(Color.WHITE, StaminaConfig.INSTANCE.getConfig().teamForNametagText ? getColorFromTeam(getTeam((AbstractClientPlayerEntity) entity), false) : StaminaConfig.INSTANCE.getConfig().nametagTextCol, yeah), eviler);
        int bgCol = convertToShadow(getColor(getLerpedColor(new Color(0, 0, 0, 0.125F), StaminaConfig.INSTANCE.getConfig().teamForNametagBG ? getColorFromTeam(getTeam((AbstractClientPlayerEntity) entity), false) : StaminaConfig.INSTANCE.getConfig().nametagBGColor, yeah)), 1, 0.125F);

        fillFloat(ms, (animatedWidth) - 1, 9, -animatedWidth + 2, -1, convertToShadow(bgCol, 1, g));
        if (StaminaConfig.INSTANCE.getConfig().nametagShadow) {
            ms.translate(0, 0, -StaminaConfig.INSTANCE.getConfig().zAxisOffset);
            textRenderer.draw(
                    name, animatedWidth + yeah, (float) i + yeah, convertToShadow(yeahhh, 0.25F, 0.125F), false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, light
            );
        }
        ms.translate(0, 0, -StaminaConfig.INSTANCE.getConfig().zAxisOffset);
        textRenderer.draw(
                name, animatedWidth, (float) i, convertToShadow(yeahhh, 1, 0.125F), false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, light
        );
        if(StaminaConfig.INSTANCE.getConfig().numberPosition == NumberPosition.AFTER_NAME && StaminaConfig.INSTANCE.getConfig().showNumber) {
            renderNumber(entity, ms, vertexConsumers, bl, text, yeah, yeahhh);
        }
        if (bl) {
            ms.push();
            if (StaminaConfig.INSTANCE.getConfig().nametagShadow) {
                textRenderer.draw(name, animatedWidth + yeah, (float) i + yeah, convertToShadow(yeahhh, 0.25F, 1), false, ms.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x00000080, light);
                ms.translate(0, 0, -StaminaConfig.INSTANCE.getConfig().zAxisOffset);
            }
            textRenderer.draw(name, animatedWidth, (float) i, yeahhh, false, ms.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            ms.pop();
        }
        setupRender(ms);
        ms.scale(1, 1, -1);
        ms.translate(0, 0, StaminaConfig.INSTANCE.getConfig().zAxisOffset);
        drawNametagBorders(entity, ms, animatedWidth);
        if (StaminaConfig.INSTANCE.getConfig().renderBar && !((AbstractClientPlayerEntity) entity).getOffHandStack().isOf(Items.LEATHER_BOOTS)) {
            renderBar(entity, ms, animatedWidth);
        }
        ms.translate(0, 0, 0.25F);

        if (StaminaConfig.INSTANCE.getConfig().showNumber && StaminaConfig.INSTANCE.getConfig().numberPosition != NumberPosition.AFTER_NAME) {
            renderNumber(entity, ms, vertexConsumers, bl, text, yeah, yeahhh);
        }
        postRender(ms);
        ms.pop();
        ci.cancel();
    }

    @Unique
    private static void updateLightMapColor(int light) {
        NativeImage t = ((LightmapTextureManagerAccessor) MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager()).getImage();
        int evil = t.getColor(LightmapTextureManager.getBlockLightCoordinates(light), LightmapTextureManager.getSkyLightCoordinates(light));
        eviler = new Color(ColorHelper.Abgr.getRed(evil), ColorHelper.Abgr.getGreen(evil), ColorHelper.Abgr.getBlue(evil), ColorHelper.Abgr.getAlpha(evil));
    }

    @Unique
    private void drawNametagBorders(T entity, MatrixStack ms, float animatedWidth) {
        if (StaminaConfig.INSTANCE.getConfig().drawNametagTeam) {
            //TODO: this currently scales the outline both inside the nametag and out, but i like how it looks
            teams.forEach((uuid, team) -> {
                drawBorder(ms, (animatedWidth - 1), -1, -animatedWidth * 2 + 3, 10, getColor(getColorFromTeam(teams.get(entity), false), eviler), StaminaConfig.INSTANCE.getConfig().outlineThickness * getAnimationProgress(entity), StaminaConfig.INSTANCE.getConfig().outlineIsTheBar ? getScaledStamina(entity) : 1);
            });
            ms.translate(0, 0, StaminaConfig.INSTANCE.getConfig().zAxisOffset);
        }
    }

    @Unique
    private void renderBar(T entity, MatrixStack ms, float animatedWidth) {
        ms.push();
        ms.translate(0, -StaminaConfig.INSTANCE.getConfig().verticalOffset, 0);
        ms.scale(1, getAnimationProgress(entity), 1);
        float p = StaminaConfig.INSTANCE.getConfig().paddingAmount;
        float width = StaminaConfig.INSTANCE.getConfig().width;
        if (StaminaConfig.INSTANCE.getConfig().fitToName) {
            width = -animatedWidth * 2;
        }
        renderRoundedQuad(ms.peek().getPositionMatrix(), (-width / 2F) - p * 2, -StaminaConfig.INSTANCE.getConfig().height / 2F - p * 2, (width / 2F) + p * 2 + 1, StaminaConfig.INSTANCE.getConfig().height / 2F + p * 2, 10, false, getColor(StaminaConfig.INSTANCE.getConfig().outlineColor, eviler), getColor(StaminaConfig.INSTANCE.getConfig().secondaryOutlineColor, eviler), StaminaConfig.INSTANCE.getConfig().backgroundDirection, 0, false);
        ms.translate(0, 0, StaminaConfig.INSTANCE.getConfig().zAxisOffset);
        if (StaminaConfig.INSTANCE.getConfig().cornerRounding == 0) {
            drawBorderSpecial(ms, -width / 2 - (p * 1.5F), -StaminaConfig.INSTANCE.getConfig().height / 2F - (p) - p / 2, (width) + p * 3 + 1, StaminaConfig.INSTANCE.getConfig().height + (p * 3), getColor(StaminaConfig.INSTANCE.getConfig().outerStrokeColor, eviler), p / 2);
        } else {
            renderRoundedQuad(ms.peek().getPositionMatrix(), (-width / 2F) - p * 2, -StaminaConfig.INSTANCE.getConfig().height / 2F - p * 2, (width / 2F) + p * 2 + 1, StaminaConfig.INSTANCE.getConfig().height / 2F + p * 2, 10, true, getColor(StaminaConfig.INSTANCE.getConfig().outerStrokeColor, eviler), getColor(StaminaConfig.INSTANCE.getConfig().outerStrokeColor, eviler), true, p / 2, false);
        }

        ms.translate(0, 0, StaminaConfig.INSTANCE.getConfig().zAxisOffset);
        if (StaminaConfig.INSTANCE.getConfig().useTeamForMain) {
            renderRoundedQuad(ms.peek().getPositionMatrix(), -width / 2, -StaminaConfig.INSTANCE.getConfig().height / 2F, MathHelper.lerp(getScaledStamina(entity), -width / 2, width / 2 + 1), StaminaConfig.INSTANCE.getConfig().height / 2F, 10, false, getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor, getColorFromTeam(teams.get(entity), false), StaminaConfig.INSTANCE.getConfig().useEmptyColor ? getScaledStamina(entity) : 1), eviler), getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().secondaryEmptyMainColor, getColorFromTeam(teams.get(entity), true), StaminaConfig.INSTANCE.getConfig().useEmptyColor ? getScaledStamina(entity) : 1), eviler), StaminaConfig.INSTANCE.getConfig().mainColorGradientDirection, 0, StaminaConfig.INSTANCE.getConfig().evilMainColor);
        } else {
            renderRoundedQuad(ms.peek().getPositionMatrix(), -width / 2, -StaminaConfig.INSTANCE.getConfig().height / 2F, MathHelper.lerp(getScaledStamina(entity), -width / 2, width / 2 + 1), StaminaConfig.INSTANCE.getConfig().height / 2F, 10, false, getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor, StaminaConfig.INSTANCE.getConfig().mainColor, StaminaConfig.INSTANCE.getConfig().useEmptyColor ? getScaledStamina(entity) : 1), eviler), getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().secondaryEmptyMainColor, StaminaConfig.INSTANCE.getConfig().secondaryMainColor, StaminaConfig.INSTANCE.getConfig().useEmptyColor ? getScaledStamina(entity) : 1), eviler), StaminaConfig.INSTANCE.getConfig().mainColorGradientDirection, 0, StaminaConfig.INSTANCE.getConfig().evilMainColor);
        }
        //TODO: shitty workaround, but good enough for now
        ms.translate(0, 0, StaminaConfig.INSTANCE.getConfig().zAxisOffset);
        if (StaminaConfig.INSTANCE.getConfig().cornerRounding == 0) {
            drawBorderSpecial(ms, -width / 2 + (p / 2), -StaminaConfig.INSTANCE.getConfig().height / 2F + (p / 2), MathHelper.lerp(getScaledStamina(entity), -width / 2, width / 2 + 1) + width / 2 - (p), StaminaConfig.INSTANCE.getConfig().height - (p), getColor(StaminaConfig.INSTANCE.getConfig().strokeColor, eviler), p / 2);
        } else {
            renderRoundedQuad(ms.peek().getPositionMatrix(), -width / 2, -StaminaConfig.INSTANCE.getConfig().height / 2F, MathHelper.lerp(getScaledStamina(entity), -width / 2, width / 2 + 1), StaminaConfig.INSTANCE.getConfig().height / 2F, 10, true, getColor(StaminaConfig.INSTANCE.getConfig().strokeColor, eviler), getColor(StaminaConfig.INSTANCE.getConfig().strokeColor.darker(), eviler), true, p / 2, false);
        }
        ms.pop();
    }

    @Unique
    private void renderNumber(T entity, MatrixStack ms, VertexConsumerProvider vertexConsumers, boolean bl, String text, float yeah, int yeahhh) {
        ms.translate(0, 0, StaminaConfig.INSTANCE.getConfig().zAxisOffset);
        ms.push();

        Vec2f pos = getPosition(text, getScaledStamina(entity), entity.getDisplayName().getString());
            /*
            The Minecraft text renderer is a nightmare to work with.

            - Text scales properly only on the X axis, but not the Y.
            - The text shadow is 1 pixel wider on the right side.
            - The text shadow rendering code is horribly unoptimized.
            - The text shadow will render in front of the actual text in many cases.
            - Properly centering text on the X axis requires truncating.

             */
        float animatedWidth = MathHelper.lerp(StaminaConfig.INSTANCE.getConfig().numberPosition == NumberPosition.AFTER_NAME && StaminaConfig.INSTANCE.getConfig().showNumber ? yeah : 0, -textRenderer.getWidth(entity.getDisplayName().getString()) / 2, -(textRenderer.getWidth(entity.getDisplayName().getString()) + (textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale)) / 2);
//        DrawableHelper.fill(ms, (int) MathHelper.lerp(yeah, pos.x, -animatedWidth), (int) pos.y, (int) (pos.x + 1), (int) (pos.y + 1), 0xFFFFFFFF);
        ms.translate(MathHelper.lerp(StaminaConfig.INSTANCE.getConfig().numberPosition == NumberPosition.AFTER_NAME ? 1 - yeah : 0, pos.x, -animatedWidth), pos.y, 0);
        ms.push();
        ms.scale(StaminaConfig.INSTANCE.getConfig().numberScale * yeah, StaminaConfig.INSTANCE.getConfig().numberScale, 1);
        int col = StaminaConfig.INSTANCE.getConfig().teamForNumber ? yeahhh : getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyNumberColor, StaminaConfig.INSTANCE.getConfig().numberColor, getScaledStamina(entity)), eviler);
        int bgCol = StaminaConfig.INSTANCE.getConfig().teamForNumberBG ? convertToShadow(yeahhh, 0.25F, bl ? -1 : 0.125F) : getColor(StaminaConfig.INSTANCE.getConfig().numberBGColor, eviler);

        if (StaminaConfig.INSTANCE.getConfig().textStyle == TextStyle.SHADOW) {
            //x - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F
            textRenderer.draw(ms, text, yeah, yeah, bgCol);
        }
        ms.translate(0, 0, StaminaConfig.INSTANCE.getConfig().zAxisOffset);
        textRenderer.draw(text, 0, 0, convertToShadow(col, 1F, bl ? 1 : 0.125F), false, ms.peek().getPositionMatrix(), vertexConsumers, bl ? TextRenderer.TextLayerType.NORMAL : TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        if (StaminaConfig.INSTANCE.getConfig().textStyle == TextStyle.OUTLINE) {
            //70x9 0-9
            textRenderer.draw(text, 0, 0, convertToShadow(col, 1F, bl ? 1 : 0.125F), false, ms.peek().getPositionMatrix(), vertexConsumers, bl ? TextRenderer.TextLayerType.NORMAL : TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            drawCustomOutline(ms, text, bgCol);
        }
        ms.pop();
        ms.pop();
    }

    @Unique
    private int convertToShadow(int color, float amount, float alpha) {
        return ColorHelper.Argb.getArgb(alpha == -1 ? ColorHelper.Argb.getAlpha(color) : (int) (alpha * 255), (int) (ColorHelper.Argb.getRed(color) * amount), (int) (ColorHelper.Argb.getGreen(color) * amount), (int) (ColorHelper.Argb.getBlue(color) * amount));
    }

    @Shadow
    @Final
    private TextRenderer textRenderer;


    @Unique
    private Color getColorFromTeam(int yeah, boolean secondary) {
        switch (yeah) {
            case 0 -> {
                if (StaminaConfig.INSTANCE.getConfig().overrideHomeColor) {
                    return secondary ? StaminaConfig.INSTANCE.getConfig().secondaryHomeTeamColorOverride : StaminaConfig.INSTANCE.getConfig().homeTeamColorOverride;
                } else {
                    return secondary ? homeColor.darker() : homeColor;
                }
            }
            case 1 -> {
                if (StaminaConfig.INSTANCE.getConfig().overrideAwayColor) {
                    return secondary ? StaminaConfig.INSTANCE.getConfig().secondaryAwayTeamColorOverride : StaminaConfig.INSTANCE.getConfig().awayTeamColorOverride;
                } else {
                    return secondary ? awayColor.darker() : awayColor;
                }
            }
        }
        return Color.WHITE;
    }

    @Unique
    private void drawCustomOutline(MatrixStack ms, String text, int col) {
        RenderSystem.setShaderTexture(0, new Identifier("stamina-display:textures/outlined.png"));
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                yeah(ms, width - 1, 7 + width - 1, -1, 8, 0, 7, 9, Character.getNumericValue(text.charAt(i)) * 7, 0, 70, 9, col);
            }
            width += textRenderer.getWidth(String.valueOf(text.charAt(i)));
        }
    }

    @Unique
    private void updateTeam(Entity entity) {
        int team = getTeamFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()));
        updateTeam((AbstractClientPlayerEntity) entity, team);
    }

    @Unique
    private void updateTeam(AbstractClientPlayerEntity entity, int team) {
        teams.put(entity, team);
    }


    @Unique
    private static void yeah(MatrixStack matrices, float x0, float x1, float y0, float y1, float z, float regionWidth, float regionHeight, float u, float v, float textureWidth, float textureHeight, int col) {
        drawGoodTexture(matrices.peek().getPositionMatrix(), x0, x1, y0, y1, z, (u + 0.0F) / textureWidth, (u + regionWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + regionHeight) / textureHeight, col);
    }

    @Unique
    private Vec2f getPosition(String text, float scaledStamina, String entityName) {
        switch (StaminaConfig.INSTANCE.getConfig().numberPosition) {
            case BAR_CENTER -> {
                return new Vec2f(-textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale / 2F, -(textRenderer.fontHeight) / 2 * StaminaConfig.INSTANCE.getConfig().numberScale - StaminaConfig.INSTANCE.getConfig().verticalOffset + 0.5F * StaminaConfig.INSTANCE.getConfig().numberScale);
            }
            case FOLLOW_BAR -> {
                float width = StaminaConfig.INSTANCE.getConfig().width;
                if (StaminaConfig.INSTANCE.getConfig().fitToName) {
                    width = textRenderer.getWidth(entityName);
                }
                return new Vec2f(MathHelper.clamp(MathHelper.lerp(scaledStamina, -width / 2, width / 2) - (textRenderer.getWidth(text) * StaminaConfig.INSTANCE.getConfig().numberScale) - (StaminaConfig.INSTANCE.getConfig().paddingAmount * 2), -width / 2, 1000), -((textRenderer.fontHeight / 2F - 1) * (StaminaConfig.INSTANCE.getConfig().numberScale)) - StaminaConfig.INSTANCE.getConfig().verticalOffset);
            }
            case AFTER_NAME -> {
                float yeah = StaminaConfig.INSTANCE.getConfig().textStyle == TextStyle.OUTLINE ? StaminaConfig.INSTANCE.getConfig().numberScale : 0;
                return new Vec2f(yeah + (textRenderer.getWidth(entityName) - textRenderer.getWidth(text) + textRenderer.getWidth(text) * (1 - StaminaConfig.INSTANCE.getConfig().numberScale)) / 2F, (1 - StaminaConfig.INSTANCE.getConfig().numberScale) * textRenderer.fontHeight / 2);
            }
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
    private static void drawGoodTexture(Matrix4f matrix, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1, int color) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix, x0, y0, z).color(color).texture(u0, v0).next();
        bufferBuilder.vertex(matrix, x0, y1, z).color(color).texture(u0, v1).next();
        bufferBuilder.vertex(matrix, x1, y1, z).color(color).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x1, y0, z).color(color).texture(u1, v0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }


    @Unique
    private static void drawBorder(MatrixStack matrices, float x, float y, float width, float height, int color, float thickness, float percentage) {
        float tempWidth = (width + thickness) * percentage;
        //TODO: this is horribly slow.
        //top
        fillFloat(matrices, x - thickness, y - thickness, x + tempWidth, y + thickness, color);
        //bottom
        fillFloat(matrices, x - thickness, y + height - thickness, x + tempWidth, y + height + thickness, color);
        //left
        fillFloat(matrices, x - thickness, y - thickness, x + thickness, y + height + thickness, color);

        //right
        if (x + tempWidth >= x + width - thickness) {
            fillFloat(matrices, x + width - thickness, y - thickness, x + tempWidth, y + height + thickness, color);
        }
    }

    @Unique
    private static void drawBorderSpecial(MatrixStack matrices, float x, float y, float width, float height, int color, float thickness) {
        float tempWidth = (width + thickness);
        //TODO: this is horribly slow.
        //top
        fillFloat(matrices, x - thickness, y - thickness, x + tempWidth, y + thickness, color);
        //bottom
        fillFloat(matrices, x - thickness, y + height - thickness, x + tempWidth, y + height + thickness, color);
        //left
        fillFloat(matrices, x - thickness, y + thickness, x + thickness, y + height - thickness, color);
        //right
        fillFloat(matrices, x + width - thickness, y + thickness, x + tempWidth, y + height - thickness, color);
    }


}
