package eu.carrade.amaury.Camelia.utils;

import eu.carrade.amaury.Camelia.*;
import org.bukkit.*;
import org.bukkit.entity.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/*
 * This file is part of Camelia.
 *
 * Camelia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Camelia is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Camelia.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ActionBar {

	private static boolean enabled = true;

	private static String nmsver;

	private static Class<?> craftPlayerClass;
	private static Class<?> packetPlayOutChatClass;
	private static Class<?> packetClass;
	private static Class<?> chatSerializerClass;
	private static Class<?> iChatBaseComponentClass;
	private static Class<?> chatComponentTextClass;

	private static Map<UUID, String> actionMessages = new ConcurrentHashMap<>();

	/**
	 * Sends a constant message to the given player. <p/> This message will remain on the screen until the {@link
	 * #removeMessage} method is called.
	 *
	 * @param player  The player.
	 * @param message The message to display.
	 */
	public static void sendPermanentMessage(Player player, String message) {
		actionMessages.put(player.getUniqueId(), message);
		sendMessage(player, message);
	}

	/**
	 * Sends a constant message to the given player. <p/> This message will remain on the screen until the {@link
	 * #removeMessage} method is called.
	 *
	 * @param playerUUID The player's UUID.
	 * @param message    The message to display.
	 */
	public static void sendPermanentMessage(UUID playerUUID, String message) {
		actionMessages.put(playerUUID, message);
		sendMessage(playerUUID, message);
	}


	/**
	 * Sends an action-bar message to the given player. <p/> This message will remain approximately three seconds.
	 *
	 * @param playerUUID The player's UUID.
	 * @param message    The message.
	 */
	public static void sendMessage(UUID playerUUID, String message) {
		sendMessage(Bukkit.getPlayer(playerUUID), message);
	}

	/**
	 * Sends an action-bar message to the given player. <p/> This message will remain approximately three seconds.
	 *
	 * @param player  The player.
	 * @param message The message.
	 *
	 * @author ConnorLinfoot (https://github.com/ConnorLinfoot/ActionBarAPI/).
	 */
	public static void sendMessage(Player player, String message) {

		if (!enabled || player == null || message == null) return;

		try {
			Object craftPlayer = craftPlayerClass.cast(player);
			Object chatPacket;

			if (nmsver.equalsIgnoreCase("v1_8_R1") || !nmsver.startsWith("v1_8_")) {
				Method m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
				Object cbc = iChatBaseComponentClass.cast(m3.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));
				chatPacket = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(cbc, (byte) 2);
			} else {
				Object o = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
				chatPacket = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(o, (byte) 2);
			}

			Method playerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
			Object handle = playerHandleMethod.invoke(craftPlayer);

			Field playerConnectionField = handle.getClass().getDeclaredField("playerConnection");
			Object playerConnection = playerConnectionField.get(handle);

			Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
			sendPacketMethod.invoke(playerConnection, chatPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Removes the action bar message displayed to the given player.
	 *
	 * @param player  The player.
	 * @param instant If {@code true}, the message will be removed instantly. Else, it will dismiss progressively.
	 *                Please note that in that case, the message may be displayed a few more seconds.
	 */
	public static void removeMessage(Player player, boolean instant) {
		actionMessages.remove(player.getUniqueId());

		if (instant) {
			sendMessage(player, "");
		}
	}

	/**
	 * Removes the action bar message displayed to the given player.
	 *
	 * @param playerUUID The UUID of the player.
	 * @param instant    If {@code true}, the message will be removed instantly. Else, it will dismiss progressively.
	 *                   Please note that in that case, the message may be displayed a few more seconds.
	 */
	public static void removeMessage(UUID playerUUID, boolean instant) {
		actionMessages.remove(playerUUID);

		if (instant) {
			sendMessage(playerUUID, "");
		}
	}

	/**
	 * Removes the action bar message displayed to the given player.
	 *
	 * @param player The player.
	 */
	public static void removeMessage(Player player) {
		removeMessage(player, false);
	}

	/**
	 * Removes the action bar message displayed to the given player.
	 *
	 * @param playerUUID The UUID of the player.
	 */
	public static void removeMessage(UUID playerUUID) {
		removeMessage(playerUUID, false);
	}


	/**
	 * Initializes the task that will resent the permanent action messages to the players.
	 */
	private static void initActionMessageUpdater() {
		Bukkit.getScheduler().runTaskTimer(Camelia.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Map.Entry<UUID, String> entry : actionMessages.entrySet()) {
					Player player = Bukkit.getPlayer(entry.getKey());
					if (player != null && player.isOnline()) {
						sendMessage(player, entry.getValue());
					}
				}
			}
		}, 2l, 30l);
	}


	static {
		nmsver = Bukkit.getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

		try {

			iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
			packetPlayOutChatClass = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
			craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
			packetClass = Class.forName("net.minecraft.server." + nmsver + ".Packet");

			if (nmsver.equalsIgnoreCase("v1_8_R1") || !nmsver.startsWith("v1_8_")) {
				chatSerializerClass = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
			} else {
				chatComponentTextClass = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
			}

		} catch (Exception e) {
			enabled = false;
		}

		initActionMessageUpdater();
	}
}
