package fr.neatmonster.nocheatplus.checks;

import net.minecraft.server.Block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.utilities.locations.PreciseLocation;

/**
 * Some stuff that's used by different checks or just too complex to keep
 * in other places
 * 
 */
public class CheckUtils {

    private static final double magic    = 0.45D;

    private static final double magic2   = 0.55D;

    private static final int    NONSOLID = 1;                    // 0x00000001
    private static final int    SOLID    = 2;                    // 0x00000010

    // All liquids are "nonsolid" too
    private static final int    LIQUID   = 4 | NONSOLID;         // 0x00000101
    // All ladders are "nonsolid" and "solid" too
    private static final int    LADDER   = 8 | NONSOLID | SOLID; // 0x00001011

    // All fences are solid - fences are treated specially due
    // to being 1.5 blocks high
    private static final int    FENCE    = 16 | NONSOLID | SOLID;  // 0x00010011

    // Webs slow players down so they must also be treated specially
    private static final int    WEB      = 32 | NONSOLID | SOLID;

    // We also treat unclimbable vines specially because players
    // can't climb them but they slow players down
    private static final int    VINE     = 64 | NONSOLID;

    private static final int    INGROUND = 128;

    private static final int    ONGROUND = 256;

    // Until I can think of a better way to determine if a block is solid or
    // not, this is what I'll do
    private static final int    types[];

    static {
        types = new int[256];

        // Find and define properties of all other blocks
        for (int i = 0; i < types.length; i++) {

            // Everything unknown is considered nonsolid and solid
            types[i] = NONSOLID | SOLID;

            if (Block.byId[i] != null)
                if (Block.byId[i].material.isSolid())
                    // STONE, CAKE, LEAFS, ...
                    types[i] = SOLID;
                else if (Block.byId[i].material.isLiquid())
                    // WATER, LAVA, ...
                    types[i] = LIQUID;
                else
                    // AIR, SAPLINGS, ...
                    types[i] = NONSOLID;
        }

        // Some exceptions where the above method fails

        // du'h
        types[Material.AIR.getId()] = NONSOLID;

        // Obvious
        types[Material.LADDER.getId()] = LADDER;
        types[Material.WATER_LILY.getId()] = LADDER;
        // The type VINE is only for unclimbable vines
        types[Material.VINE.getId()] = LADDER;

        types[Material.FENCE.getId()] = FENCE;
        types[Material.FENCE_GATE.getId()] = FENCE;
        types[Material.NETHER_FENCE.getId()] = FENCE;

        types[Material.WEB.getId()] = WEB;

        // These are sometimes solid, sometimes not
        types[Material.IRON_FENCE.getId()] = SOLID | NONSOLID;
        types[Material.THIN_GLASS.getId()] = SOLID | NONSOLID;

        // Signs are NOT solid, despite the game claiming they are
        types[Material.WALL_SIGN.getId()] = NONSOLID;
        types[Material.SIGN_POST.getId()] = NONSOLID;

        // (trap)doors can be solid or not
        types[Material.WOODEN_DOOR.getId()] = SOLID | NONSOLID;
        types[Material.IRON_DOOR_BLOCK.getId()] = SOLID | NONSOLID;
        types[Material.TRAP_DOOR.getId()] = SOLID | NONSOLID;

        // repeaters are technically half blocks
        types[Material.DIODE_BLOCK_OFF.getId()] = SOLID | NONSOLID;
        types[Material.DIODE_BLOCK_ON.getId()] = SOLID | NONSOLID;

        // pressure plates are so slim, you can consider them
        // nonsolid too
        types[Material.STONE_PLATE.getId()] = SOLID | NONSOLID;
        types[Material.WOOD_PLATE.getId()] = SOLID | NONSOLID;
    }

    /**
     * Check if a player looks at a target of a specific size, with a specific
     * precision value (roughly)
     */
    public static double directionCheck(final NCPPlayer player, final double targetX, final double targetY,
            final double targetZ, final double targetWidth, final double targetHeight, final double precision) {

        // Eye location of the player
        final Location eyes = player.getBukkitPlayer().getEyeLocation();

        final double factor = Math.sqrt(Math.pow(eyes.getX() - targetX, 2) + Math.pow(eyes.getY() - targetY, 2)
                + Math.pow(eyes.getZ() - targetZ, 2));

        // View direction of the player
        final Vector direction = eyes.getDirection();

        final double x = targetX - eyes.getX();
        final double y = targetY - eyes.getY();
        final double z = targetZ - eyes.getZ();

        final double xPrediction = factor * direction.getX();
        final double yPrediction = factor * direction.getY();
        final double zPrediction = factor * direction.getZ();

        double off = 0.0D;

        off += Math.max(Math.abs(x - xPrediction) - (targetWidth / 2 + precision), 0.0D);
        off += Math.max(Math.abs(z - zPrediction) - (targetWidth / 2 + precision), 0.0D);
        off += Math.max(Math.abs(y - yPrediction) - (targetHeight / 2 + precision), 0.0D);

        if (off > 1)
            off = Math.sqrt(off);

        return off;
    }

    /**
     * Ask NoCheatPlus what it thinks about a certain location.
     * Is it a place where a player can safely stand, should
     * it be considered as being inside a liquid etc.
     * 
     * @param world
     *            The world the coordinates belong to
     * @param location
     *            The precise location in the world
     * 
     * @return
     */
    public static int evaluateLocation(final World world, final PreciseLocation location) {

        final int lowerX = lowerBorder(location.x);
        final int upperX = upperBorder(location.x);
        final int Y = (int) location.y;
        final int lowerZ = lowerBorder(location.z);
        final int upperZ = upperBorder(location.z);

        // Check the four borders of the players hitbox for something he could
        // be standing on, and combine the results
        int result = 0;

        result |= evaluateSimpleLocation(world, lowerX, Y, lowerZ);
        result |= evaluateSimpleLocation(world, upperX, Y, lowerZ);
        result |= evaluateSimpleLocation(world, upperX, Y, upperZ);
        result |= evaluateSimpleLocation(world, lowerX, Y, upperZ);

        if (!isInGround(result))
            // Original location: X, Z (allow standing in walls this time)
            if (isSolid(getType(world, Location.locToBlock(location.x), Location.locToBlock(location.y),
                    Location.locToBlock(location.z))))
                result |= INGROUND;

        return result;
    }

    /**
     * Evaluate a location by only looking at a specific
     * "column" of the map to find out if that "column"
     * would allow a player to stand, swim etc. there
     * 
     * @param world
     * @param x
     * @param y
     * @param z
     * @return Returns INGROUND, ONGROUND, LIQUID, combination of the three or 0
     */
    private static int evaluateSimpleLocation(final World world, final int x, final int y, final int z) {

        // First we need to know about the block itself, the block
        // below it and the block above it
        final int top = getType(world, x, y + 1, z);
        final int base = getType(world, x, y, z);
        final int below = getType(world, x, y - 1, z);

        int type = 0;
        // Special case: Standing on a fence
        // Behave as if there is a block on top of the fence
        if (below == FENCE && base != FENCE && isNonSolid(top))
            type = INGROUND;
        else if (below != FENCE && isNonSolid(base) && getType(world, x, y - 2, z) == FENCE)
            type = ONGROUND;
        else if (isNonSolid(top))
            // Simplest (and most likely) case:
            // Below the player is a solid block
            if (isSolid(below) && isNonSolid(base))
                type = ONGROUND;
            else if (isLadder(base) || isLadder(top))
                type = ONGROUND;
            else if (isSolid(base))
                type = INGROUND;

        // (In every case, check for water...)
        if (isLiquid(base) || isLiquid(top))
            type |= LIQUID | INGROUND;
        // (...and for web...)
        if (isWeb(base) || isWeb(top))
            type |= WEB;
        // (...and for vine)
        if (isVine(base))
            type |= VINE;

        return type;
    }

    public static int getType(final int typeId) {
        return types[typeId];
    }

    private static int getType(final World world, final int x, final int y, final int z) {
        if (world.getBlockAt(x, y, z).getType() == Material.VINE && !isClimbable(world, x, y, z))
            return VINE;
        return types[world.getBlockTypeIdAt(x, y, z)];
    }

    public static boolean isClimbable(final World world, final int x, final int y, final int z) {
        BlockFace attachedFace = null;
        if (world.getBlockAt(x, y, z).getData() == 0x1)
            attachedFace = BlockFace.WEST;
        else if (world.getBlockAt(x, y, z).getData() == 0x2)
            attachedFace = BlockFace.NORTH;
        else if (world.getBlockAt(x, y, z).getData() == 0x4)
            attachedFace = BlockFace.EAST;
        else if (world.getBlockAt(x, y, z).getData() == 0x8)
            attachedFace = BlockFace.SOUTH;
        if (attachedFace == null)
            return true;
        return isSolid(getType(world.getBlockAt(x, y, z).getRelative(attachedFace).getTypeId()));
    }

    public static boolean isFood(final ItemStack item) {
        if (item == null)
            return false;
        return item.getType().isEdible();
    }

    public static boolean isInGround(final int fromType) {
        return (fromType & INGROUND) == INGROUND;
    }

    private static boolean isLadder(final int value) {
        return (value & LADDER) == LADDER;
    }

    public static boolean isLiquid(final int value) {
        return (value & LIQUID) == LIQUID;
    }

    private static boolean isNonSolid(final int value) {
        return (value & NONSOLID) == NONSOLID;
    }

    public static boolean isOnGround(final int fromType) {
        return (fromType & ONGROUND) == ONGROUND;
    }

    public static boolean isSolid(final int value) {
        return (value & SOLID) == SOLID;
    }

    public static boolean isVine(final int value) {
        return (value & VINE) == VINE;
    }

    public static boolean isWeb(final int value) {
        return (value & WEB) == WEB;
    }

    /**
     * Personal Rounding function to determine if a player is still touching a
     * block or not
     * 
     * @param d1
     * @return
     */
    private static int lowerBorder(final double d1) {

        final double floor = Math.floor(d1);

        if (floor + magic <= d1)
            return (int) floor;
        else
            return (int) (floor - 1);
    }

    /**
     * Check if a player is close enough to a target, based on his eye location
     * 
     * @param player
     * @param targetX
     * @param targetY
     * @param targetZ
     * @param limit
     * @return
     */
    public static double reachCheck(final NCPPlayer player, final double targetX, final double targetY,
            final double targetZ, final double limit) {

        final Location eyes = player.getBukkitPlayer().getEyeLocation();

        final double distance = Math.sqrt(Math.pow(eyes.getX() - targetX, 2) + Math.pow(eyes.getY() - targetY, 2)
                + Math.pow(eyes.getZ() - targetZ, 2));

        return Math.max(distance - limit, 0.0D);
    }

    /**
     * Personal Rounding function to determine if a player is still touching a
     * block or not
     * 
     * @param d1
     * @return
     */
    private static int upperBorder(final double d1) {

        final double floor = Math.floor(d1);

        if (floor + magic2 < d1)
            return (int) (floor + 1);
        else
            return (int) floor;
    }
}
