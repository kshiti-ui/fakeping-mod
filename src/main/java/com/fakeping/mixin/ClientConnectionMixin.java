package com.fakeping.mixin;

import com.fakeping.FakePingMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketCallbacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin to intercept outgoing packets from the client to the server.
 * 
 * This mixin hooks into the ClientConnection.send() method for Minecraft 1.21+.
 * In 1.21+, the send method signature changed to use PacketCallbacks instead of
 * GenericFutureListener.
 * 
 * IMPORTANT: This only affects client → server packets. Server → client packets
 * are not touched, as delaying those would cause visual desync issues.
 * 
 * The mixin only activates when connected to an integrated server (singleplayer),
 * ensuring it doesn't interfere with real multiplayer servers.
 */
@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    
    // ThreadLocal flag to prevent recursion when we send delayed packets
    private static final ThreadLocal<Boolean> SENDING_DELAYED_PACKET = ThreadLocal.withInitial(() -> false);
    
    /**
     * Intercept the packet send method to add artificial delay.
     * 
     * For Minecraft 1.21+, the method signature is:
     * send(Packet<?> packet, @Nullable PacketCallbacks callbacks)
     * 
     * @param packet The packet being sent
     * @param callbacks Optional callbacks for when the packet is actually sent (can be null)
     * @param ci Callback info for controlling the injection
     */
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", 
            at = @At("HEAD"), 
            cancellable = true)
    private void onSendPacket(Packet<?> packet, 
                             @Nullable PacketCallbacks callbacks,
                             CallbackInfo ci) {
        
        // Don't intercept if we're already sending a delayed packet (prevents recursion)
        if (SENDING_DELAYED_PACKET.get()) {
            return;
        }
        
        // Only apply fake ping in singleplayer (integrated server)
        if (!isIntegratedServer()) {
            return;
        }
        
        // Only delay if fake ping is enabled
        if (!FakePingMod.getConfig().isEnabled()) {
            return;
        }
        
        // Cancel the immediate send
        ci.cancel();
        
        // Queue the packet for delayed sending
        ClientConnection self = (ClientConnection) (Object) this;
        FakePingMod.getPacketDelayManager().queuePacket(packet, () -> {
            // Set flag to prevent recursion
            SENDING_DELAYED_PACKET.set(true);
            try {
                // Execute the original send when the delay expires
                self.send(packet, callbacks);
            } finally {
                // Always clear the flag
                SENDING_DELAYED_PACKET.set(false);
            }
        });
    }
    
    /**
     * Helper method to check if we're connected to an integrated server.
     * This ensures fake ping only works in singleplayer and doesn't affect
     * real multiplayer servers.
     */
    private boolean isIntegratedServer() {
        // In Fabric, we can check if the MinecraftClient has an integrated server
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        return client != null && client.getServer() != null && client.isInSingleplayer();
    }
}
