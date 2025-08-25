package me.itsglobally.circleTpa;

import org.bukkit.plugin.java.JavaPlugin;

public final class CircleTpa extends JavaPlugin {
    
    private static CircleTpa instance;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Register command executor
        tpaCommand commandExecutor = new tpaCommand();
        getCommand("tpa").setExecutor(commandExecutor);
        getCommand("tpahere").setExecutor(commandExecutor);
        getCommand("tpaccept").setExecutor(commandExecutor);
        getCommand("tpdeny").setExecutor(commandExecutor);
        getCommand("tpcancel").setExecutor(commandExecutor);
        getCommand("tpallow").setExecutor(commandExecutor);
        getCommand("tpauto").setExecutor(commandExecutor);
        
        getLogger().info("CircleTpa plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CircleTpa plugin has been disabled!");
    }
    
    public static CircleTpa getInstance() {
        return instance;
    }
}