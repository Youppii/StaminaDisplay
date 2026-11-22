package me.noobilybridge;

import me.noobilybridge.config26.StaminaSettings26;
import me.noobilybridge.config26.ModMenuIntegration26;
import me.noobilybridge.mixin26.BossOverlayAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.HashMap;
import java.util.OptionalInt;
import java.util.UUID;

public final class StaminaDisplay26 implements ClientModInitializer {
    private static final int FATIGUE_THRESHOLD = 15;
    private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("stamina-display", "stamina_bar");

    public static boolean gameActive;
    public static boolean modEnabled = true;
    private static KeyMapping toggleMod;
    private static KeyMapping toggleBars;
    private static int homeTeamColor = 0xFFFFFFFF;
    private static int awayTeamColor = 0xFFFFFFFF;
    private static int homeTeamSecondaryColor = 0xFFFFFFFF;
    private static int awayTeamSecondaryColor = 0xFFFFFFFF;
    private static float localStamina;
    private static final Map<UUID, Float> displayedStamina = new HashMap<>();
    private static final Map<UUID, Float> animation = new HashMap<>();
    @Override
    public void onInitializeClient() {
        StaminaSettings26.load();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("stamina-display", "controls"));
        toggleMod = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.staminadisplay.toggleMod",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, category));
        toggleBars = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.staminadisplay.toggleBars",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, category));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommands.literal("staminadisplay").executes(context -> {
                    Minecraft client = Minecraft.getInstance();
                    client.setScreenAndShow(ModMenuIntegration26.open(null));
                    return 1;
                })));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if ("dragonfruit".equals(message.getString())) {
                Minecraft client = Minecraft.getInstance();
                if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("unlerp") && client.player != null) {
                    client.player.connection.sendCommand("pineapple");
                }
                return false;
            }
            return true;
        });
        HudElementRegistry.replaceElement(VanillaHudElements.FOOD_BAR, vanillaFoodBar -> (graphics, tickCounter) -> {
            if (!StaminaSettings26.customHungerBar) {
                vanillaFoodBar.extractRenderState(graphics, tickCounter);
            }
        });
        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, HUD_ID, StaminaDisplay26::renderStaminaBar);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleMod.consumeClick()) modEnabled = !modEnabled;
            while (toggleBars.consumeClick()) {
                StaminaSettings26.renderBar = !StaminaSettings26.renderBar;
                StaminaSettings26.save();
            }
            if (client.player == null || client.getConnection() == null) {
                gameActive = false;
                displayedStamina.clear();
                animation.clear();
                localStamina = 0;
                return;
            }
            Map<UUID, LerpingBossEvent> bossEvents = ((BossOverlayAccessor) client.gui.hud.getBossOverlay()).staminaDisplay$getEvents();
            gameActive = bossEvents.values().stream().anyMatch(StaminaDisplay26::isStaminaMatch);
            for (LerpingBossEvent event : bossEvents.values()) {
                if (!isStaminaMatch(event)) continue;
                try {
                    homeTeamColor = event.getName().getSiblings().get(3).getSiblings().get(0).getStyle().getColor().getValue() | 0xFF000000;
                    awayTeamColor = event.getName().getSiblings().get(1).getSiblings().get(0).getStyle().getColor().getValue() | 0xFF000000;
                    homeTeamSecondaryColor = event.getName().getSiblings().get(3).getSiblings().get(1).getStyle().getColor().getValue() | 0xFF000000;
                    awayTeamSecondaryColor = event.getName().getSiblings().get(1).getSiblings().get(1).getStyle().getColor().getValue() | 0xFF000000;
                } catch (IndexOutOfBoundsException | NullPointerException ignored) {
                }
                break;
            }
            float step = (float) (1 - Math.exp(-StaminaSettings26.animationSpeed / 20.0F));
            PlayerInfo localInfo = client.getConnection().getPlayerInfo(client.player.getUUID());
            int localTarget = client.player.getVehicle() == null
                    ? client.player.getFoodData().getFoodLevel()
                    : getStaminaFromName(localInfo == null ? null : localInfo.getTabListDisplayName())
                            .orElse(client.player.getFoodData().getFoodLevel());
            localStamina += (localTarget - localStamina) * step;
            for (PlayerInfo info : client.getConnection().getOnlinePlayers()) {
                UUID id = info.getProfile().id();
                int stamina = getStaminaFromName(info.getTabListDisplayName()).orElse(0);
                displayedStamina.compute(id, (ignored, current) -> current == null ? (float) stamina : current + (stamina - current) * step);
            boolean active = modEnabled && (gameActive && (!StaminaSettings26.teammatesOnly || sameTeam(client, id)));
                animation.compute(id, (ignored, current) -> current == null ? 0 : current + ((active ? 1 : 0) - current) * step);
            }
        });
    }

    private static boolean isStaminaMatch(LerpingBossEvent event) {
        try {
            Component name = event.getName();
            return name.getSiblings().get(3).getSiblings().get(0).getStyle().getColor() != null
                    && name.getSiblings().get(1).getSiblings().get(0).getStyle().getColor() != null;
        } catch (IndexOutOfBoundsException | NullPointerException ignored) {
            return false;
        }
    }

    public static void renderStaminaBar(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null) {
            return;
        }
        if (!StaminaSettings26.customHungerBar) {
            return;
        }

        int stamina = getStamina(player);
        int width = Math.round(StaminaSettings26.hungerBarWidth);
        int height = Math.round(StaminaSettings26.hungerBarHeight);
        int padding = Math.round(StaminaSettings26.paddingAmount);
        int x = graphics.guiWidth() / 2 + 10 + (81 - width) / 2;
        int y = graphics.guiHeight() - 39 + (9 - height) / 2 - Math.round(StaminaSettings26.hungerBarVerticalOffset);
        int backgroundStart = StaminaSettings26.backgroundDirection
                ? StaminaSettings26.outlineColor : StaminaSettings26.secondaryOutlineColor;
        int backgroundEnd = StaminaSettings26.backgroundDirection
                ? StaminaSettings26.secondaryOutlineColor : StaminaSettings26.outlineColor;
        graphics.fillGradient(x - padding - 1, y - padding - 1, x + width + padding + 1,
                y + height + padding + 1, backgroundStart, backgroundEnd);
        graphics.fill(x, y, x + width, y + height, 0x80000000);
        float progress = Math.clamp((stamina - StaminaSettings26.minStamina) / (20.0F - StaminaSettings26.minStamina), 0, 1);
        int filledWidth = Math.round(width * progress);
        if (filledWidth > 0) {
            int colorStart = StaminaSettings26.hungerBarGradientDirection
                    ? StaminaSettings26.hungerBarMainColor : StaminaSettings26.hungerBarSecondaryColor;
            int colorEnd = StaminaSettings26.hungerBarGradientDirection
                    ? StaminaSettings26.hungerBarSecondaryColor : StaminaSettings26.hungerBarMainColor;
            graphics.fillGradient(x, y, x + filledWidth, y + height, colorStart, colorEnd);
        }
        if (StaminaSettings26.lowStaminaCue && stamina <= FATIGUE_THRESHOLD) {
            graphics.fill(x - 1, y - 1, x + width + 1, y, 0xFFFF3030);
            graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, 0xFFFF3030);
        }
        if (StaminaSettings26.showNumberInBar) {
            String number = Integer.toString(stamina);
            float scale = StaminaSettings26.barNumberScale;
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + width / 2.0F, y + height / 2.0F);
            graphics.pose().scale(scale, scale);
            graphics.text(client.font, number, -client.font.width(number) / 2,
                    -client.font.lineHeight / 2, StaminaSettings26.barNumberColor, true);
            graphics.pose().popMatrix();
        }
    }

    public static int getStamina(Player player) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && player.getUUID().equals(client.player.getUUID())) {
            return Math.round(localStamina);
        }
        if (client.player != null && !player.getUUID().equals(client.player.getUUID())) {
            PlayerInfo info = client.getConnection() == null ? null : client.getConnection().getPlayerInfo(player.getUUID());
            return info == null ? player.getFoodData().getFoodLevel()
                    : Math.round(displayedStamina.getOrDefault(player.getUUID(), (float) player.getFoodData().getFoodLevel()));
        }
        if (player.getVehicle() == null) {
            return player.getFoodData().getFoodLevel();
        }
        if (client.getConnection() == null) {
            return player.getFoodData().getFoodLevel();
        }
        PlayerInfo info = client.getConnection().getPlayerInfo(player.getUUID());
        return info == null ? player.getFoodData().getFoodLevel() : getStaminaFromName(info.getTabListDisplayName()).orElse(player.getFoodData().getFoodLevel());
    }

    public static float getAnimation(UUID player) {
        return animation.getOrDefault(player, 0F);
    }

    public static int getTeamColor(Minecraft client, UUID player) {
        if (!StaminaSettings26.useTeamForMain) return StaminaSettings26.mainColor;
        if (client.getConnection() == null) return 0xFFFFFFFF;
        PlayerInfo info = client.getConnection().getPlayerInfo(player);
        if (info == null || info.getTabListDisplayName() == null) return 0xFFFFFFFF;
        for (Component sibling : info.getTabListDisplayName().getSiblings()) {
            if (sibling.getStyle().getColor() == null) continue;
            String color = sibling.getStyle().getColor().serialize();
            if ("blue".equals(color)) return StaminaSettings26.overrideHomeColor
                    ? StaminaSettings26.homeTeamColorOverride : homeTeamColor;
            if ("yellow".equals(color)) return StaminaSettings26.overrideAwayColor
                    ? StaminaSettings26.awayTeamColorOverride : awayTeamColor;
        }
        return StaminaSettings26.mainColor;
    }

    public static boolean isOnStaminaTeam(Minecraft client, UUID player) {
        return client.getConnection() != null && teamColor(client.getConnection().getPlayerInfo(player)) != null;
    }

    public static int getSecondaryTeamColor(Minecraft client, UUID player) {
        if (!StaminaSettings26.useTeamForMain) return StaminaSettings26.secondaryMainColor;
        String team = teamColor(client.getConnection() == null ? null : client.getConnection().getPlayerInfo(player));
        if ("blue".equals(team)) return StaminaSettings26.overrideHomeColor
                ? StaminaSettings26.secondaryHomeTeamColorOverride : homeTeamSecondaryColor;
        if ("yellow".equals(team)) return StaminaSettings26.overrideAwayColor
                ? StaminaSettings26.secondaryAwayTeamColorOverride : awayTeamSecondaryColor;
        return StaminaSettings26.secondaryMainColor;
    }

    public static boolean sameTeam(Minecraft client, UUID target) {
        if (client.player == null || client.getConnection() == null) return false;
        PlayerInfo local = client.getConnection().getPlayerInfo(client.player.getUUID());
        PlayerInfo other = client.getConnection().getPlayerInfo(target);
        String localTeam = teamColor(local);
        return localTeam != null && localTeam.equals(teamColor(other));
    }

    private static String teamColor(PlayerInfo info) {
        if (info == null || info.getTabListDisplayName() == null) return null;
        return info.getTabListDisplayName().getSiblings().stream()
                .map(sibling -> sibling.getStyle().getColor())
                .filter(java.util.Objects::nonNull)
                .map(color -> color.serialize())
                .filter(color -> color.equals("blue") || color.equals("yellow"))
                .findFirst().orElse(null);
    }

    public static OptionalInt getStaminaFromName(Component name) {
        if (name == null) return OptionalInt.empty();
        String value = name.getString();
        int start = value.lastIndexOf('[');
        int end = value.lastIndexOf(']');
        if (start < 0 || end <= start) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(value.substring(start + 1, end).trim()));
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    private static int lerpColor(int start, int end, float amount) {
        int alpha = Math.round(((start >>> 24) & 255) * (1 - amount) + ((end >>> 24) & 255) * amount);
        int red = Math.round(((start >>> 16) & 255) * (1 - amount) + ((end >>> 16) & 255) * amount);
        int green = Math.round(((start >>> 8) & 255) * (1 - amount) + ((end >>> 8) & 255) * amount);
        int blue = Math.round((start & 255) * (1 - amount) + (end & 255) * amount);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }
}
