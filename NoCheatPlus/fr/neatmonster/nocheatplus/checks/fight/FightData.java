package fr.neatmonster.nocheatplus.checks.fight;

import net.minecraft.server.Entity;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckData;

/**
 * Player specific data for the fight checks
 * 
 */
public class FightData extends CheckData {

    // Keep track of the violation levels of the checks
    public double  directionVL;
    public double  noswingVL;
    public double  reachVL;
    public int     speedVL;
    public double  godmodeVL;
    public double  instanthealVL;
    public double  knockbackVL;
    public double  criticalVL;

    // For checks that have penalty time
    public long    directionLastViolationTime;
    public long    reachLastViolationTime;

    // godmode check needs to know these
    public long    godmodeLastDamageTime;
    public int     godmodeLastAge;
    public int     godmodeBuffer     = 40;

    // last time player regenerated health by satiation
    public long    instanthealLastRegenTime;

    // three seconds buffer to smooth out lag
    public long    instanthealBuffer = 3000;

    // While handling an event, use this to keep the attacked entity
    public Entity  damagee;

    // Remember the player who attacked the entity
    public Player  damager;

    // The player swung his arm
    public boolean armswung          = true;

    // For some reason the next event should be ignored
    public boolean skipNext          = false;

    // Keep track of time and amount of attacks
    public long    speedTime;
    public int     speedAttackCount;

    // Remember when the player has toggled his sprint mode
    public long    sprint            = 0L;

}
