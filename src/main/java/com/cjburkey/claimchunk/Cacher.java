package com.cjburkey.claimchunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class Cacher {
	
	private final Map<UUID, String> players = new ConcurrentHashMap<>();
	
	public void onJoin(Player player) {
		players.put(player.getUniqueId(), player.getName());
		reload();
	}
	
	public String getName(UUID uuid) {
		return players.get(uuid);
	}
	
	public UUID getUuid(String name) {
		for(Entry<UUID, String> entry : players.entrySet()) {
			if(entry.getValue().equals(name)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public void reload() {
		try {
			write(ClaimChunk.getInstance().getPlyFile());
			read(ClaimChunk.getInstance().getPlyFile());
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
			oos.writeObject(players);
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
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(file));
			Object in = ois.readObject();
			ois.close();
			players.clear();
			Map<?, ?> inMap = (ConcurrentHashMap<?, ?>) in;
			for(Entry<?, ?> entry : inMap.entrySet()) {
				players.put((UUID) entry.getKey(), (String) entry.getValue());
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}
	
}