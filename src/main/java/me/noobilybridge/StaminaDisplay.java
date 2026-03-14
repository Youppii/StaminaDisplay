package me.noobilybridge;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.yacl3.config.GsonConfigInstance;
import me.noobilybridge.config.StaminaConfig;
import me.noobilybridge.mixin.BossBarAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Unique;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class StaminaDisplay implements ClientModInitializer {
    //Left - Stamina
    //Right - Animation progress
    public static Map<AbstractClientPlayerEntity, Pair<Float, Float>> staminaValues = new HashMap<>();
    public static Map<AbstractClientPlayerEntity, Integer> teams = new HashMap<>();
    public static Color homeColor = Color.WHITE;
    public static Color awayColor = Color.WHITE;
    public static boolean modAllowed = true;
    public static boolean gameActive = false;
    public static boolean modEnabled = true;
    public static float clientStamina = 0F;
    public static KeyBinding toggleMod;
    public static KeyBinding toggleBars;
    public static float clientAnimationProgress = 0F;
    public static boolean openConfig = false;
    public static Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Text.class, new Text.Serializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .registerTypeHierarchyAdapter(Color.class, new GsonConfigInstance.ColorTypeAdapter())
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    @Override
    public void onInitializeClient() {
        StaminaConfig.INSTANCE.load();
        toggleMod = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.staminadisplay.toggleMod", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "Stamina Display"));
        toggleBars = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.staminadisplay.toggleBars", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, "Stamina Display"));
        if (StaminaConfig.INSTANCE.getConfig().iceTranslucencyDisable) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getSolid(), Blocks.ICE, Blocks.PACKED_ICE, Blocks.FROSTED_ICE, Blocks.BLUE_ICE);
        }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
                    ClientCommandManager.literal("staminadisplay").executes(c -> {
                        openConfig = true;
                        return 1;
                    })
            );
        });

        WorldRenderEvents.START.register(worldRenderContext -> {

            for (BossBar clientBossBar : ((BossBarAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().values()) {
                try {
                    homeColor = new Color(clientBossBar.getName().getSiblings().get(3).getSiblings().get(0).getStyle().getColor().getRgb());
                    awayColor = new Color(clientBossBar.getName().getSiblings().get(1).getSiblings().get(0).getStyle().getColor().getRgb());
                    gameActive = true;
                    break;
                } catch (Exception e) {
                    gameActive = false;
                }
            }
            boolean yeah = MinecraftClient.getInstance().player.hasVehicle();
            clientStamina = (float) ease(clientStamina, yeah ? 0 : MinecraftClient.getInstance().player.getHungerManager().getFoodLevel(), StaminaConfig.INSTANCE.getConfig().animationSpeed);
            clientAnimationProgress = (float) StaminaDisplay.ease(clientAnimationProgress, yeah || !modEnabled || !gameActive ? 0 : 1, StaminaConfig.INSTANCE.getConfig().animationSpeed);
        });
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (openConfig) {
                client.setScreen(StaminaConfig.getScreen(client.currentScreen));
                openConfig = false;
            }
            while (toggleMod.wasPressed()) {
                modEnabled = !modEnabled;
            }
            while (toggleBars.wasPressed()) {
                StaminaConfig.INSTANCE.getConfig().renderBar = !StaminaConfig.INSTANCE.getConfig().renderBar;
            }
        });
    }

    public static int getTeam(AbstractClientPlayerEntity entity) {
        if (teams.containsKey(entity)) {
            return teams.get(entity);
        }
        return -1;
    }

    @Unique
    public static int getTeamFromTablist(PlayerListEntry info) {
        /*
        0 - home
        1 - away
        -1 - neither
         */
        if (info == null) {
            return -1;
        }
        Text displayName = info.getDisplayName();
        try {
            if (displayName != null) {
                switch (displayName.getSiblings().get(2).getStyle().getColor().getName()) {
                    case "yellow" -> {
                        return 1;
                    }
                    case "blue" -> {
                        return 0;
                    }
                    //TODO:
                    case "gold" -> {
                        return -1;
                    }
                    case "red" -> {
                        return -1;
                    }
                    case "white" -> {
                        return -1;
                    }
                }
            }
        } catch (Exception ignored) {
            return -1;
        }
        return -1;
    }

    public static float getStamina(Entity e) {
        if (e.equals(MinecraftClient.getInstance().player)) {
            return clientStamina;
        }
        if (!staminaValues.containsKey(e)) {
            return 0;
        }
        return MathHelper.clamp(StaminaDisplay.staminaValues.get(e).getLeft() - StaminaConfig.INSTANCE.getConfig().minStamina, 0, 20);
    }

    public static float getScaledStamina(Entity e) {
        if (e.equals(MinecraftClient.getInstance().player)) {
            return MathHelper.clamp(clientStamina - StaminaConfig.INSTANCE.getConfig().minStamina, 0, 20) / getMaxStamina();
        }
        return MathHelper.clamp(StaminaDisplay.staminaValues.get(e).getLeft() - StaminaConfig.INSTANCE.getConfig().minStamina, 0, 20) / getMaxStamina();
    }

    public static float getAnimationProgress(Entity entity) {
        if (entity.equals(MinecraftClient.getInstance().player)) {
            return clientAnimationProgress;
        }
        return staminaValues.get(entity).getRight();
    }

    public static void updateStaminaForEntity(Entity entity) {
        boolean yeah = entity.hasVehicle();
        int staminaVal = getStaminaFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()));
        StaminaDisplay.staminaValues.putIfAbsent((AbstractClientPlayerEntity) entity, new Pair<>((float) staminaVal, 0F));
        float lastStamina = StaminaDisplay.staminaValues.get(entity).getLeft();
        StaminaDisplay.staminaValues.put((AbstractClientPlayerEntity) entity, new Pair<>((float) StaminaDisplay.ease(lastStamina, staminaVal, StaminaConfig.INSTANCE.getConfig().animationSpeed),
                (float) StaminaDisplay.ease(staminaValues.get(entity).getRight(), yeah || !modEnabled || !gameActive ? 0 : 1, StaminaConfig.INSTANCE.getConfig().animationSpeed)));
    }

    public static float getMaxStamina() {
        return MathHelper.clamp(20 - StaminaConfig.INSTANCE.getConfig().minStamina, 1, 20);
    }

    public static int getColor(Color col, Color light) {
        return ColorHelper.Argb.getArgb(col.getAlpha(), (int) ((col.getRed() / 255F) * (light.getRed() / 255F) * 255), (int) ((col.getGreen() / 255F) * (light.getGreen() / 255F) * 255), (int) ((col.getBlue() / 255F) * (light.getBlue() / 255F) * 255));
    }

    public static int getColor(Color col) {
        return ColorHelper.Argb.getArgb(col.getAlpha(), col.getRed(), col.getGreen(), col.getBlue());
    }

    @Unique
    public static int getStaminaFromTablist(PlayerListEntry info) {
        if (info == null) {
            return 0;
        }
        Text displayName = info.getDisplayName();
        if (displayName != null) {
            String displayNameString = displayName.getString();
            int startBracket = displayNameString.lastIndexOf('[');
            int endBracket = displayNameString.lastIndexOf(']');
            if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
                String staminaString = displayNameString.substring(startBracket + 1, endBracket).trim();
                try {
                    return Integer.parseInt(staminaString);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    public static double ease(double start, double end, float speed) {
        return (start + (end - start) * (1 - Math.exp(-((double) MinecraftClient.getInstance().getRenderTime() / 1000000000) * speed)));
    }

    public static Color getLerpedColor(Color c1, Color c2, float percent) {
        return new Color(MathHelper.clamp(MathHelper.lerp(percent, c1.getRed(), c2.getRed()), 0, 255), MathHelper.clamp(MathHelper.lerp(percent, c1.getGreen(), c2.getGreen()), 0, 255), MathHelper.clamp(MathHelper.lerp(percent, c1.getBlue(), c2.getBlue()), 0, 255));
    }


    @Unique
    public static void fillFloat(MatrixStack matrices, float x1, float y1, float x2, float y2, int color) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        if (x1 < x2) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        float f = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float h = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float j = (float) ColorHelper.Argb.getBlue(color) / 255.0F;
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) 0).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y2, (float) 0).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, (float) 0).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y1, (float) 0).color(g, h, j, f).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }


    public static void renderRoundedQuad(Matrix4f matrix, double fromX, double fromY, double toX, double toY, double samples, boolean outline, int startCol, int endCol, boolean verticalGradient, float thickness, boolean evilmode) {
        float startA = ((startCol >> 24) & 0xFF) / 255f;
        float startR = ((startCol >> 16) & 0xFF) / 255f;
        float startG = ((startCol >> 8) & 0xFF) / 255f;
        float startB = (startCol & 0xFF) / 255f;

        float endA = ((endCol >> 24) & 0xFF) / 255f;
        float endR = ((endCol >> 16) & 0xFF) / 255f;
        float endG = ((endCol >> 8) & 0xFF) / 255f;
        float endB = (endCol & 0xFF) / 255f;

        if (startA == 0 && endA == 0) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        double radius = MathHelper.lerp(StaminaConfig.INSTANCE.getConfig().cornerRounding, 0, Math.min(Math.abs(toY - fromY), Math.abs(toX - fromX)) / 2);
        if (outline) {
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

            double[][] map = new double[][]{
                    new double[]{toX - radius, toY - radius, radius},
                    new double[]{toX - radius, fromY + radius, radius},
                    new double[]{fromX + radius, fromY + radius, radius},
                    new double[]{fromX + radius, toY - radius, radius}
            };

            for (int i = 0; i < 4; i++) {
                double[] current = map[i];
                double rad = current[2];
                double innerRad = Math.max(0, rad - thickness);

                for (double r = i * 90d; r <= 360 / 4d + i * 90d; r += 90 / samples) {
                    float rad1 = (float) Math.toRadians(r);
                    float sin = (float) Math.sin(rad1);
                    float cos = (float) Math.cos(rad1);

                    // Outer vertex
                    float x = (float) current[0] + sin * (float) innerRad;
                    float y = (float) current[1] + cos * (float) innerRad;
                    float[] color = getGradientColor(x, y, fromX, fromY, toX, toY, startR, startG, startB, endR, endG, endB, startA, endA, verticalGradient);
                    bufferBuilder.vertex(matrix, x, y, 0)
                            .color(color[0], color[1], color[2], color[3]).next();


                    x = (float) current[0] + sin * (float) rad;
                    y = (float) current[1] + cos * (float) rad;
                    color = getGradientColor(x, y, fromX, fromY, toX, toY, startR, startG, startB, endR, endG, endB, startA, endA, verticalGradient);
                    bufferBuilder.vertex(matrix, x, y, 0)
                            .color(color[0], color[1], color[2], color[3]).next();

                    // Inner vertex
                }
            }

            // Close the loop by connecting back to the first vertices
            float rad1 = (float) Math.toRadians(0);
            double rad = map[0][2];
            double innerRad = Math.max(0, rad - thickness);
            float[] color = getGradientColor((float) map[0][0] + (float) Math.sin(rad1) * (float) innerRad, (float) map[0][1] + (float) Math.cos(rad1) * (float) innerRad, fromX, fromY, toX, toY, startR, startG, startB, endR, endG, endB, startA, endA, verticalGradient);
            bufferBuilder.vertex(matrix, (float) map[0][0] + (float) Math.sin(rad1) * (float) innerRad, (float) map[0][1] + (float) Math.cos(rad1) * (float) innerRad, 0)
                    .color(color[0], color[1], color[2], color[3]).next();
            color = getGradientColor((float) map[0][0] + (float) Math.sin(rad1) * (float) rad, (float) map[0][1] + (float) Math.cos(rad1) * (float) rad, fromX, fromY, toX, toY, startR, startG, startB, endR, endG, endB, startA, endA, verticalGradient);
            bufferBuilder.vertex(matrix, (float) map[0][0] + (float) Math.sin(rad1) * (float) rad, (float) map[0][1] + (float) Math.cos(rad1) * (float) rad, 0)
                    .color(color[0], color[1], color[2], color[3]).next();
        } else {
            // Draw filled quad using TRIANGLE_FAN
            if (evilmode) {
                RenderSystem.setShader(GameRenderer::getRenderTypeEndGatewayProgram);
                RenderSystem.setShaderTexture(0, EndPortalBlockEntityRenderer.SKY_TEXTURE);
                RenderSystem.setShaderTexture(1, EndPortalBlockEntityRenderer.PORTAL_TEXTURE);
            }
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            double[][] map = new double[][]{
                    new double[]{toX - radius, toY - radius, radius},
                    new double[]{toX - radius, fromY + radius, radius},
                    new double[]{fromX + radius, fromY + radius, radius},
                    new double[]{fromX + radius, toY - radius, radius}
            };

            for (int i = 0; i < 4; i++) {
                double[] current = map[i];
                double rad = current[2];
                for (double r = i * 90d; r < 360 / 4d + i * 90d; r += 90 / samples) {
                    float rad1 = (float) Math.toRadians(r);
                    float sin = (float) (Math.sin(rad1) * rad);
                    float cos = (float) (Math.cos(rad1) * rad);
                    float x = (float) current[0] + sin;
                    float y = (float) current[1] + cos;

                    float[] color = getGradientColor(x, y, fromX, fromY, toX, toY, startR, startG, startB, endR, endG, endB, startA, endA, verticalGradient);
                    bufferBuilder.vertex(matrix, x, y, 0).color(color[0], color[1], color[2], color[3]).next();
                }
                float rad1 = (float) Math.toRadians(360 / 4d + i * 90d);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                float x = (float) current[0] + sin;
                float y = (float) current[1] + cos;

                float[] color = getGradientColor(x, y, fromX, fromY, toX, toY, startR, startG, startB, endR, endG, endB, startA, endA, verticalGradient);
                bufferBuilder.vertex(matrix, x, y, 0).color(color[0], color[1], color[2], color[3]).next();
            }
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private static float[] getGradientColor(float x, float y, double fromX, double fromY, double toX, double toY,
                                            float startR, float startG, float startB,
                                            float endR, float endG, float endB, float startA, float endA,
                                            boolean vertical) {

        float t;
        if (vertical) {
            t = (float) ((y - fromY) / (toY - fromY));
        } else {
            t = (float) ((x - fromX) / (toX - fromX));
        }
        t = MathHelper.clamp(t, 0, 1);
        //TODO: 4 way gradients!!!
        float r = startR + (endR - startR) * t;
        float g = startG + (endG - startG) * t;
        float b = startB + (endB - startB) * t;
        float a = startA + (endA - startA) * t;

        return new float[]{r, g, b, a};
    }

}
