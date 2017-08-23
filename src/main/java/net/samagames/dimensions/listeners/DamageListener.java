package net.samagames.dimensions.listeners;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.Dimensions;
import net.samagames.dimensions.arena.APlayer;
import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.arena.ArenaStatisticsHelper;
import net.samagames.dimensions.arena.DimensionsManager;
import net.samagames.dimensions.utils.Metadatas;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * This file is part of Dimensions.
 *
 * Dimensions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dimensions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dimensions.  If not, see <http://www.gnu.org/licenses/>.
 */
public class DamageListener implements Listener
{
    private Dimensions plugin;
    private ICoherenceMachine coherenceMachine;

    public DamageListener(Dimensions plugin)
    {
        this.plugin = plugin;

        this.coherenceMachine = plugin.getArena().getCoherenceMachine();
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event)
    {
        final Arena arena = this.plugin.getArena();
        Player p = event.getEntity();
        p.setHealth(p.getMaxHealth());

        final List<ItemStack> remove = event.getDrops().stream().filter(stack -> stack.getType() == Material.COMPASS || stack.getType() == Material.EYE_OF_ENDER).collect(Collectors.toList());
        for (final ItemStack rem : remove)
            event.getDrops().remove(rem);

        event.getDrops().remove(Dimensions.getCompass());
        event.getDrops().remove(Dimensions.getSwap());

        if (!arena.hasPlayer(p))
            return ;

        event.setDeathMessage("");
        this.playerDie(event.getEntity());
        arena.stumpPlayer(event.getEntity());
    }

    private void playerDie(final Player dead)
    {
        final Object OlastDamager = Metadatas.getMetadata(dead, "lastDamager");
        if (OlastDamager == null)
            this.plugin.getServer().broadcastMessage(this.coherenceMachine.getGameTag() + ChatColor.RED + " " + dead.getDisplayName() + " " + ChatColor.RED + "a été éliminé sans aide extérieure.");
        else
        {
            final APlayer pplayer = (APlayer)OlastDamager;
            final UUID lastDamager = pplayer.getUUID();
            final Player killer = pplayer.getPlayerIfOnline();
            if (killer == null || !this.plugin.getArena().isPlaying(killer))
                this.plugin.getServer().broadcastMessage(this.coherenceMachine.getGameTag() + ChatColor.RED + " " + dead.getDisplayName() + " " + ChatColor.RED + "a été éliminé.");
            else
            {
                this.plugin.getServer().broadcastMessage(this.coherenceMachine.getGameTag() + ChatColor.RED + " " + dead.getDisplayName() + " " + ChatColor.RED + "a été tué par " + killer.getDisplayName() + ".");
                pplayer.addKill();
                if(pplayer.getUUID().equals(killer.getUniqueId())) //USELESS
                {
                    this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                    {
                        Arena arena = this.plugin.getArena();
                        arena.addCoins(killer, 20, "Un joueur tué !");

                        try
                        {
                            ((ArenaStatisticsHelper) SamaGamesAPI.get().getGameManager().getGameStatisticsHelper()).increaseKills(killer.getUniqueId());
                        }
                        catch (Exception ignored) {}

                        if (DamageListener.this.plugin.getArena().getTargetedBy(dead.getUniqueId()).contains(lastDamager) && !DamageListener.this.plugin.getArena().isDeathmatch())
                        {
                            killer.sendMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + " Vous avez tué votre cible \\o/");
                            arena.addCoins(killer, 40, "Objectif réussi !");
                        }
                        if (killer.getHealth() >= 1.0 && DamageListener.this.plugin.getArena().isPlaying(killer))
                        {
                            int healAtKill = pplayer.getHealAtKill();
                            if (healAtKill != 0)
                            {
                                double health = killer.getHealth() + healAtKill;
                                if (health > killer.getMaxHealth())
                                    health = killer.getMaxHealth();
                                killer.setHealth(health);
                            }
                            int strengthAtKill = pplayer.getStrengthAtKill();
                            if (strengthAtKill != 0)
                                this.plugin.getServer().getScheduler().runTask(plugin, () -> killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * strengthAtKill, 1)));
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent ev)
    {
        if (ev.getCause() == EntityDamageEvent.DamageCause.WITHER)
            ev.setCancelled(true);

        if (ev.getCause() == EntityDamageEvent.DamageCause.MAGIC && !this.plugin.getArena().isPVPEnabled())
                ev.setCancelled(true);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent ev)
    {
        ev.setRespawnLocation(this.plugin.getArena().getWaitLocation());
        this.plugin.getArena().respawnSpec(ev.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (this.plugin.getArena().getStatus() == Status.REBOOTING)
            return ;

        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE)
        {
            if (event.getEntity() instanceof Player)
            {
                if (event.getDamager() instanceof Projectile)
                {
                    Arena arena = this.plugin.getArena();
                    if (((Projectile) event.getDamager()).getShooter() instanceof Player)
                    {
                        if (!arena.isPlaying((Player) ((Projectile) event.getDamager()).getShooter())
                                || !arena.isPlaying((Player) event.getEntity()) || ! arena.isPVPEnabled())
                        {
                            event.setCancelled(true);
                            return ;
                        }
                    }
                    this.damageByPlayer((Player)event.getEntity(), (Player)((Projectile)event.getDamager()).getShooter());
                }
            }
        }

        if (event.getEntity() instanceof Player)
        {
            if (event.getDamager() instanceof Player)
            {
                Arena arena = this.plugin.getArena();
                if (!arena.isPlaying((Player) event.getDamager()) || !arena.isPlaying((Player) event.getEntity()) || !arena.isPVPEnabled())
                {
                    event.setCancelled(true);
                    return ;
                }
                this.damageByPlayer((Player)event.getEntity(), (Player)event.getDamager());
            }
        }
    }

    private void damageByPlayer(final Player damaged, final Player damager)
    {
        APlayer aPlayer = this.plugin.getArena().getPlayer(damager.getUniqueId());
        Metadatas.setMetadata(damaged, "lastDamager", aPlayer);
        int healAtStrike = aPlayer.getHealAtStrike();
        if (healAtStrike != 0)
        {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
            {
                if (damager.getHealth() < 1.0 || !DamageListener.this.plugin.getArena().isPlaying(damager))
                    return;
                final Random random = new Random();
                if (random.nextInt(100) <= healAtStrike)
                {
                    double h = damager.getHealth() + 2.0;
                    if (h > damager.getMaxHealth())
                        h = damager.getMaxHealth();
                    damager.setHealth(h);
                    damager.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + "Une fée vous a restauré un coeur !");
                }
            }, 5L);
        }
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Arena arena = this.plugin.getArena();
            if (arena.isPlaying((Player) event.getEntity()) && arena.getDimensionsManager().getDimension((Player) event.getEntity()).equals(DimensionsManager.Dimension.PARALLEL))
                event.setCancelled(event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED));
        }
    }
}
