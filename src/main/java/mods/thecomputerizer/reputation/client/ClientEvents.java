package mods.thecomputerizer.reputation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import mods.thecomputerizer.reputation.ReputationRef;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.*;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX;
import static com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS;
import static mods.thecomputerizer.reputation.client.ClientConfigHandler.DEBUG;
import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

@EventBusSubscriber(modid=MODID,value=CLIENT)
public class ClientEvents {

    public static final int RENDER_DISTANCE = 64;
    public static final ResourceLocation BAD_REPUTATION =
            ReputationRef.res("textures/icons/reputation_decrease.png");
    public static final ResourceLocation GOOD_REPUTATION =
            ReputationRef.res("textures/icons/reputation_increase.png");
    public static final ResourceLocation FLEE = ReputationRef.res("textures/icons/flee.png");
    public static final Map<ResourceLocation,Faction> CLIENT_FACTIONS = new HashMap<>();
    public static final Map<Faction,Integer> CLIENT_FACTIONS_REPUTATION = new HashMap<>();
    public static final List<UUID> FLEEING_MOBS = new ArrayList<>();
    private static int TICK_COUNTER = 0;

    @SubscribeEvent
    public static void onDisconnect(LoggedOutEvent event) {
        CLIENT_FACTIONS.clear();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        TICK_COUNTER++;
        if(TICK_COUNTER==40) TICK_COUNTER =0;
    }

    @SubscribeEvent(priority=LOWEST)
    public static void renderName(RenderNameplateEvent event) {
        Player player = Minecraft.getInstance().player;
        if(Objects.nonNull(player) && event.getEntity() instanceof LivingEntity living) {
            if(living.distanceTo(player)<=RENDER_DISTANCE) {
                PoseStack matrix = event.getPoseStack();
                EntityRenderer<?> renderer = event.getEntityRenderer();
                if(!FLEEING_MOBS.contains(living.getUUID())) {
                    if(ClientConfigHandler.DEBUG.get()) {
                        int offset = 0;
                        ResourceLocation icon = null;
                        for(Faction f : ReputationHandler.getEntityFactions(living)) {
                            if(ReputationHandler.isGoodReputation(player,f)) icon = GOOD_REPUTATION;
                            else if(ReputationHandler.isBadReputation(player,f)) icon = BAD_REPUTATION;
                            double xTranslate = (living.getBbWidth()/2f)-(living.getBbWidth()*0.0125f*1.1f*offset);
                            render(matrix,renderer,living,player,
                                    ReputationRef.res("textures/icons/faction_"+f.getID().getPath()+".png"),
                                   xTranslate,0.25d,0.22f);
                            if(Objects.nonNull(icon)) render(matrix,renderer,living,player,icon,xTranslate,
                                                             0.28d,0.15f);
                            offset++;
                        }
                    }
                    ResourceLocation chatIcon = ClientTrackers.getChatIcon(living);
                    if(Objects.nonNull(chatIcon)) render(matrix,renderer,living,player,chatIcon,
                                                         0d,1d,1.5f);
                } else if(TICK_COUNTER <20) render(matrix,renderer,living,player,FLEE,
                                                   0d,1d,1.5f);
            }
        }
    }

    private static void render(PoseStack matrix, EntityRenderer<?> renderer, LivingEntity living, Player player,
            ResourceLocation icon, double xTranslate, double yTranslate, float scale) {
        float opacity = Mth.clamp(((float)RENDER_DISTANCE-living.distanceTo(player))/(float)RENDER_DISTANCE,
                                  0f,1f);
        matrix.pushPose();
        matrix.translate(xTranslate,living.getBbHeight()+yTranslate,0d);
        matrix.mulPose(renderer.entityRenderDispatcher.cameraOrientation());
        matrix.scale(-0.025f*scale,-0.025f*scale,0.025f*scale);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f,1f,1f,opacity);
        matrix.pushPose();
        matrix.scale(1f, 1f, 1f);
        Matrix4f matrix4f = matrix.last().pose();
        Minecraft.getInstance().getTextureManager().getTexture(icon).setFilter(false, false);
        RenderSystem.setShaderTexture(0, icon);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(QUADS,POSITION_TEX);
        buffer.vertex(matrix4f,-8,16,0).uv(0,1).endVertex();
        buffer.vertex(matrix4f,8,16,0).uv(1,1).endVertex();
        buffer.vertex(matrix4f,8,0,0).uv(1,0).endVertex();
        buffer.vertex(matrix4f,-8,0,0).uv(0,0).endVertex();
        Tesselator.getInstance().end();
        matrix.popPose();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        matrix.popPose();
    }

    /**
     * Prints the player's reputation for each faction to the screen
     */
    @SubscribeEvent
    public static void debugInfo(Text event) {
        if(DEBUG.get() && Objects.nonNull(Minecraft.getInstance().player))
            for(Faction f : CLIENT_FACTIONS.values())
                event.getLeft().add("Reputation for the "+f.getID()+" faction: "+CLIENT_FACTIONS_REPUTATION.get(f));
    }
}