package me.rainbow.gameutilities;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateSign;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin implements Listener {

	Logger log = Logger.getLogger("Minecraft");
	public static Economy econ = null;
	public boolean hasVault;
	
	@Override
	public void onEnable() {
		//saveDefaultConfig();
		if(!setupEconomy()) {
			hasVault = false;
		}else {
			hasVault = true;
		}
		getServer().getPluginManager().registerEvents(this, this);
		log.info(String.format("[%s] %s has been enabled!", getDescription().getName(), getDescription().getName()));
	}
	
	@Override
	public void onDisable() {
		
		log.info(String.format("[%s] %s has been diabled!", getDescription().getName(), getDescription().getName()));
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		
		if(e.getClickedBlock() == null) return;
		if(
				e.getClickedBlock().getType() != Material.SIGN &&
				e.getClickedBlock().getType() != Material.SIGN_POST &&
				e.getClickedBlock().getType() != Material.WALL_SIGN
		) {
			return;
		}
		
		Sign s = (Sign)e.getClickedBlock().getState();
		
		CraftWorld world = (CraftWorld) e.getClickedBlock().getLocation().getWorld();
		BlockPosition pos = new BlockPosition(e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ());
		
		if(e.getPlayer().hasPermission("gameutilites.findPlayer")) {
			if(hasVault) {
				if(econ.withdrawPlayer(e.getPlayer(), getConfig().getInt("findPlayerSignCost")).transactionSuccess()) {
					econ.withdrawPlayer(e.getPlayer(), getConfig().getInt("findPlayerSignCost"));
					econ.depositPlayer(e.getPlayer(), getConfig().getInt("findPlayerSignCost"));
				}else {
					e.getPlayer().sendMessage(ChatColor.RED + "You have insufficient funds!");
					return;
				}
			}
			
			if(Bukkit.getPlayer(s.getLine(0)) != null && s.getLine(1).contains("Find Player") || Bukkit.getPlayer(s.getLine(0)) != null && s.getLine(1).contains("X")) {
				PacketPlayOutUpdateSign packet = new PacketPlayOutUpdateSign(world.getHandle(), pos, new IChatBaseComponent[] {
					ChatSerializer.a("{\"text\":\"" + ChatColor.BOLD + s.getLine(0) + "\"}"),
					ChatSerializer.a("{\"text\":\"" + "X" + ChatColor.RED + Bukkit.getPlayer(s.getLine(0)).getLocation().getX() + "\"}"),
					ChatSerializer.a("{\"text\":\"" + "Y" + ChatColor.BLUE + Bukkit.getPlayer(s.getLine(0)).getLocation().getY() + "\"}"),
					ChatSerializer.a("{\"text\":\"" + "Z" + ChatColor.GREEN + Bukkit.getPlayer(s.getLine(0)).getLocation().getZ() + "\"}")
				});
				((CraftPlayer)e.getPlayer()).getHandle().playerConnection.sendPacket(packet);
			}else {
				
			}
		}else {
			e.getPlayer().sendMessage(ChatColor.RED + "You do not have permission!");
		}
	}
}
