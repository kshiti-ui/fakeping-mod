package com.fakeping;

import com.fakeping.command.PingCommand;
import com.fakeping.config.FakePingConfig;
import com.fakeping.network.PacketDelayManager;
import com.fakeping.ui.FakePingHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FakePing - A Fabric mod that simulates network latency in singleplayer for PvP practice.
 * 
 * This mod intercepts outgoing packets and delays them to simulate network lag,
 * allowing players to practice PvP mechanics under realistic network conditions
 * without needing a real server.
 * 
 * Architecture:
 * - FakePingMod: Main entry point, handles initialization
 * - PacketDelayManager: Core system for queueing and delaying packets
 * - ClientConnectionMixin: Intercepts packet sending
 * - PingCommand: User commands for controlling fake ping
 * - FakePingConfig: Configuration and settings management
 * - FakePingHud: Visual overlay showing current status
 */
public class FakePingMod implements ClientModInitializer {
    public static final String MOD_ID = "fakeping";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static FakePingConfig config;
    private static PacketDelayManager packetDelayManager;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing FakePing mod...");
        
        // Initialize configuration
        config = new FakePingConfig();
        config.load();
        
        // Initialize packet delay manager
        packetDelayManager = new PacketDelayManager(config);
        
        // Register commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            PingCommand.register(dispatcher);
        });
        
        // Register tick event for processing delayed packets
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                packetDelayManager.tick();
            }
        });
        
        // Register HUD overlay
        FakePingHud.register();
        
        LOGGER.info("FakePing mod initialized successfully!");
    }
    
    public static FakePingConfig getConfig() {
        return config;
    }
    
    public static PacketDelayManager getPacketDelayManager() {
        return packetDelayManager;
    }
}
