package com.cjburkey.claimchunk.player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.data.DataPlayer;
import com.cjburkey.claimchunk.data.DataStorage;

public class PlayerHandler {
	
	private final Queue<DataPlayer> playerData = new ConcurrentLinkedQueue<>();
	private final DataStorage<DataPlayer> data;
	
	public PlayerHandler(File file) {
		data = new DataStorage<>(DataPlayer[].class, file);
	}
	
	/**
	 * Toggles the supplied players access to the owner's chunks.
	 * @param owner The chunk owner.
	 * @param player The player to toggle access.
	 * @return Whether or not the player NOW has access.
	 * @throws IOException 
	 */
	public boolean toggleAccess(UUID owner, UUID player) {
		if (hasAccess(owner, player)) {
			takeAccess(owner, player);
			return false;
		}
		giveAccess(owner, player);
		return true;
	}
	
	public void giveAccess(UUID owner, UUID player) {
		if (!hasAccess(owner, player)) {
			DataPlayer a = getPlayer(owner);
			if (a != null) {
				a.permitted.add(player);
			}
		}
	}
	
	public void takeAccess(UUID owner, UUID player) {
		if (hasAccess(owner, player)) {
			DataPlayer a = getPlayer(owner);
			if (a != null) {
				a.permitted.remove(player);
			}
		}
	}
	
	public boolean hasAccess(UUID owner, UUID player) {
		DataPlayer a = getPlayer(owner);
		if (a != null) {
			return a.permitted.contains(player);
		}
		return false;
	}
	
	public void clearChunkName(UUID player) {
		DataPlayer a = getPlayer(player);
		if (a != null) {
			a.chunkName = null;
		}
	}
	
	public void setChunkName(UUID player, String name) {
		DataPlayer a = getPlayer(player);
		if (a != null) {
			a.chunkName = name;
		}
	}
	
	public String getChunkName(UUID player) {
		if (hasChunkName(player)) {
			return getPlayer(player).chunkName;
		}
		return null;
	}
	
	public boolean hasChunkName(UUID player) {
		return getPlayer(player) != null && getPlayer(player).chunkName != null;
	}
	
	public String getUsername(UUID player) {
		DataPlayer a = getPlayer(player);
		if (a != null) {
			return a.lastIgn;
		}
		return null;
	}
	
	public UUID getUUID(String username) {
		for (DataPlayer ply : playerData) {
			if (ply.lastIgn != null && ply.lastIgn.equals(username)) {
				return ply.player;
			}
		}
		return null;
	}
	
	public List<String> getJoinedPlayers(String start) {
		List<String> out = new ArrayList<>();
		for (DataPlayer ply : playerData) {
			if (ply.lastIgn != null && ply.lastIgn.toLowerCase().startsWith(start.toLowerCase())) {
				out.add(ply.lastIgn);
			}
		}
		return out;
	}
	
	public void onJoin(Player ply) {
		if (getPlayer(ply.getUniqueId()) == null) {
			playerData.add(new DataPlayer(ply));
		}
	}
	
	public void addOldPlayerData(UUID id, String name) {
		if (getPlayer(id) == null) {
			playerData.add(new DataPlayer(id, name));
		}
	}
	
	public void writeToDisk() throws IOException {
		data.emptyObjects();
		for (DataPlayer a : playerData) {
			data.addObject(a.clone());
		}
		data.write();
	}
	
	public void readFromDisk() throws IOException {
		data.read();
		playerData.clear();
		for (DataPlayer ply : data.getObjects()) {
			playerData.add(ply.clone());
		}
	}
	
	private DataPlayer getPlayer(UUID owner) {
		for (DataPlayer a : playerData) {
			if (a.player.equals(owner)) {
				return a;
			}
		}
		return null;
	}
	
	/*public void reload() {
		try {
			write(ClaimChunk.getInstance().getAccessFile());
			read(ClaimChunk.getInstance().getAccessFile());
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void write(File file) throws IOException {
		if (file.exists()) {
			file.delete();
		}
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(access);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			throw e;
		} finally {
			if (oos != null) {
				oos.close();
			}
		}
	}
	
	public void read(File file) throws IOException, ClassNotFoundException {
		if (file.exists()) {
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new FileInputStream(file));
				Object in = ois.readObject();
				ois.close();
				access.clear();
				Queue<?> inQueue = (Queue<?>) in;
				for(Object obj : inQueue) {
					access.add((Access) obj);
				}
			} catch (IOException e) {
				throw e;
			} finally {
				if (ois != null) {
					ois.close();
				}
			}
		}
	}*/
	
}