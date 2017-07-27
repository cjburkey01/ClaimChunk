package com.cjburkey.claimchunk.player;

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
import com.cjburkey.claimchunk.ClaimChunk;

public class PlayerCustomNames {
	
	private final Map<UUID, String> customNames = new ConcurrentHashMap<>();
	
	public void setName(UUID player, String name) {
		customNames.put(player, name);
		reload();
	}
	
	public void resetName(UUID player) {
		customNames.remove(player);
		reload();
	}
	
	public boolean hasCustomName(UUID uuid) {
		return customNames.containsKey(uuid);
	}
	
	public String getCustomName(UUID uuid) {
		return customNames.get(uuid);
	}
	
	public UUID getPlayer(String name) {
		for (Entry<UUID, String> entry : customNames.entrySet()) {
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
			oos.writeObject(customNames);
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
				customNames.clear();
				Map<?, ?> inMap = (ConcurrentHashMap<?, ?>) in;
				for(Entry<?, ?> entry : inMap.entrySet()) {
					customNames.put((UUID) entry.getKey(), (String) entry.getValue());
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
	
}