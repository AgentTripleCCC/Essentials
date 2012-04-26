package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.players.informations.Permissions;

/**
 * Configurations specific for the "Fight" checks
 * Every world gets one of these assigned to it, or if a world doesn't get
 * it's own, it will use the "global" version
 * 
 */
public class FightConfig extends CheckConfig {

    public final boolean    directionCheck;
    public final double     directionPrecision;
    public final ActionList directionActions;
    public final long       directionPenaltyTime;

    public final boolean    noswingCheck;
    public final ActionList noswingActions;

    public final boolean    reachCheck;
    public final double     reachLimit;
    public final long       reachPenaltyTime;
    public final ActionList reachActions;

    public final int        speedAttackLimit;
    public final ActionList speedActions;
    public final boolean    speedCheck;

    public final boolean    godmodeCheck;
    public final ActionList godmodeActions;

    public final boolean    instanthealCheck;
    public final ActionList instanthealActions;

    public final boolean    knockbackCheck;
    public final long       knockbackInterval;
    public final ActionList knockbackActions;

    public final boolean    criticalCheck;
    public final double     criticalFallDistance;
    public final double     criticalVelocity;
    public final ActionList criticalActions;

    public FightConfig(final ConfigFile data) {

        directionCheck = data.getBoolean(ConfPaths.FIGHT_DIRECTION_CHECK);
        directionPrecision = data.getInt(ConfPaths.FIGHT_DIRECTION_PRECISION) / 100D;
        directionPenaltyTime = data.getInt(ConfPaths.FIGHT_DIRECTION_PENALTYTIME);
        directionActions = data.getActionList(ConfPaths.FIGHT_DIRECTION_ACTIONS, Permissions.FIGHT_DIRECTION);

        noswingCheck = data.getBoolean(ConfPaths.FIGHT_NOSWING_CHECK);
        noswingActions = data.getActionList(ConfPaths.FIGHT_NOSWING_ACTIONS, Permissions.FIGHT_NOSWING);

        reachCheck = data.getBoolean(ConfPaths.FIGHT_REACH_CHECK);
        reachLimit = data.getInt(ConfPaths.FIGHT_REACH_LIMIT) / 100D;
        reachPenaltyTime = data.getInt(ConfPaths.FIGHT_REACH_PENALTYTIME);
        reachActions = data.getActionList(ConfPaths.FIGHT_REACH_ACTIONS, Permissions.FIGHT_REACH);

        speedCheck = data.getBoolean(ConfPaths.FIGHT_SPEED_CHECK);
        speedActions = data.getActionList(ConfPaths.FIGHT_SPEED_ACTIONS, Permissions.FIGHT_SPEED);
        speedAttackLimit = data.getInt(ConfPaths.FIGHT_SPEED_ATTACKLIMIT);

        godmodeCheck = data.getBoolean(ConfPaths.FIGHT_GODMODE_CHECK);
        godmodeActions = data.getActionList(ConfPaths.FIGHT_GODMODE_ACTIONS, Permissions.FIGHT_GODMODE);

        instanthealCheck = data.getBoolean(ConfPaths.FIGHT_INSTANTHEAL_CHECK);
        instanthealActions = data.getActionList(ConfPaths.FIGHT_INSTANTHEAL_ACTIONS, Permissions.FIGHT_INSTANTHEAL);

        knockbackCheck = data.getBoolean(ConfPaths.FIGHT_KNOCKBACK_CHECK);
        knockbackInterval = data.getLong(ConfPaths.FIGHT_KNOCKBACK_INTERVAL);
        knockbackActions = data.getActionList(ConfPaths.FIGHT_KNOCKBACK_ACTIONS, Permissions.FIGHT_KNOCKBACK);

        criticalCheck = data.getBoolean(ConfPaths.FIGHT_CRITICAL_CHECK);
        criticalFallDistance = data.getDouble(ConfPaths.FIGHT_CRITICAL_FALLDISTANCE);
        criticalVelocity = data.getDouble(ConfPaths.FIGHT_CRITICAL_VELOCITY);
        criticalActions = data.getActionList(ConfPaths.FIGHT_CRITICAL_ACTIONS, Permissions.FIGHT_CRITICAL);
    }
}
