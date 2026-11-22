package me.noobilybridge.mixin26;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.noobilybridge.StaminaDisplay26;
import me.noobilybridge.config26.StaminaSettings26;
import me.noobilybridge.config26.StaminaSettings26.NumberPosition26;
import me.noobilybridge.config26.StaminaSettings26.TextStyle26;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(AvatarRenderer.class)
public abstract class PlayerNameTagMixin<T extends Avatar> {
    private static final int FATIGUE_THRESHOLD = 15;
    private static final int GRADIENT_STEPS = 12;

    @Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void staminaDisplay$submitStamina(AvatarRenderState state, PoseStack pose, SubmitNodeCollector collector,
                                               CameraRenderState camera, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!StaminaDisplay26.modEnabled || !StaminaDisplay26.gameActive || client.level == null || state.nameTag == null) return;
        if (!(client.level.getEntity(state.id) instanceof Player player)
                || !StaminaDisplay26.isOnStaminaTeam(client, player.getUUID())) return;
        if (player.isSpectator()) return;

        UUID playerId = player.getUUID();
        if (StaminaSettings26.teammatesOnly && !StaminaDisplay26.sameTeam(client, playerId)) return;
        float reveal = StaminaDisplay26.getAnimation(playerId);
        if (reveal < 0.01F) return;

        int stamina = StaminaDisplay26.getStamina(player);
        float progress = Math.clamp((stamina - StaminaSettings26.minStamina)
                / (float) Math.max(1, 20 - StaminaSettings26.minStamina), 0, 1);
        int teamColor = StaminaDisplay26.getTeamColor(client, playerId);
        int targetNameColor = StaminaSettings26.teamForNametagText ? teamColor : StaminaSettings26.nametagTextCol;
        int nameColor = lerpColor(0xFFFFFFFF, targetNameColor, reveal);
        int targetBackground = StaminaSettings26.teamForNametagBG ? teamColor : StaminaSettings26.nametagBGColor;
        int nameBackground = lerpColor(0x20000000, targetBackground, reveal);
        MutableComponent name = state.nameTag.copy().withStyle(style -> style
                .withColor(TextColor.fromRgb(nameColor & 0xFFFFFF))
                .withShadowColor(StaminaSettings26.nametagShadow ? nameBackground : 0));
        int nameWidth = client.font.width(name);
        float width = StaminaSettings26.fitToName ? nameWidth : StaminaSettings26.width;
        Vec3 attachment = state.nameTagAttachment == null ? Vec3.ZERO : state.nameTagAttachment;
        int nameOffset = state.showExtraEars ? -10 : 0;
        ci.cancel();

        if (state.scoreText != null) {
            collector.submitNameTag(pose, attachment, nameOffset, state.scoreText, !state.isDiscrete, state.lightCoords, camera);
        }

        pose.pushPose();
        if (state.scoreText != null) pose.translate(0, 9 * 1.15F * 0.025F, 0);
        pose.translate(attachment.x, attachment.y + 0.5, attachment.z);
        pose.mulPose(camera.orientation);
        pose.translate(0, 0, StaminaSettings26.zAxisOffset);
        pose.scale(0.025F, -0.025F, 0.025F);
        pose.translate(0, nameOffset, 0);

        submitName(collector, pose, name, nameWidth, 0, nameBackground, state.lightCoords, state.isDiscrete);

        List<BarQuad> quads = new ArrayList<>();
        if (StaminaSettings26.drawNametagTeam) {
            float borderProgress = StaminaSettings26.outlineIsTheBar ? progress : 1;
            border(quads, -nameWidth / 2F - 1, -1, nameWidth + 2, 10,
                    StaminaSettings26.outlineThickness * reveal, alpha(teamColor, reveal), borderProgress);
        }
        if (StaminaSettings26.renderBar && !player.getOffhandItem().is(Items.LEATHER_BOOTS)) {
            addBar(quads, playerId, width, stamina, progress, teamColor, reveal);
        }
        if (!quads.isEmpty()) {
            collector.submitCustomGeometry(pose, RenderTypes.debugQuads(), (matrix, vertex) -> emit(quads, matrix, vertex));
        }
        submitNumber(client, state, nameWidth, pose, collector, teamColor, stamina, progress, reveal, width);
        pose.popPose();
    }

    private static void submitName(SubmitNodeCollector collector, PoseStack pose, Component name,
                                   int width, int y, int background, int light, boolean discrete) {
        int x = -width / 2;
        var visual = name.getVisualOrderText();
        if (!discrete) {
            collector.submitText(pose, x, y, visual, StaminaSettings26.nametagShadow,
                    Font.DisplayMode.NORMAL, -1, 0, light, 0);
            collector.submitText(pose, x, y, visual, false,
                    Font.DisplayMode.SEE_THROUGH, 0x80FFFFFF, background, light, 0);
        } else {
            collector.submitText(pose, x, y, visual, StaminaSettings26.nametagShadow,
                    Font.DisplayMode.NORMAL, 0x80FFFFFF, background, light, 0);
        }
    }

    private static void addBar(List<BarQuad> quads, UUID player, float width, int stamina, float progress,
                               int teamColor, float reveal) {
        float height = StaminaSettings26.height * reveal;
        float halfWidth = width / 2F;
        float y = -StaminaSettings26.verticalOffset;
        float padding = StaminaSettings26.paddingAmount;
        float rounding = Math.min(height / 2F, height * StaminaSettings26.cornerRounding);
        roundedGradient(quads, -halfWidth - padding * 2, y - height / 2F - padding * 2,
                halfWidth + padding * 2, y + height / 2F + padding * 2,
                rounding + padding, StaminaSettings26.outlineColor, StaminaSettings26.secondaryOutlineColor,
                StaminaSettings26.backgroundDirection);
        if (StaminaSettings26.paddingAmount > 0) {
            rounded(quads, -halfWidth - padding * 2, y - height / 2F - padding * 2,
                    halfWidth + padding * 2, y + height / 2F + padding * 2,
                    rounding + padding, StaminaSettings26.outerStrokeColor);
            roundedGradient(quads, -halfWidth - padding, y - height / 2F - padding,
                    halfWidth + padding, y + height / 2F + padding, rounding,
                    StaminaSettings26.outlineColor, StaminaSettings26.secondaryOutlineColor,
                    StaminaSettings26.backgroundDirection);
        }

        float filled = width * progress;
        if (filled > 0) {
            int primary = StaminaSettings26.useTeamForMain ? teamColor : StaminaSettings26.mainColor;
            int secondary = StaminaSettings26.useTeamForMain
                    ? StaminaDisplay26.getSecondaryTeamColor(Minecraft.getInstance(), player)
                    : StaminaSettings26.secondaryMainColor;
            if (StaminaSettings26.useEmptyColor) {
                primary = lerpColor(StaminaSettings26.emptyMainColor, primary, progress);
                secondary = lerpColor(StaminaSettings26.secondaryEmptyMainColor, secondary, progress);
            }
            if (StaminaSettings26.lowStaminaCue && stamina <= FATIGUE_THRESHOLD) {
                primary = StaminaSettings26.emptyMainColor;
                secondary = StaminaSettings26.secondaryEmptyMainColor;
            }
            if (StaminaSettings26.evilMainColor) {
                primary = invert(primary);
                secondary = invert(secondary);
            }
            float fillEnd = -halfWidth + filled;
            roundedGradient(quads, -halfWidth, y - height / 2F, fillEnd, y + height / 2F,
                    rounding, primary, secondary, StaminaSettings26.mainColorGradientDirection);
            border(quads, -halfWidth, y - height / 2F, filled, height,
                    Math.max(0.1F, padding / 2F), StaminaSettings26.strokeColor, 1);
        }
    }

    private static void submitNumber(Minecraft client, AvatarRenderState state, int nameWidth,
                                    PoseStack pose, SubmitNodeCollector collector, int teamColor,
                                    int stamina, float progress, float reveal, float width) {
        if (!StaminaSettings26.showNumber) return;
        String text = StaminaSettings26.numberPosition == NumberPosition26.AFTER_NAME
                ? StaminaSettings26.innerText + stamina + StaminaSettings26.outerText
                : Integer.toString(stamina);
        float scale = StaminaSettings26.numberScale * reveal;
        float textWidth = client.font.width(text) * scale;
        float x;
        float y;
        if (StaminaSettings26.numberPosition == NumberPosition26.BAR_CENTER && StaminaSettings26.renderBar) {
            x = -textWidth / 2F;
            y = -StaminaSettings26.verticalOffset - client.font.lineHeight * scale / 2F + 0.5F * scale;
        } else if (StaminaSettings26.numberPosition == NumberPosition26.FOLLOW_BAR && StaminaSettings26.renderBar) {
            x = Math.clamp((progress - 0.5F) * width - textWidth - StaminaSettings26.paddingAmount * 2,
                    -width / 2F, 1000F);
            y = -StaminaSettings26.verticalOffset - (client.font.lineHeight / 2F - 1) * scale;
        } else {
            x = nameWidth / 2F + 1;
            y = (1 - scale) * client.font.lineHeight / 2F;
        }
        int color = StaminaSettings26.teamForNumber ? teamColor
                : lerpColor(StaminaSettings26.emptyNumberColor, StaminaSettings26.numberColor, progress);
        int background = StaminaSettings26.teamForNumberBG ? teamColor : StaminaSettings26.numberBGColor;
        TextStyle26 style = StaminaSettings26.textStyle;
        pose.pushPose();
        pose.translate(x, y, StaminaSettings26.zAxisOffset);
        pose.scale(scale, scale, 1);
        int outlineColor = style == TextStyle26.OUTLINE ? background : 0;
        Component component = Component.literal(text).withStyle(s -> s.withColor(TextColor.fromRgb(color & 0xFFFFFF)));
        collector.submitText(pose, 0, 0, component.getVisualOrderText(), style == TextStyle26.SHADOW,
                Font.DisplayMode.NORMAL, color, style == TextStyle26.SHADOW ? background : 0, state.lightCoords, outlineColor);
        pose.popPose();
    }

    private static void roundedGradient(List<BarQuad> out, float x0, float y0, float x1, float y1,
                                       float radius, int first, int second, boolean vertical) {
        if (x1 <= x0 || y1 <= y0) return;
        radius = Math.max(0, Math.min(radius, Math.min((x1 - x0) / 2, (y1 - y0) / 2)));
        int steps = GRADIENT_STEPS;
        for (int i = 0; i < steps; i++) {
            float t0 = i / (float) steps;
            float t1 = (i + 1) / (float) steps;
            float tm = (t0 + t1) / 2;
            int color = lerpColor(first, second, tm);
            if (vertical) {
                float top = y0 + (y1 - y0) * t0;
                float bottom = y0 + (y1 - y0) * t1;
                float inset = roundedInset(tm * (y1 - y0), y1 - y0, radius);
                rect(out, x0 + inset, top, x1 - inset, bottom, color);
            } else {
                float left = x0 + (x1 - x0) * t0;
                float right = x0 + (x1 - x0) * t1;
                float inset = roundedInset(tm * (x1 - x0), x1 - x0, radius);
                rect(out, left, y0 + inset, right, y1 - inset, color);
            }
        }
    }

    private static float roundedInset(float position, float length, float radius) {
        if (radius <= 0) return 0;
        float edge = Math.min(position, length - position);
        if (edge >= radius) return 0;
        float delta = radius - edge;
        return radius - (float) Math.sqrt(Math.max(0, radius * radius - delta * delta));
    }

    private static void rounded(List<BarQuad> out, float x0, float y0, float x1, float y1, float radius, int color) {
        if (x1 <= x0 || y1 <= y0 || color >>> 24 == 0) return;
        radius = Math.max(0, Math.min(radius, Math.min((x1 - x0) / 2, (y1 - y0) / 2)));
        if (radius == 0) { rect(out, x0, y0, x1, y1, color); return; }
        int steps = 8;
        for (int i = 0; i < steps; i++) {
            float a0 = (float) (Math.PI * 2 * i / steps);
            float a1 = (float) (Math.PI * 2 * (i + 1) / steps);
            float cx = i < 4 ? x1 - radius : x0 + radius;
            float cy = i < 2 || i >= 6 ? y0 + radius : y1 - radius;
            float px0 = cx + (float) Math.cos(a0) * radius;
            float py0 = cy + (float) Math.sin(a0) * radius;
            float px1 = cx + (float) Math.cos(a1) * radius;
            float py1 = cy + (float) Math.sin(a1) * radius;
            triangle(out, cx, cy, px0, py0, px1, py1, color);
        }
        rect(out, x0 + radius, y0, x1 - radius, y1, color);
        rect(out, x0, y0 + radius, x1, y1 - radius, color);
    }

    private static void border(List<BarQuad> out, float x, float y, float width, float height,
                               float thickness, int color, float percentage) {
        float end = x + (width + thickness) * Math.clamp(percentage, 0, 1);
        if (end <= x || thickness <= 0 || color >>> 24 == 0) return;
        rect(out, x - thickness, y - thickness, end, y + thickness, color);
        rect(out, x - thickness, y + height - thickness, end, y + height + thickness, color);
        rect(out, x - thickness, y - thickness, x + thickness, y + height + thickness, color);
        if (end >= x + width - thickness) rect(out, x + width - thickness, y - thickness, end, y + height + thickness, color);
    }

    private static void rect(List<BarQuad> out, float x0, float y0, float x1, float y1, int color) {
        if (x1 <= x0 || y1 <= y0 || color >>> 24 == 0) return;
        out.add(new BarQuad(x0, y0, x0, y1, x1, y1, x1, y0, 0, color));
    }

    private static void triangle(List<BarQuad> out, float cx, float cy, float x0, float y0, float x1, float y1, int color) {
        out.add(new BarQuad(cx, cy, x0, y0, x1, y1, x1, y1, 0, color));
    }

    private static void emit(List<BarQuad> quads, PoseStack.Pose pose, VertexConsumer vertices) {
        for (BarQuad q : quads) {
            vertices.addVertex(pose, q.x0, q.y0, q.z).setColor(q.color);
            vertices.addVertex(pose, q.x1, q.y1, q.z).setColor(q.color);
            vertices.addVertex(pose, q.x2, q.y2, q.z).setColor(q.color);
            vertices.addVertex(pose, q.x3, q.y3, q.z).setColor(q.color);
        }
    }

    private static int alpha(int color, float amount) {
        int a = Math.round((color >>> 24 & 255) * amount);
        return a << 24 | color & 0xFFFFFF;
    }

    private static int invert(int color) { return color & 0xFF000000 | (~color & 0x00FFFFFF); }

    private static int lerpColor(int a, int b, float t) {
        int alpha = Math.round((a >>> 24 & 255) * (1 - t) + (b >>> 24 & 255) * t);
        int red = Math.round((a >>> 16 & 255) * (1 - t) + (b >>> 16 & 255) * t);
        int green = Math.round((a >>> 8 & 255) * (1 - t) + (b >>> 8 & 255) * t);
        int blue = Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private record BarQuad(float x0, float y0, float x1, float y1, float x2, float y2,
                           float x3, float y3, float z, int color) { }
}
