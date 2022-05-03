package mods.thecomputerizer.reputation.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.reputation.Reputation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class RenderIcon {

    public static List<RenderIcon> renderable = new ArrayList<>();

    private ResourceLocation ICON_LOCATION = null;
    private int fadeCount = 1000;
    private boolean activated = false;
    private int timer = 0;
    private int startDelayCount = 0;

    public static void setIcon(ResourceLocation icon) {
        RenderIcon toRender = new RenderIcon();
        toRender.ICON_LOCATION = icon;
        toRender.activated = true;
        renderable.add(toRender);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (!renderable.isEmpty()) {
            renderable = renderable.stream().filter(icon -> icon.ICON_LOCATION != null).collect(Collectors.toList());
            for (RenderIcon icon : renderable) {
                if (icon.activated) {
                    icon.timer++;
                    icon.startDelayCount++;
                    if (icon.startDelayCount > 0) {
                        if (icon.fadeCount > 1) {
                            icon.fadeCount -= 7;
                            if (icon.fadeCount < 1) icon.fadeCount = 1;
                        }
                    }
                    if (icon.timer > 100) icon.activated = false;
                } else {
                    if (icon.fadeCount < 1000) {
                        icon.fadeCount += 7;
                        if (icon.fadeCount > 1000) icon.ICON_LOCATION = null;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderIcons(RenderGameOverlayEvent.Post e) {
        if (!renderable.isEmpty()) {
            RenderIcon toRemove = null;
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (e.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                renderable = renderable.stream().filter(icon -> icon.ICON_LOCATION != null).collect(Collectors.toList());
                for (RenderIcon icon : renderable) {
                    if (player != null && icon.ICON_LOCATION != null) {
                        int x = mc.getWindow().getGuiScaledWidth();
                        int y = mc.getWindow().getGuiScaledHeight();
                        if (icon.fadeCount != 1000) {
                            e.getMatrixStack().pushPose();
                            float opacity = (int) (17f - (icon.fadeCount / 80f));
                            opacity = (opacity * 1.15f) / 15f;

                            int sizeX = 50;
                            int sizeY = 50;

                            float scaleY = 1f;
                            float scaleX = 1f;
                            e.getMatrixStack().scale(scaleX, scaleY, 1f);

                            float posY = y-((float)sizeY*sizeY/2f);
                            float posX = (x/2f) - (float)sizeX*scaleX-100;

                            Reputation.logInfo("Rendering Icon: Pos X: "+posX);

                            RenderSystem.setShaderColor(1F, 1F, 1F, Math.max(0, Math.min(0.95f, opacity)));
                            RenderSystem.setShaderTexture(0, icon.ICON_LOCATION);
                            GuiComponent.blit(e.getMatrixStack(), (int) posX, (int) posY, 0F, 0F, sizeX, sizeY, sizeX, sizeY);

                            e.getMatrixStack().popPose();
                        }
                    } else toRemove = icon;
                }
                renderable.remove(toRemove);
            }
        }
    }
}
