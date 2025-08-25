package me.itsglobally.circleTpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class tpaCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player p)) {
            commandSender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        // Handle commands that don't require a target player
        if (label.equals("tpauto")) {
            tpauto(p);
            return true;
        }
        
        if (label.equals("tpallow")) {
            tpallow(p);
            return true;
        }
        
        // Commands that require a target player
        if (args.length == 0) {
            utils.send(p, "&c請指定一個玩家!");
            return true;
        }
        
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            utils.send(p, "&c找不到玩家: " + args[0]);
            return true;
        }
        
        switch (label) {
            case "tpa" -> tpa(p, target);
            case "tpahere" -> tpahere(p, target);
            case "tpaccept" -> tpaccept(p, target);
            case "tpdeny" -> tpdeny(p, target);
            case "tpcancel" -> tpcancel(p, target);
        }
        return true;
    }

    private void tpahere(Player p, Player target) {
        if (p == target) {
            utils.send(p, "&c你不能對自己發送請求!");
            return;
        }
        
        if (!data.canTpa(target)) {
            utils.sendActionBar(p, "&7這個人不允許tpahere請求!");
            return;
        }

        if (data.getTpAuto(target)) {
            target.teleport(p.getLocation());
            utils.sendActionBar(p, "&3" + target.getName() + "已傳送到你的位置!");
            utils.sendActionBar(target, "&3你已傳送到" + p.getName() + "的位置!");
            return;
        }

        data.addTpa(p, target, tpaType.TPAHERE);
        utils.sendActionBar(p, "&a已發送請求! 他有5分鐘的時間接受!");
        utils.sendActionBar(target, "&a" + p.getName() + "對你發送tpahere請求! 你有5分鐘的時間接受!");
        
        Component msg = Component.text(p.getName() + "對你發送tpahere請求!").color(NamedTextColor.GREEN);
        Component acceptButton = Component.text(" ✔")
                .color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/tpaccept " + p.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("接受請求")));
        Component denyButton = Component.text(" ❌ ")
                .color(NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/tpdeny " + p.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("拒絕請求")));

        utils.sendComponent(target, msg.append(acceptButton).append(denyButton));
    }

    private void tpcancel(Player p, Player target) {
        List<HashMap<UUID, BukkitTask>> tpaList = data.getTpa(p);
        List<HashMap<UUID, BukkitTask>> tpaHereList = data.getTpaHere(p);
        
        // Check TPA requests
        for (HashMap<UUID, BukkitTask> requestMap : tpaList) {
            if (requestMap.containsKey(target.getUniqueId())) {
                data.remTpa(p, target, false, tpaType.TPA);
                utils.sendActionBar(p, "&7已取消對" + target.getName() + "的請求!");
                utils.sendActionBar(target, "&7" + p.getName() + "已取消對你的tpa請求!");
                return;
            }
        }
        
        // Check TPAHERE requests
        for (HashMap<UUID, BukkitTask> requestMap : tpaHereList) {
            if (requestMap.containsKey(target.getUniqueId())) {
                data.remTpa(p, target, false, tpaType.TPAHERE);
                utils.sendActionBar(p, "&7已取消對" + target.getName() + "的請求!");
                utils.sendActionBar(target, "&7" + p.getName() + "已取消對你的tpahere請求!");
                return;
            }
        }
        
        utils.send(p, "&c你沒有對" + target.getName() + "發送請求!");
    }

    private void tpdeny(Player p, Player target) {
        boolean foundRequest = false;
        
        // Check if there's a TPA request from target to p
        if (data.hasIncomingRequest(p, target, tpaType.TPA)) {
            data.remTpa(target, p, false, tpaType.TPA);
            foundRequest = true;
        }
        
        // Check if there's a TPAHERE request from target to p
        if (data.hasIncomingRequest(p, target, tpaType.TPAHERE)) {
            data.remTpa(target, p, false, tpaType.TPAHERE);
            foundRequest = true;
        }
        
        if (foundRequest) {
            utils.sendActionBar(p, "&7已拒絕" + target.getName() + "的請求!");
            utils.sendActionBar(target, "&7" + p.getName() + "已拒絕你的請求!");
        } else {
            utils.send(p, "&c" + target.getName() + "沒有對你發送請求!");
        }
    }

    private void tpaccept(Player p, Player target) {
        // Check TPA requests (target wants to teleport to p)
        if (data.hasIncomingRequest(p, target, tpaType.TPA)) {
            data.remTpa(target, p, false, tpaType.TPA);
            target.teleport(p.getLocation());
            utils.sendActionBar(p, "&3" + target.getName() + "已傳送到你的位置!");
            utils.sendActionBar(target, "&3你已傳送到" + p.getName() + "的位置!");
            return;
        }
        
        // Check TPAHERE requests (target wants p to teleport to target)
        if (data.hasIncomingRequest(p, target, tpaType.TPAHERE)) {
            data.remTpa(target, p, false, tpaType.TPAHERE);
            p.teleport(target.getLocation());
            utils.sendActionBar(p, "&3你已傳送到" + target.getName() + "的位置!");
            utils.sendActionBar(target, "&3" + p.getName() + "已傳送到你的位置!");
            return;
        }
        
        utils.send(p, "&c" + target.getName() + "沒有對你發送請求!");
    }

    private void tpauto(Player p) {
        if (data.getTpAuto(p)) {
            data.setTpAuto(p, false);
            utils.sendActionBar(p, "&7關閉自動接受tpa!");
        } else {
            data.setTpAuto(p, true);
            utils.sendActionBar(p, "&3開啟自動接受tpa!");
        }
    }

    private void tpallow(Player p) {
        if (data.canTpa(p)) {
            data.setCanTpa(p, false);
            utils.sendActionBar(p, "&7沒有人可以tp你!");
        } else {
            data.setCanTpa(p, true);
            utils.sendActionBar(p, "&3所有人都可以tp你!");
        }
    }

    private void tpa(Player p, Player target) {
        if (p == target) {
            utils.send(p, "&c你不能對自己發送請求!");
            return;
        }
        
        if (!data.canTpa(target)) {
            utils.sendActionBar(p, "&7這個人不允許tpa請求!");
            return;
        }

        if (data.getTpAuto(target)) {
            p.teleport(target.getLocation());
            utils.sendActionBar(p, "&3你已傳送到" + target.getName() + "的位置!");
            utils.sendActionBar(target, "&3" + p.getName() + "已傳送到你的位置!");
            return;
        }

        data.addTpa(p, target, tpaType.TPA);
        utils.sendActionBar(p, "&3已發送請求! 他有5分鐘的時間接受!");
        utils.sendActionBar(target, "&3" + p.getName() + "對你發送tpa請求! 你有5分鐘的時間接受!");
        
        Component msg = Component.text(p.getName() + "對你發送tpa請求!").color(NamedTextColor.DARK_AQUA);
        Component acceptButton = Component.text(" ✔")
                .color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/tpaccept " + p.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("接受請求")));
        Component denyButton = Component.text(" ❌ ")
                .color(NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/tpdeny " + p.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("拒絕請求")));

        utils.sendComponent(target, msg.append(acceptButton).append(denyButton));
    }
}