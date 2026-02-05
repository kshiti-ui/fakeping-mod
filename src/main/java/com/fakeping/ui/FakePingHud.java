package com.fakeping.ui;

import com.fakeping.FakePingMod;
import com.fakeping.config.FakePingConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * HUD overlay that displays the current fake ping status.
 * 
 * Shows a small indicator in the top-right corner of the screen when
 * fake ping is enabled, displaying the current delay and queue size.
 */
public class FakePingHud implements HudRenderCallback {
    
    private static final int PADDING = 4;
    private static final int BACKGROUND_COLOR = 0x80000000; // Semi-transparent black
    private static final int TEXT_COLOR = 0xFFFFFF; // White
    private static final int ENABLED_COLOR = 0x55FF55; // Green
    
    public static void register() {
        HudRenderCallback.EVENT.register(new FakePingHud());
    }
    
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        FakePingConfig config = FakePingMod.getConfig();
        
        // Don't show HUD if disabled in config or not in singleplayer
        if (!config.shouldShowHud()) {
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !client.isInSingleplayer()) {
            return;
        }
        
        // Only show when fake ping is enabled
        if (!config.isEnabled()) {
            return;
        }
        
        int windowWidth = client.getWindow().getScaledWidth();
        
        // Build the display text
        int delay = config.getBaseDelayMs();
        int jitter = config.getJitterMs();
        int queueSize = FakePingMod.getPacketDelayManager().getQueueSize();
        
        String text = String.format("Fake Ping: %dms", delay);
        if (jitter > 0) {
            text += String.format(" (±%d)", jitter);
        }
        if (queueSize > 0) {
            text += String.format(" [%d]", queueSize);
        }
        
        int textWidth = client.textRenderer.getWidth(text);
        int textHeight = client.textRenderer.fontHeight;
        
        // Position in top-right corner
        int x = windowWidth - textWidth - PADDING * 2;
        int y = PADDING;
        
        // Draw background
        drawContext.fill(
            x - PADDING, 
            y - PADDING, 
            x + textWidth + PADDING, 
            y + textHeight + PADDING, 
            BACKGROUND_COLOR
        );
        
        // Draw text
        drawContext.drawText(
            client.textRenderer,
            text,
            x,
            y,
            ENABLED_COLOR,
            true // shadow
        );
    }
}
