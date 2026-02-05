package com.fakeping.network;

import com.fakeping.FakePingMod;
import com.fakeping.config.FakePingConfig;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Core packet delay management system.
 * 
 * This class maintains a thread-safe queue of packets that need to be delayed.
 * Each packet is stored with a timestamp indicating when it should be sent.
 * 
 * The tick() method is called every client tick to check if any packets are
 * ready to be sent, maintaining temporal ordering while simulating network latency.
 */
public class PacketDelayManager {
    private final FakePingConfig config;
    private final ConcurrentLinkedQueue<DelayedPacket> packetQueue;
    private final Random random;
    
    public PacketDelayManager(FakePingConfig config) {
        this.config = config;
        this.packetQueue = new ConcurrentLinkedQueue<>();
        this.random = new Random();
    }
    
    /**
     * Queue a packet for delayed sending.
     * 
     * @param packet The packet to delay
     * @param sendAction The action to execute when the delay expires
     */
    public void queuePacket(Packet<?> packet, Runnable sendAction) {
        if (!config.isEnabled() || !shouldDelayPacket(packet)) {
            // If fake ping is disabled or this packet type shouldn't be delayed,
            // send it immediately
            sendAction.run();
            return;
        }
        
        // Calculate delay with jitter
        int delay = calculateDelay();
        long sendTime = System.currentTimeMillis() + delay;
        
        packetQueue.offer(new DelayedPacket(packet, sendAction, sendTime));
        
        FakePingMod.LOGGER.debug("Queued packet {} with delay {}ms", 
                packet.getClass().getSimpleName(), delay);
    }
    
    /**
     * Process the packet queue and send any packets whose time has come.
     * Called every client tick.
     */
    public void tick() {
        long currentTime = System.currentTimeMillis();
        
        // Process all packets that are ready to be sent
        while (!packetQueue.isEmpty()) {
            DelayedPacket delayedPacket = packetQueue.peek();
            
            if (delayedPacket != null && delayedPacket.sendTime <= currentTime) {
                packetQueue.poll();
                
                try {
                    delayedPacket.sendAction.run();
                    FakePingMod.LOGGER.debug("Sent delayed packet: {}", 
                            delayedPacket.packet.getClass().getSimpleName());
                } catch (Exception e) {
                    FakePingMod.LOGGER.error("Error sending delayed packet", e);
                }
            } else {
                // Packets are ordered by time, so if this one isn't ready, neither are the rest
                break;
            }
        }
    }
    
    /**
     * Determine if a packet should be delayed based on its type and configuration.
     */
    private boolean shouldDelayPacket(Packet<?> packet) {
        // Attack packets
        if (packet instanceof PlayerInteractEntityC2SPacket) {
            return config.shouldDelayAttacks();
        }
        
        // Movement packets
        if (packet instanceof PlayerMoveC2SPacket) {
            return config.shouldDelayMovement();
        }
        
        // Hand swing and interaction packets
        if (packet instanceof HandSwingC2SPacket || packet instanceof PlayerInteractBlockC2SPacket) {
            return config.shouldDelayInteractions();
        }
        
        // Block breaking packets
        if (packet instanceof PlayerActionC2SPacket) {
            return config.shouldDelayBlockBreaking();
        }
        
        // Item use packets
        if (packet instanceof PlayerInteractItemC2SPacket) {
            return config.shouldDelayItemUse();
        }
        
        // Don't delay other packet types by default
        return false;
    }
    
    /**
     * Calculate the delay for a packet, including base delay and random jitter.
     */
    private int calculateDelay() {
        int baseDelay = config.getBaseDelayMs();
        int jitter = config.getJitterMs();
        
        if (jitter > 0) {
            // Add random jitter: ±jitter milliseconds
            int randomJitter = random.nextInt(jitter * 2 + 1) - jitter;
            return Math.max(0, baseDelay + randomJitter);
        }
        
        return baseDelay;
    }
    
    /**
     * Get the current queue size (for debugging/monitoring).
     */
    public int getQueueSize() {
        return packetQueue.size();
    }
    
    /**
     * Clear all queued packets (useful when disabling fake ping).
     */
    public void clearQueue() {
        packetQueue.clear();
    }
    
    /**
     * Internal class to store a packet with its scheduled send time.
     */
    private static class DelayedPacket {
        final Packet<?> packet;
        final Runnable sendAction;
        final long sendTime;
        
        DelayedPacket(Packet<?> packet, Runnable sendAction, long sendTime) {
            this.packet = packet;
            this.sendAction = sendAction;
            this.sendTime = sendTime;
        }
    }
}
