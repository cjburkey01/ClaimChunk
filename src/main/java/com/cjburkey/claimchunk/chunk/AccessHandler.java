package com.cjburkey.claimchunk.chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.cjburkey.claimchunk.ClaimChunk;

public class AccessHandler {
	
	private final Queue<Access> access = new ConcurrentLinkedQueue<>();
	
	/**
	 * Toggles the supplied players access to the owner's chunks.
	 * @param owner The chunk owner.
	 * @param player The player to toggle access.
	 * @return Whether or not the player NOW has access.
	 */
	public boolean toggleAccess(UUID owner, UUID player) {
		if (hasAccess(owner, player)) {
			takeAccess(owner, player);
			return false;
		}
		giveAccess(owner, player);
		return true;
	}
	
	private void giveAccess(UUID owner, UUID player) {
		if (!hasAccess(owner, player)) {
			access.add(new Access(owner, player));
			reload();
		}
	}
	
	private void takeAccess(UUID owner, UUID player) {
		if (hasAccess(owner, player)) {
			access.remove(getAccess(owner, player));
			reload();
		}
	}
	
	public boolean hasAccess(UUID owner, UUID player) {
		for (UUID uuid : getPermitted(owner)) {
			if (uuid.equals(player)) {
				return true;
			}
		}
		return false;
	}
	
	public void reload() {
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
	
	private Access getAccess(UUID owner, UUID player) {
		for(Access a : access) {
			if(a.getOwner().equals(owner) && a.getAcessee().equals(player)) {
				return a;
			}
		}
		return null;
	}
	
	private UUID[] getPermitted(UUID owner) {
		List<UUID> out = new ArrayList<>();
		for (Access permit : access) {
			if (permit.getOwner().equals(owner)) {
				out.add(permit.getAcessee());
			}
		}
		return out.toArray(new UUID[out.size()]);
	}
	
}