package io.github.andrew6rant.slot_helper.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {
    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Nullable protected Slot focusedSlot;

    @Shadow
    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        pointX -= this.x;
        pointY -= this.y;
        return pointX >= (double)(x - 1) && pointX < (double)(x + width + 1) && pointY >= (double)(y - 1) && pointY < (double)(y + height + 1);
    }

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Redirect(method= "render", at = @At(value = "INVOKE", target="Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlight(Lnet/minecraft/client/util/math/MatrixStack;III)V"))
    private void customHighlighter(MatrixStack matrices, int startX, int startY, int z) {
        int endX = startX + 16;
        int endY = startY + 16;
        int color = -2130706433;

        if (this.title.equals(Text.translatable("container.blast_furnace"))) {
            if(focusedSlot != null && focusedSlot.id == 1) {
                startX -= 10;
                endX += 10;
                color = -2141560118;
                //System.out.println(toHexString(-2130706433)); // 80ffffff // rgba(255, 255, 255, 128)
            } else if (focusedSlot != null && focusedSlot.id == 2) {
                startX -= 4;
                endX += 4;
                startY -= 4;
                endY += 4;
                color = -597290227;
            }
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
        bufferBuilder.vertex(matrix, (float)endX, (float)startY, (float)z).color(color).next();
        bufferBuilder.vertex(matrix, (float)startX, (float)startY, (float)z).color(color).next();
        bufferBuilder.vertex(matrix, (float)startX, (float)endY, (float)z).color(color).next();
        bufferBuilder.vertex(matrix, (float)endX, (float)endY, (float)z).color(color).next();
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
