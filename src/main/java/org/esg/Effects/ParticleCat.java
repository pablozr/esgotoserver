package org.esg.Effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

/**
 * Utility class for sending particle effects in the game world.
 */
public final class ParticleCat {

    private static final float DEFAULT_OFFSET = 0.0f;
    private static final float DEFAULT_SPEED = 0.0f;
    private static final int DEFAULT_COUNT = 1;

    private ParticleCat() {
        // Construtor privado para evitar instanciação.
    }

    /**
     * Sends particles to all online players at a given location.
     */
    public static void sendParticle(EnumParticle type, Location loc, float xOffset, float yOffset, float zOffset,
                                    float speed, int count) {
        sendParticleToAll(type, loc.getX(), loc.getY(), loc.getZ(), xOffset, yOffset, zOffset, speed, count);
    }

    /**
     * Sends particles to all online players at specific coordinates.
     */
    public static void sendParticle(EnumParticle type, double x, double y, double z, float xOffset, float yOffset,
                                    float zOffset, float speed, int count) {
        sendParticleToAll(type, x, y, z, xOffset, yOffset, zOffset, speed, count);
    }

    /**
     * Sends particles to a specific player at a given location.
     */
    public static void sendParticleTo(EnumParticle type, Player player, Location loc, float xOffset, float yOffset,
                                      float zOffset, float speed, int count) {
        sendParticleToPlayer(type, player, loc.getX(), loc.getY(), loc.getZ(), xOffset, yOffset, zOffset, speed, count);
    }

    /**
     * Sends particles to a specific player at specific coordinates.
     */
    public static void sendParticleTo(EnumParticle type, Player player, double x, double y, double z, float xOffset,
                                      float yOffset, float zOffset, float speed, int count) {
        sendParticleToPlayer(type, player, x, y, z, xOffset, yOffset, zOffset, speed, count);
    }

    private static void sendParticleToAll(EnumParticle type, double x, double y, double z, float xOffset, float yOffset,
                                          float zOffset, float speed, int count) {
        PacketPlayOutWorldParticles packet = createParticlePacket(type, x, y, z, xOffset, yOffset, zOffset, speed, count);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendPacket(player, packet);
        }
    }

    private static void sendParticleToPlayer(EnumParticle type, Player player, double x, double y, double z, float xOffset,
                                             float yOffset, float zOffset, float speed, int count) {
        PacketPlayOutWorldParticles packet = createParticlePacket(type, x, y, z, xOffset, yOffset, zOffset, speed, count);
        sendPacket(player, packet);
    }

    private static PacketPlayOutWorldParticles createParticlePacket(EnumParticle type, double x, double y, double z,
                                                                    float xOffset, float yOffset, float zOffset,
                                                                    float speed, int count) {
        return new PacketPlayOutWorldParticles(type, true, (float) x, (float) y, (float) z,
                xOffset, yOffset, zOffset, speed, count, null);
    }

    private static void sendPacket(Player player, PacketPlayOutWorldParticles packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}