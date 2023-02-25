package org.popcraft.bolt.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.popcraft.bolt.BoltPlugin;
import org.popcraft.bolt.data.ProfileCache;
import org.popcraft.bolt.protection.BlockProtection;
import org.popcraft.bolt.protection.EntityProtection;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BukkitAdapter {
    private static final String NIL_UUID_STRING = "00000000-0000-0000-0000-000000000000";
    public static final UUID NIL_UUID = UUID.fromString(NIL_UUID_STRING);

    private BukkitAdapter() {
    }

    public static BlockProtection createBlockProtection(final Block block, final UUID owner, final String type) {
        final long now = System.currentTimeMillis();
        return new BlockProtection(UUID.randomUUID(), owner, type, now, now, new HashMap<>(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getType().name());
    }

    public static BlockLocation blockLocation(final Block block) {
        return new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static BlockLocation blockLocation(final BlockState blockState) {
        return new BlockLocation(blockState.getWorld().getName(), blockState.getX(), blockState.getY(), blockState.getZ());
    }

    public static BlockLocation blockLocation(final Location location) {
        Objects.requireNonNull(location.getWorld());
        return new BlockLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockLocation blockLocation(final BlockProtection blockProtection) {
        return new BlockLocation(blockProtection.getWorld(), blockProtection.getX(), blockProtection.getY(), blockProtection.getZ());
    }

    public static EntityProtection createEntityProtection(final Entity entity, final UUID owner, final String type) {
        final long now = System.currentTimeMillis();
        return new EntityProtection(entity.getUniqueId(), owner, type, now, now, new HashMap<>(), entity.getType().name());
    }

    public static UUID findPlayerUniqueId(final String name) {
        if (name == null || NIL_UUID_STRING.equals(name)) {
            return null;
        }
        try {
            return UUID.fromString(name);
        } catch (final IllegalArgumentException e) {
            final ProfileCache profileCache = JavaPlugin.getPlugin(BoltPlugin.class).getProfileCache();
            final UUID cached = profileCache.getUniqueId(name);
            if (cached != null) {
                return cached;
            }
            final OfflinePlayer offlinePlayer = PaperUtil.getOfflinePlayer(name);
            if (offlinePlayer != null) {
                profileCache.add(offlinePlayer.getUniqueId(), offlinePlayer.getName());
            }
            return offlinePlayer == null ? null : offlinePlayer.getUniqueId();
        }
    }

    public static CompletableFuture<UUID> lookupPlayerUniqueId(final String name) {
        final PlayerProfile playerProfile = Bukkit.createPlayerProfile(name);
        final CompletableFuture<PlayerProfile> updatedProfile = playerProfile.update();
        updatedProfile.thenAccept(profile -> {
            if (profile.isComplete()) {
                final ProfileCache profileCache = JavaPlugin.getPlugin(BoltPlugin.class).getProfileCache();
                profileCache.add(profile.getUniqueId(), profile.getName());
            }
        });
        return updatedProfile.thenApplyAsync(PlayerProfile::getUniqueId, BukkitMainThreadExecutor.get());
    }

    public static CompletableFuture<UUID> findOrLookupPlayerUniqueId(final String name) {
        final UUID found = BukkitAdapter.findPlayerUniqueId(name);
        if (found != null) {
            return CompletableFuture.completedFuture(found);
        }
        return lookupPlayerUniqueId(name);
    }

    public static String findPlayerName(final UUID uuid) {
        final ProfileCache profileCache = JavaPlugin.getPlugin(BoltPlugin.class).getProfileCache();
        return profileCache.getName(uuid);
    }

    public static CompletableFuture<String> lookupPlayerName(final UUID uuid) {
        if (NIL_UUID.equals(uuid)) {
            return CompletableFuture.completedFuture(null);
        }
        final PlayerProfile playerProfile = Bukkit.createPlayerProfile(uuid);
        final CompletableFuture<PlayerProfile> updatedProfile = playerProfile.update();
        updatedProfile.thenAccept(profile -> {
            if (profile.isComplete()) {
                final ProfileCache profileCache = JavaPlugin.getPlugin(BoltPlugin.class).getProfileCache();
                profileCache.add(profile.getUniqueId(), profile.getName());
            }
        });
        return updatedProfile.thenApplyAsync(PlayerProfile::getName, BukkitMainThreadExecutor.get());
    }

    public static CompletableFuture<String> findOrLookupPlayerName(final UUID uuid) {
        final String found = BukkitAdapter.findPlayerName(uuid);
        if (found != null) {
            return CompletableFuture.completedFuture(found);
        }
        return lookupPlayerName(uuid);
    }
}
