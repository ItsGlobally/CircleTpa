package me.itsglobally.circleTpa;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class data {
    private static final Map<UUID, List<HashMap<UUID, BukkitTask>>> tpaRequests = new HashMap<>();
    private static final Map<UUID, List<HashMap<UUID, BukkitTask>>> tpaHereRequests = new HashMap<>();
    private static final Map<UUID, Boolean> tpAutoSettings = new HashMap<>();
    private static final Map<UUID, Boolean> canTpaSettings = new HashMap<>();
    
    public static boolean canTpa(Player player) {
        return canTpaSettings.getOrDefault(player.getUniqueId(), true);
    }
    
    public static void setCanTpa(Player player, boolean canTpa) {
        canTpaSettings.put(player.getUniqueId(), canTpa);
    }
    
    public static boolean getTpAuto(Player player) {
        return tpAutoSettings.getOrDefault(player.getUniqueId(), false);
    }
    
    public static void setTpAuto(Player player, boolean tpAuto) {
        tpAutoSettings.put(player.getUniqueId(), tpAuto);
    }
    
    public static void addTpa(Player sender, Player target, tpaType type) {
        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        
        // Create timeout task
        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(
            CircleTpa.getInstance(), 
            () -> {
                remTpa(sender, target, true, type);
                utils.sendActionBar(sender, "&c對 " + target.getName() + " 的請求已過期!");
                utils.sendActionBar(target, "&c來自 " + sender.getName() + " 的請求已過期!");
            }, 
            6000L // 5 minutes = 6000 ticks
        );
        
        HashMap<UUID, BukkitTask> requestMap = new HashMap<>();
        requestMap.put(targetUUID, timeoutTask);
        
        Map<UUID, List<HashMap<UUID, BukkitTask>>> requestsMap = 
            (type == tpaType.TPA) ? tpaRequests : tpaHereRequests;
        
        requestsMap.computeIfAbsent(senderUUID, k -> new ArrayList<>()).add(requestMap);
    }
    
    public static void remTpa(Player sender, Player target, boolean timeout, tpaType type) {
        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        
        Map<UUID, List<HashMap<UUID, BukkitTask>>> requestsMap = 
            (type == tpaType.TPA) ? tpaRequests : tpaHereRequests;
        
        List<HashMap<UUID, BukkitTask>> senderRequests = requestsMap.get(senderUUID);
        if (senderRequests != null) {
            senderRequests.removeIf(requestMap -> {
                if (requestMap.containsKey(targetUUID)) {
                    BukkitTask task = requestMap.get(targetUUID);
                    if (!timeout && task != null) {
                        task.cancel();
                    }
                    return true;
                }
                return false;
            });
            
            if (senderRequests.isEmpty()) {
                requestsMap.remove(senderUUID);
            }
        }
    }
    
    public static List<HashMap<UUID, BukkitTask>> getTpa(Player player) {
        return tpaRequests.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
    
    public static List<HashMap<UUID, BukkitTask>> getTpaHere(Player player) {
        return tpaHereRequests.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
    
    public static boolean hasIncomingRequest(Player receiver, Player sender, tpaType type) {
        Map<UUID, List<HashMap<UUID, BukkitTask>>> requestsMap = 
            (type == tpaType.TPA) ? tpaRequests : tpaHereRequests;
        
        List<HashMap<UUID, BukkitTask>> senderRequests = requestsMap.get(sender.getUniqueId());
        if (senderRequests != null) {
            for (HashMap<UUID, BukkitTask> requestMap : senderRequests) {
                if (requestMap.containsKey(receiver.getUniqueId())) {
                    return true;
                }
            }
        }
        return false;
    }
}