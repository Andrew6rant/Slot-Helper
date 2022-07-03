package io.github.andrew6rant.slot_helper.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.andrew6rant.slot_helper.SlotHelperClient;
import io.github.andrew6rant.slot_helper.data.ResourceLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {
    @Shadow
    protected int x;

    @Shadow
    protected int y;

    protected Slot focusedSlot;

    @Shadow
    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        pointX -= this.x;
        pointY -= this.y;
        return pointX >= (double)(x - 1) && pointX < (double)(x + width + 1) && pointY >= (double)(y - 1) && pointY < (double)(y + height + 1);
    }

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    Map<Identifier, Resource> resources = ResourceLoader.getResources();
    JsonObject jsonObject = ResourceLoader.getJSON();

    @Redirect(method= "render", at = @At(value = "INVOKE", target="Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlight(Lnet/minecraft/client/util/math/MatrixStack;III)V"))
    private void customHighlighter(MatrixStack matrices, int startX, int startY, int z) {
        int[][] posTest = null;
        int color = -2130706433;
        outerloop:
        for (Identifier path : resources.keySet()) {
            String pathString = path.toString();
            String newSentence = pathString.substring(pathString.lastIndexOf('/') + 1);
            if (this.title.equals(Text.translatable(newSentence.substring(0,newSentence.length()-5)))) {
                try {
                    JsonObject assetData = jsonObject.getAsJsonObject("slot_helper");
                    for (Map.Entry<String, JsonElement> entry : assetData.entrySet()) {
                        if(focusedSlot.id == Integer.parseInt(entry.getKey())) {
                            JsonObject slotData = entry.getValue().getAsJsonObject();
                            JsonArray coordData = slotData.getAsJsonArray("coords");
                            posTest = new int[coordData.size()][2];
                            for (int i = 0; i < coordData.size(); i++) {
                                JsonArray coord = coordData.get(i).getAsJsonArray();
                                posTest[i][0] = coord.get(0).getAsInt();
                                posTest[i][1] = coord.get(1).getAsInt();
                            }
                            color = slotData.get("color").getAsInt();
                            break outerloop;
                        }
                    }
                } catch (Exception e) {
                    SlotHelperClient.warn("Caught exception: %s", e.toString());
                }
            }
        }
        if (posTest==null) { // if no match is found, use default shape
            posTest = new int[][] {
                    {0, 0},
                    {0, 16},
                    {16, 16},
                    {16, 0}
            };
        }
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        for (int[] pos : posTest) {
            bufferBuilder.vertex(matrix, (float)(startX + pos[0]), (float)(startY + pos[1]), (float)z).color(color).next();
            //System.out.println("X: " + (float)(startX + pos[0]) + " Y: " + (float)(startY + pos[1]));
        }
        //System.out.println("------------");

        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    @Inject(method="isPointOverSlot", at = @At(value = "HEAD"), cancellable = true)
    private void customSlotHitbox(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
        if (this.title.equals(Text.translatable("container.blast_furnace"))) {
            if(slot.id == 1) {
                cir.setReturnValue(this.isPointWithinBounds(slot.x - 10, slot.y, 36, 16, pointX, pointY));
            } else if (slot.id == 2) {
                cir.setReturnValue(this.isPointWithinBounds(slot.x - 4, slot.y - 4, 24, 24, pointX, pointY));
            }
        }

    }
}
