package net.samagames.dimensions.listeners;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.Dimensions;
import net.samagames.dimensions.arena.APlayer;
import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.arena.DimensionsManager;
import net.samagames.dimensions.utils.Metadatas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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

/**
 * Created by zyuiop on 26/09/14.
 */
public class DamageListener implements Listener {

    protected Dimensions plugin;

    protected ICoherenceMachine coherenceMachine;

    public DamageListener(Dimensions plugin) {
        this.plugin = plugin;

        coherenceMachine = SamaGamesAPI.get().getGameManager().getCoherenceMachine();
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        final Arena arena = plugin.getArena();
        Player p = event.getEntity();
        p.setHealth(p.getMaxHealth());

        final List<ItemStack> remove = event.getDrops().stream().filter(stack -> stack.getType() == Material.COMPASS || stack.getType() == Material.EYE_OF_ENDER).collect(Collectors.toList());
        for (final ItemStack rem : remove) {
            event.getDrops().remove(rem);
        }

        event.getDrops().remove(Dimensions.getCompass());
        event.getDrops().remove(Dimensions.getSwap());

        if (!arena.hasPlayer(p)) {
            return;
        }
/*
        EntityDamageEvent last = event.getEntity().getLastDamageCause();
        if (last instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent obj = (EntityDamageByEntityEvent)last;
            if (obj.getDamager() instanceof Player) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    arena.addCoins((Player) obj.getDamager(), 2, "Un joueur tué !");
                    arena.increaseStat(obj.getDamager().getUniqueId(), "kills", 1);

                    if (arena.getTargetedBy(event.getEntity().getUniqueId()).contains(obj.getDamager().getUniqueId())) {
                        ((Player) obj.getDamager()).sendMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + " Vous avez tué votre cible \\o/");
                        arena.addCoins((Player) obj.getDamager(), 5, "Objectif réussi !");
                    }
                    //((Player)obj.getDamager()).sendMessage(ChatColor.GOLD + "Vous gagnez " + montant + " coins " + ChatColor.AQUA + "(Un joueur tué !)");
                });
            } else if (obj.getDamager() instanceof Arrow) {
                final Arrow damager = (Arrow) obj.getDamager();
                final LivingEntity shooter = (LivingEntity) damager.getShooter();
                if (shooter instanceof Player) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

                        arena.addCoins((Player) shooter, 2, "Un joueur tué !");
                        arena.increaseStat(shooter.getUniqueId(), "kills", 1);
                        if (arena.getTargetedBy(event.getEntity().getUniqueId()).contains(shooter.getUniqueId())) {
                            ((Player) shooter).sendMessage(coherenceMachine.getGameTag() + ChatColor.GOLD+" Vous avez tué votre cible \\o/");
                            arena.addCoins((Player) shooter, 5, "Objectif réussi !");
                        }
                        //((Player)obj.getDamager()).sendMessage(ChatColor.GOLD + "Vous gagnez " + montant + " coins " + ChatColor.AQUA + "(Un joueur tué !)");
                    });
                }
            }
        }*/

        event.setDeathMessage("");
        this.playerDie(event.getEntity());

        Bukkit.getScheduler().runTaskLater(plugin, () -> arena.stumpPlayer(event.getEntity(), false), 10L);
    }

    private void playerDie(final Player dead) {
        final Object OlastDamager = Metadatas.getMetadata(dead, "lastDamager");
        if (OlastDamager == null) {
            Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.RED + dead.getDisplayName() + " " + ChatColor.RED + "a été éliminé sans aide extérieure.");
        }
        else {
            final APlayer pplayer = (APlayer)OlastDamager;
            final UUID lastDamager = pplayer.getUUID();
            if (!this.plugin.getArena().isPlaying(pplayer.getPlayerIfOnline())) {
                Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.RED + dead.getDisplayName() + " " + ChatColor.RED + "a été éliminé.");
            }
            else {
                final Player killer = pplayer.getPlayerIfOnline();
                if (killer == null) {
                    Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.RED + dead.getDisplayName() + " " + ChatColor.RED + "a été éliminé.");
                    return;
                }
                Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.RED + dead.getDisplayName() + " " + ChatColor.RED + "a été tué par " + killer.getDisplayName() + ".");
                pplayer.addKill();
                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    Arena arena = plugin.getArena();
                    arena.addCoins(pplayer.getPlayerIfOnline(), 20, "Un joueur tué !");
                    arena.increaseStat(lastDamager, "kills", 1);
                    if (DamageListener.this.plugin.getArena().getTargetedBy(dead.getUniqueId()).contains(lastDamager) && !DamageListener.this.plugin.getArena().isDeathmatch()) {
                        killer.sendMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + " Vous avez tué votre cible \\o/");
                        arena.addCoins(pplayer.getPlayerIfOnline(), 40, "Objectif réussi !");
                    }
                    if (killer.getHealth() >= 1.0 && DamageListener.this.plugin.getArena().isPlaying(pplayer.getPlayerIfOnline())) {
                        final Integer healAtKill = pplayer.getHealAtKill();
                        if (healAtKill != null) {
                            double health = killer.getHealth() + healAtKill;
                            if (health > killer.getMaxHealth()) {
                                health = killer.getMaxHealth();
                            }
                            killer.setHealth(health);
                        }
                        final Integer strenghtAtKill = pplayer.getStrengthAtKill();
                        if (strenghtAtKill != null) {
                            Bukkit.getScheduler().runTask(plugin, () -> killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * strenghtAtKill, 0)));
                        }
                    }
                });
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent ev) {
        if (ev.getCause() == EntityDamageEvent.DamageCause.WITHER)
            ev.setCancelled(true);


        if (ev.getCause() == EntityDamageEvent.DamageCause.POISON
                || ev.getCause() == EntityDamageEvent.DamageCause.MAGIC
                || ev.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || ev.getCause() == EntityDamageEvent.DamageCause.FIRE)
            ev.setCancelled(true);

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent ev) {
        ev.setRespawnLocation(plugin.getArena().getWaitLocation());
        plugin.getArena().respawnSpec(ev.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (this.plugin.getArena().getStatus() == Status.REBOOTING) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            if (event.getEntity() instanceof Player) {
                if (event.getDamager() instanceof Projectile) {
                    Arena arena = plugin.getArena();
                    if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                        if (! arena.isPlaying((Player) ((Projectile) event.getDamager()).getShooter()) || ! arena.isPlaying((Player) event.getEntity()) || ! arena.isPVPEnabled()) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                    this.damageByPlayer((Player)event.getEntity(), (Player)((Projectile)event.getDamager()).getShooter());
                }
            }
        }

        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Player) {
                Arena arena = plugin.getArena();
                if (!arena.isPlaying((Player) event.getDamager()) || !arena.isPlaying((Player) event.getEntity()) || !arena.isPVPEnabled()) {
                    event.setCancelled(true);
                    return;
                }
                this.damageByPlayer((Player)event.getEntity(), (Player)event.getDamager());
            }
        }
    }

    private void damageByPlayer(final Player damaged, final Player damager) {
        APlayer aPlayer = plugin.getArena().getPlayer(damager.getUniqueId());
        Metadatas.setMetadata(damaged, "lastDamager", aPlayer);
        final Integer healAtStrike = aPlayer.getHealAtStrike();
        if (healAtStrike != null) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                if (damager.getHealth() < 1.0 || !DamageListener.this.plugin.getArena().isPlaying(damager)) {
                    return;
                }
                final Random random = new Random();
                if (random.nextInt(100) <= healAtStrike) {
                    double h = damager.getHealth() + 2.0;
                    if (h > damager.getMaxHealth()) {
                        h = damager.getMaxHealth();
                    }
                    damager.setHealth(h);
                    damager.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + "Une fée vous a restauré un coeur !");
                }
            }, 5L);
        }
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Arena arena = plugin.getArena();
            if (arena.isPlaying((Player) event.getEntity())) {
                if (arena.getDimensionsManager().getDimension((Player) event.getEntity()).equals(DimensionsManager.Dimension.PARALLEL))
                    event.setCancelled(event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED));
            }
        }
    }

    @EventHandler
    public void onProjectileHit(final EntityDamageByEntityEvent event) {
        final Entity entityDamager = event.getDamager();
        final Entity entityDamaged = event.getEntity();
        if (!(entityDamager instanceof Arrow) || !(entityDamaged instanceof Player) || ((Arrow)entityDamager).getShooter() instanceof Player) {
            // FIXME: WHAT IS THIS SHIT?!
        }
    }
}
