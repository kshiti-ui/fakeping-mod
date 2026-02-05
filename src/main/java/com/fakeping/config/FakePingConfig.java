package com.fakeping.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration management for FakePing mod.
 * Handles saving/loading settings and providing runtime configuration access.
 */
public class FakePingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("fakeping.json");
    
    // Configuration fields
    private boolean enabled = false;
    private int baseDelayMs = 150;
    private int jitterMs = 20;
    private boolean delayAttacks = true;
    private boolean delayMovement = true;
    private boolean delayInteractions = true;
    private boolean delayBlockBreaking = true;
    private boolean delayItemUse = true;
    private boolean showHud = true;
    
    /**
     * Load configuration from file, or create default if it doesn't exist.
     */
    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                FakePingConfig loaded = GSON.fromJson(json, FakePingConfig.class);
                if (loaded != null) {
                    copyFrom(loaded);
                }
            } catch (IOException e) {
                System.err.println("Failed to load FakePing config: " + e.getMessage());
            }
        } else {
            save();
        }
    }
    
    /**
     * Save current configuration to file.
     */
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("Failed to save FakePing config: " + e.getMessage());
        }
    }
    
    private void copyFrom(FakePingConfig other) {
        this.enabled = other.enabled;
        this.baseDelayMs = other.baseDelayMs;
        this.jitterMs = other.jitterMs;
        this.delayAttacks = other.delayAttacks;
        this.delayMovement = other.delayMovement;
        this.delayInteractions = other.delayInteractions;
        this.delayBlockBreaking = other.delayBlockBreaking;
        this.delayItemUse = other.delayItemUse;
        this.showHud = other.showHud;
    }
    
    // Getters and setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }
    
    public int getBaseDelayMs() {
        return baseDelayMs;
    }
    
    public void setBaseDelayMs(int baseDelayMs) {
        this.baseDelayMs = Math.max(0, Math.min(1000, baseDelayMs));
        save();
    }
    
    public int getJitterMs() {
        return jitterMs;
    }
    
    public void setJitterMs(int jitterMs) {
        this.jitterMs = Math.max(0, Math.min(100, jitterMs));
        save();
    }
    
    public boolean shouldDelayAttacks() {
        return delayAttacks;
    }
    
    public void setDelayAttacks(boolean delayAttacks) {
        this.delayAttacks = delayAttacks;
        save();
    }
    
    public boolean shouldDelayMovement() {
        return delayMovement;
    }
    
    public void setDelayMovement(boolean delayMovement) {
        this.delayMovement = delayMovement;
        save();
    }
    
    public boolean shouldDelayInteractions() {
        return delayInteractions;
    }
    
    public void setDelayInteractions(boolean delayInteractions) {
        this.delayInteractions = delayInteractions;
        save();
    }
    
    public boolean shouldDelayBlockBreaking() {
        return delayBlockBreaking;
    }
    
    public void setDelayBlockBreaking(boolean delayBlockBreaking) {
        this.delayBlockBreaking = delayBlockBreaking;
        save();
    }
    
    public boolean shouldDelayItemUse() {
        return delayItemUse;
    }
    
    public void setDelayItemUse(boolean delayItemUse) {
        this.delayItemUse = delayItemUse;
        save();
    }
    
    public boolean shouldShowHud() {
        return showHud;
    }
    
    public void setShowHud(boolean showHud) {
        this.showHud = showHud;
        save();
    }
}
