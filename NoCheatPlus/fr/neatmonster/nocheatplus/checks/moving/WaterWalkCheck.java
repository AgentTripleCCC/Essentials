package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.utilities.locations.PreciseLocation;

/**
 * This check is used to verify that players aren't walking on water
 * 
 */
public class WaterWalkCheck extends MovingCheck {

    public WaterWalkCheck() {
        super("waterwalk");
    }

    public PreciseLocation check(final NCPPlayer player, final Object... args) {
        final MovingConfig cc = getConfig(player);
        final MovingData data = getData(player);

        // Some shortcuts:
        final PreciseLocation to = data.to;
        final PreciseLocation from = data.from;
        final PreciseLocation up = new PreciseLocation();
        up.x = to.x;
        up.y = to.y + 1;
        up.z = to.z;

        final PreciseLocation toAbove = new PreciseLocation();
        toAbove.x = to.x;
        toAbove.y = to.y + 2;
        toAbove.z = to.z;
        final PreciseLocation fromAbove = new PreciseLocation();
        fromAbove.x = from.x;
        fromAbove.y = from.y + 2;
        fromAbove.z = from.z;

        // To know if a player "is in water" is useful
        final int fromType = CheckUtils.evaluateLocation(player.getWorld(), from);
        final int toType = CheckUtils.evaluateLocation(player.getWorld(), to);
        final int upType = CheckUtils.evaluateLocation(player.getWorld(), up);

        final boolean fromLiquid = CheckUtils.isLiquid(fromType);
        final boolean toLiquid = CheckUtils.isLiquid(toType);
        final boolean upLiquid = CheckUtils.isLiquid(upType);

        final boolean toAboveSolid = CheckUtils.isSolid(CheckUtils.getType(new Location(player.getWorld(), toAbove.x,
                toAbove.y, toAbove.z).getBlock().getTypeId()));
        final boolean fromAboveSolid = CheckUtils.isSolid(CheckUtils.getType(new Location(player.getWorld(),
                fromAbove.x, fromAbove.y, fromAbove.z).getBlock().getTypeId()));
        boolean aboveSolid = toAboveSolid || fromAboveSolid;
        final boolean save = aboveSolid;
        for (final boolean hadBlocksAbove : data.hadBlocksAbove)
            if (hadBlocksAbove)
                aboveSolid = true;
        data.rotateWaterWalkData(save);

        final Block fromBlock = new Location(player.getWorld(), from.x, from.y, from.z).getBlock();

        // Handle the issue with water streams
        boolean waterStreamsFix = false;
        if (fromBlock.getType() == Material.STATIONARY_WATER || fromBlock.getType() == Material.STATIONARY_LAVA
                || (fromBlock.getType() == Material.WATER || fromBlock.getType() == Material.LAVA)
                && fromBlock.getData() == 0x0)
            waterStreamsFix = true;

        // Handle the issue with slabs/stairs
        boolean slabsStairsFix = false;
        for (final BlockFace blockFace : BlockFace.values()) {
            final Material material = fromBlock.getRelative(blockFace).getType();
            if (material == Material.STEP || material == Material.WOOD_STAIRS
                    || material == Material.COBBLESTONE_STAIRS || material == Material.BRICK_STAIRS
                    || material == Material.SMOOTH_STAIRS || material == Material.NETHER_BRICK_STAIRS)
                slabsStairsFix = true;
        }

        // Calculate some distances
        final double deltaX = Math.abs(Math.round(to.x) - to.x);
        final double deltaY = Math.abs(from.y - to.y);
        final double deltaZ = Math.abs(Math.round(to.z) - to.z);
        final double deltaWithSurface = Math.abs(to.y - Math.ceil(to.y)) + (to.y - Math.ceil(to.y) == 0 ? 1 : 0);
        final double resultXZ = (Math.abs(deltaX - 0.30D) + Math.abs(deltaZ - 0.30D)) * 100;
        final double resultY = deltaWithSurface * 100;

        PreciseLocation newToLocation = null;

        // Slowly reduce the level with each event
        data.waterWalkVL *= 0.95;

        if (!slabsStairsFix && fromLiquid && toLiquid && !upLiquid && !aboveSolid && deltaY == 0D
                && deltaWithSurface < 0.8D) {
            // If the player is trying to move while being in water
            // Increment violation counter
            data.waterWalkVL += resultY;

            incrementStatistics(player, data.statisticCategory, resultY);

            final boolean cancel = executeActions(player, cc.actions, data.waterWalkVL);

            // Was one of the actions a cancel? Then do it
            if (cancel)
                newToLocation = from;
        } else if (waterStreamsFix && fromLiquid && !toLiquid && !aboveSolid && (deltaX < 0.28D || deltaX > 0.31D)
                && (deltaZ < 0.28D || deltaZ > 0.31D)) {
            // If the player is trying to jump while being in water
            // Increment violation counter
            data.waterWalkVL += resultXZ;

            incrementStatistics(player, data.statisticCategory, resultXZ);

            final boolean cancel = executeActions(player, cc.actions, data.waterWalkVL);

            // Was one of the actions a cancel? Then do it
            if (cancel)
                newToLocation = from;
        }

        return newToLocation;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).waterWalkVL);
        else
            return super.getParameter(wildcard, player);
    }
}
