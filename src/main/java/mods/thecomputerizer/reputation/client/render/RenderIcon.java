package mods.thecomputerizer.reputation.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mods.thecomputerizer.reputation.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class RenderIcon {

    private static final List<RenderIcon> RENDERABLES = new ArrayList<>();

    private final ResourceLocation iconRes;
    private final ResourceLocation factionRes;
    private boolean activated;
    private int fadeCount;
    private int timer;
    private int startDelayCount;

    private RenderIcon(ResourceLocation iconRes, ResourceLocation factionRes) {
        this.iconRes = iconRes;
        this.factionRes = factionRes;
        this.activated = true;
        this.fadeCount = 1000;
    }

    public static void setIcon(ResourceLocation iconRes, ResourceLocation factionRes) {
        RenderIcon toRender = new RenderIcon(iconRes,factionRes);
        if(Objects.nonNull(toRender.iconRes)) RENDERABLES.add(toRender);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(!RENDERABLES.isEmpty()) {
            RENDERABLES.removeIf(icon -> {
                if(icon.activated) {
                    icon.timer++;
                    icon.startDelayCount++;
                    if(icon.startDelayCount>0) {
                        if(icon.fadeCount>1) {
                            icon.fadeCount-=70;
                            if(icon.fadeCount<1) icon.fadeCount = 1;
                        }
                    }
                    if(icon.timer>100) icon.activated = false;
                } else {
                    if(icon.fadeCount<1000) {
                        icon.fadeCount+=70;
                        return icon.fadeCount>1000;
                    }
                }
                return false;
            });
        }
    }

    @SubscribeEvent
    public static void renderIcons(RenderGameOverlayEvent.Post e) {
        if(!RENDERABLES.isEmpty()) {
            if(e.getType()==RenderGameOverlayEvent.ElementType.ALL) {
                PoseStack matrix = e.getMatrixStack();
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;
                RENDERABLES.removeIf(icon -> {
                    if (Objects.nonNull(player)) return true;
                    int x = mc.getWindow().getGuiScaledWidth();
                    int y = mc.getWindow().getGuiScaledHeight();
                    if(icon.fadeCount != 1000 && Objects.isNull(mc.screen)) {
                        float opacity = (int) (17f - (icon.fadeCount / 80f));
                        opacity = (opacity * 1.15f) / 15f;
                        int sizeX = 20;
                        int sizeY = 20;
                        float scaleY = 0.36f;
                        float scaleX = 0.36f;
                        float posY = ((y / scaleY) / 2f) - ((float) sizeY * scaleY / 2f);
                        float posX = (x / scaleX) - (float) sizeX * scaleX - (25 / scaleX);
                        matrix.pushPose();
                        matrix.scale(scaleX, scaleY, 1f);
                        RenderSystem.setShaderColor(1f, 1f, 1f, Math.max(0, Math.min(0.95f, opacity)));
                        RenderSystem.setShaderTexture(0, icon.iconRes);
                        GuiComponent.blit(matrix, (int) posX, (int) posY, 0f, 0f, sizeX, sizeY, sizeX, sizeY);
                        matrix.popPose();
                        if(Objects.nonNull(icon.factionRes)) {
                            matrix.pushPose();
                            scaleY = 0.6f;
                            scaleX = 0.6f;
                            posY = ((y / scaleY) / 2f) - ((float) sizeY * scaleY / 2f);
                            posX = (x / scaleX) - (float) sizeX * scaleX - 16;
                            matrix.scale(scaleX, scaleY, 1f);
                            RenderSystem.setShaderColor(1f, 1f, 1f, Math.max(0, Math.min(0.95f, opacity)));
                            RenderSystem.setShaderTexture(0, icon.factionRes);
                            GuiComponent.blit(matrix, (int) posX, (int) posY, 0f, 0f, sizeX, sizeY, sizeX, sizeY);
                            matrix.popPose();
                        }
                    }
                    return false;
                });
            }
        }
    }
}