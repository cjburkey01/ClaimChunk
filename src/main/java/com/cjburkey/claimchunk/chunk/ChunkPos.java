package com.cjburkey.claimchunk.chunk;

import java.util.regex.Pattern;

public final class ChunkPos {
	
	private final int x;
	private final int z;
	
	public ChunkPos(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	public ChunkPos() {
		this(0, 0);
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	public String toString() {
		return x + "," + z;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChunkPos other = (ChunkPos) obj;
		if (x != other.x)
			return false;
		if (z != other.z)
			return false;
		return true;
	}
	
	public static ChunkPos fromString(String in) {
		String[] split = in.split(Pattern.quote(","));
		if(split.length == 2) {
			try {
				int x = Integer.parseInt(split[0].trim());
				int z = Integer.parseInt(split[1].trim());
				return new ChunkPos(x, z);
			} catch(Exception e) {
			}
		}
		return null;
	}
	
}