package com.fakeping.command;

import com.fakeping.FakePingMod;
import com.fakeping.config.FakePingConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

/**
 * Command handler for the /ping command.
 * 
 * Provides the following subcommands:
 * - /ping set <ms> - Set fake ping delay
 * - /ping off - Disable fake ping
 * - /ping on - Enable fake ping
 * - /ping status - Show current settings
 * - /ping jitter <ms> - Set ping jitter/variance
 * - /ping toggle attacks - Toggle attack packet delay
 * - /ping toggle movement - Toggle movement packet delay
 * - /ping toggle interactions - Toggle interaction packet delay
 */
public class PingCommand {
    
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("ping")
            .then(literal("set")
                .then(argument("milliseconds", IntegerArgumentType.integer(0, 1000))
                    .executes(PingCommand::setDelay)))
            .then(literal("off")
                .executes(PingCommand::disablePing))
            .then(literal("on")
                .executes(PingCommand::enablePing))
            .then(literal("status")
                .executes(PingCommand::showStatus))
            .then(literal("jitter")
                .then(argument("milliseconds", IntegerArgumentType.integer(0, 100))
                    .executes(PingCommand::setJitter)))
            .then(literal("toggle")
                .then(literal("attacks")
                    .executes(ctx -> togglePacketType(ctx, "attacks")))
                .then(literal("movement")
                    .executes(ctx -> togglePacketType(ctx, "movement")))
                .then(literal("interactions")
                    .executes(ctx -> togglePacketType(ctx, "interactions")))
                .then(literal("blocks")
                    .executes(ctx -> togglePacketType(ctx, "blocks")))
                .then(literal("items")
                    .executes(ctx -> togglePacketType(ctx, "items"))))
            .executes(PingCommand::showStatus)
        );
    }
    
    private static int setDelay(CommandContext<FabricClientCommandSource> ctx) {
        int ms = IntegerArgumentType.getInteger(ctx, "milliseconds");
        FakePingConfig config = FakePingMod.getConfig();
        
        config.setBaseDelayMs(ms);
        config.setEnabled(true);
        
        ctx.getSource().sendFeedback(Text.literal("§aFake ping set to " + ms + "ms"));
        return 1;
    }
    
    private static int disablePing(CommandContext<FabricClientCommandSource> ctx) {
        FakePingConfig config = FakePingMod.getConfig();
        config.setEnabled(false);
        
        // Clear any queued packets
        FakePingMod.getPacketDelayManager().clearQueue();
        
        ctx.getSource().sendFeedback(Text.literal("§cFake ping disabled"));
        return 1;
    }
    
    private static int enablePing(CommandContext<FabricClientCommandSource> ctx) {
        FakePingConfig config = FakePingMod.getConfig();
        config.setEnabled(true);
        
        ctx.getSource().sendFeedback(Text.literal("§aFake ping enabled (" + 
            config.getBaseDelayMs() + "ms)"));
        return 1;
    }
    
    private static int setJitter(CommandContext<FabricClientCommandSource> ctx) {
        int ms = IntegerArgumentType.getInteger(ctx, "milliseconds");
        FakePingConfig config = FakePingMod.getConfig();
        
        config.setJitterMs(ms);
        
        ctx.getSource().sendFeedback(Text.literal("§aPing jitter set to ±" + ms + "ms"));
        return 1;
    }
    
    private static int showStatus(CommandContext<FabricClientCommandSource> ctx) {
        FakePingConfig config = FakePingMod.getConfig();
        
        ctx.getSource().sendFeedback(Text.literal("§6§l=== FakePing Status ==="));
        
        if (config.isEnabled()) {
            ctx.getSource().sendFeedback(Text.literal("§aEnabled: §fYes"));
            ctx.getSource().sendFeedback(Text.literal("§aBase Delay: §f" + 
                config.getBaseDelayMs() + "ms"));
            ctx.getSource().sendFeedback(Text.literal("§aJitter: §f±" + 
                config.getJitterMs() + "ms"));
            
            ctx.getSource().sendFeedback(Text.literal("§6Delayed Packet Types:"));
            ctx.getSource().sendFeedback(Text.literal("  §7Attacks: " + 
                (config.shouldDelayAttacks() ? "§aON" : "§cOFF")));
            ctx.getSource().sendFeedback(Text.literal("  §7Movement: " + 
                (config.shouldDelayMovement() ? "§aON" : "§cOFF")));
            ctx.getSource().sendFeedback(Text.literal("  §7Interactions: " + 
                (config.shouldDelayInteractions() ? "§aON" : "§cOFF")));
            ctx.getSource().sendFeedback(Text.literal("  §7Block Breaking: " + 
                (config.shouldDelayBlockBreaking() ? "§aON" : "§cOFF")));
            ctx.getSource().sendFeedback(Text.literal("  §7Item Use: " + 
                (config.shouldDelayItemUse() ? "§aON" : "§cOFF")));
            
            int queueSize = FakePingMod.getPacketDelayManager().getQueueSize();
            ctx.getSource().sendFeedback(Text.literal("§aQueued Packets: §f" + queueSize));
        } else {
            ctx.getSource().sendFeedback(Text.literal("§cEnabled: §fNo"));
            ctx.getSource().sendFeedback(Text.literal("§7Use §f/ping set <ms>§7 to enable"));
        }
        
        return 1;
    }
    
    private static int togglePacketType(CommandContext<FabricClientCommandSource> ctx, String type) {
        FakePingConfig config = FakePingMod.getConfig();
        boolean newState = false;
        
        switch (type) {
            case "attacks":
                newState = !config.shouldDelayAttacks();
                config.setDelayAttacks(newState);
                break;
            case "movement":
                newState = !config.shouldDelayMovement();
                config.setDelayMovement(newState);
                break;
            case "interactions":
                newState = !config.shouldDelayInteractions();
                config.setDelayInteractions(newState);
                break;
            case "blocks":
                newState = !config.shouldDelayBlockBreaking();
                config.setDelayBlockBreaking(newState);
                break;
            case "items":
                newState = !config.shouldDelayItemUse();
                config.setDelayItemUse(newState);
                break;
        }
        
        String status = newState ? "§aenabled" : "§cdisabled";
        ctx.getSource().sendFeedback(Text.literal("§6Delay for " + type + " " + status));
        
        return 1;
    }
}
