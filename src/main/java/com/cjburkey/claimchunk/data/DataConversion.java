package com.cjburkey.claimchunk.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.Access;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.player.PlayerHandler;

@SuppressWarnings("deprecation")
public class DataConversion {

    public static void check(File chunk, File cache, File access, ClaimChunk c) throws Exception {
        Utils.log("Checking for old data...");
        if (chunk.exists()) {
            convertChunks(chunk, c.getChunkHandler());
        }
        if (cache.exists()) {
            convertCache(cache, c.getPlayerHandler());
        }
        if (access.exists()) {
            convertAccess(access, c.getPlayerHandler());
        }
    }

    private static void convertChunks(File file, ChunkHandler handler) throws Exception {
        Utils.log("Updating chunks.");
        readChunks(file, handler);
        handler.writeToDisk();
        file.delete();
    }

    private static void convertCache(File file, PlayerHandler handler) throws Exception {
        Utils.log("Updating cache.");
        readOldCacher(file, handler);
        handler.writeToDisk();
        file.delete();
    }

    private static void convertAccess(File file, PlayerHandler handler) throws Exception {
        Utils.log("Updating access.");
        readOldAccess(file, handler);
        handler.writeToDisk();
        file.delete();
    }

    private static void readChunks(File file, ChunkHandler handler) throws IOException {
        if (file.exists()) {
            BufferedReader reader = null;
            List<String> lines = new ArrayList<>();
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                reader.close();
                loadChunkLines(lines.toArray(new String[lines.size()]), handler);
                return;
            } catch (FileNotFoundException e) {
                throw e;
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } else {
            Utils.err("File not found: " + file);
        }
    }

    private static void loadChunkLines(String[] lines, ChunkHandler handler) throws IOException {
        for (String line : lines) {
            if (!line.startsWith(";;-")) {
                String[] split = line.split(Pattern.quote(";"));
                if (split.length == 2) {
                    ChunkPos pos = ChunkPos.fromString(split[0].trim());
                    UUID ply = UUID.fromString(split[1].trim());
                    if (pos != null && ply != null) {
                        handler.claimChunk(pos.getWorld(), pos.getX(), pos.getZ(), ply);
                    }
                }
            }
        }
    }

    private static void readOldCacher(File file, PlayerHandler ply) throws IOException, ClassNotFoundException {
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                Object in = ois.readObject();
                ois.close();
                Map<?, ?> inMap = (ConcurrentHashMap<?, ?>) in;
                for (Entry<?, ?> entry : inMap.entrySet()) {
                    ply.addOldPlayerData((UUID) entry.getKey(), (String) entry.getValue());
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

    private static void readOldAccess(File file, PlayerHandler ply) throws ClassNotFoundException, IOException {
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                Object in = ois.readObject();
                ois.close();
                Queue<?> inQueue = (Queue<?>) in;
                for (Object obj : inQueue) {
                    Access ac = (Access) obj;
                    ply.giveAccess(ac.getOwner(), ac.getAcessee());
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