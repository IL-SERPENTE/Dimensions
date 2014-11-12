package net.zyuiop.parallelspvp.arena;

import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.types.GameArena;
import net.samagames.utils.IconMenu;
import net.zyuiop.MasterBundle.StarsManager;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.listeners.NetworkListener;
import net.zyuiop.parallelspvp.tasks.BeginCountdown;
import net.zyuiop.parallelspvp.tasks.Deathmatch;
import net.zyuiop.parallelspvp.tasks.PVPEnable;
import net.zyuiop.parallelspvp.tasks.RandomEffects;
import net.zyuiop.parallelspvp.utils.Colors;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class Arena implements GameArena {

    /** Variables d'arène **/
    protected int maxPlayers;
    protected int maxVIP;
    protected String mapName;
    protected UUID arenaId;
    protected boolean isFamous;
    protected Status status = Status.Idle;

    protected ArrayList<ParallelsPlayer> players = new ArrayList<>();
    protected ArrayList<ParallelsPlayer> spectators = new ArrayList<>();

    protected ParallelsPVP parallelsPVP;
    protected BukkitTask countdown;
    protected ArrayList<Location> spawns = new ArrayList<Location>();
    protected ArrayList<Location> deathmatchSpawns = new ArrayList<Location>();
    protected Location waitLocation;
    protected DimensionsManager dimensionsManager;
    protected boolean isDeathmatch = false;
    protected BukkitTask dmCount = null;
    protected BukkitTask pvpCount = null;
    protected ArrayList<Material> allowed = new ArrayList<>();
    protected Scoreboard scoreboard = null;
    protected BukkitTask randomEffects = null;

    public int getTotalMaxSlots() {
        return spawns.size();
    }


    public boolean canBreak(Material madeOf) {
        return allowed.contains(madeOf);
    }

    public Arena(ParallelsPVP parallelsPVP, YamlConfiguration arenaData, int maxPlayers, int maxVIP, UUID arenaId) {
        this.maxPlayers = maxPlayers;
        this.maxVIP = maxVIP;
        this.arenaId = arenaId;
        this.parallelsPVP = parallelsPVP;

        dimensionsManager = new DimensionsManager(this, arenaData.getInt("dimension-diff"), arenaData.getString("overworld-name"), arenaData.getString("hard-name"));
        waitLocation = null;
        World world = Bukkit.getWorlds().get(0);
        String[] locparts = arenaData.getString("wait-spawn", " ").split(";");
        if (locparts.length < 3) {
            Bukkit.getLogger().severe("ERREUR : coordonnées spawn invalides (not enough params) dans le fichier d'arène.");
        }

        if (locparts.length == 5) {
            waitLocation = new Location(world, Double.parseDouble(locparts[0]), Double.parseDouble(locparts[1]), Double.parseDouble(locparts[2]), Float.parseFloat(locparts[3]), Float.parseFloat(locparts[4]));
        } else {
            waitLocation = new Location(world, Double.parseDouble(locparts[0]), Double.parseDouble(locparts[1]), Double.parseDouble(locparts[2]));
        }

        this.mapName = arenaData.getString("map-name");

        int minPlayers = arenaData.getInt("min-players");

        for (Object material : arenaData.getList("allowed")) {
            try {
                if (material instanceof String) {
                    Material mat = Material.valueOf((String) material);
                    if (!allowed.contains(mat))
                        allowed.add(mat);
                } else if (material instanceof  Integer) {
                    Material mat = Material.getMaterial((Integer)material);
                    if (!allowed.contains(mat))
                        allowed.add(mat);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Object spawn : arenaData.getList("spawns")) {
            if (!(spawn instanceof String)) {
                Bukkit.getLogger().severe("ERREUR : coordonnées invalides (not a string) dans le fichier d'arène.");
                continue;
            }

            String spawnString = (String) spawn;

            String[] parts = spawnString.split(";");
            if (parts.length < 3) {
                Bukkit.getLogger().severe("ERREUR : coordonnées invalides (not enough params) dans le fichier d'arène.");
                continue;
            }

            Location loc = null;
            if (parts.length == 5) {
                loc = new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Float.parseFloat(parts[3]), Float.parseFloat(parts[4]));
            } else {
                loc = new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
            }

            spawns.add(loc);
        }

        for (Object spawn : arenaData.getList("deathmatchspawns")) {
            if (!(spawn instanceof String)) {
                Bukkit.getLogger().severe("ERREUR : coordonnées invalides (not a string) dans le fichier d'arène.");
                continue;
            }

            String spawnString = (String) spawn;

            String[] parts = spawnString.split(";");
            if (parts.length < 3) {
                Bukkit.getLogger().severe("ERREUR : coordonnées invalides (not enough params) dans le fichier d'arène.");
                continue;
            }

            Location loc = null;
            if (parts.length == 5) {
                loc = new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Float.parseFloat(parts[3]), Float.parseFloat(parts[4]));
            } else {
                loc = new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
            }

            deathmatchSpawns.add(loc);
        }

        if (spawns.size() < (maxPlayers + maxVIP)) {
            maxPlayers = spawns.size() - maxVIP;
            Bukkit.getLogger().severe("ATTENTION : pas assez de spawns, nombre de joueurs max réduit a "+maxPlayers+" (+ "+maxVIP+" slots VIP)");
        }

        if (minPlayers > (maxPlayers + maxVIP)) {
            minPlayers = ((maxPlayers + maxVIP) / 100) * 50;
        }


        countdown = Bukkit.getScheduler().runTaskTimerAsynchronously(parallelsPVP, new BeginCountdown(this, maxPlayers + this.maxVIP, minPlayers), 0, 20L);



        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Bukkit.getPluginManager().registerEvents(new NetworkListener(this), parallelsPVP);
        status = Status.Available;

    }

    public void updateStatus(Status newStatus) {
        this.status = newStatus;
        GameAPI.getManager().refreshArena(this);
    }

    public DimensionsManager getDimensionsManager() {
        return dimensionsManager;
    }


    /*
     Methods related to game management
     */

    protected boolean isPVPEnabled = false;

    public void start() {
        try {
            countdown.cancel();
            countdown = null;
            Bukkit.getLogger().info("Cancelled thread");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.updateStatus(Status.InGame);

        Bukkit.broadcastMessage(ParallelsPVP.pluginTAG + ChatColor.GOLD + " La partie commence. Bonne chance !");
        Bukkit.broadcastMessage(ParallelsPVP.pluginTAG + ChatColor.GOLD + " Le PVP sera activé dans 3 minutes.");
        this.pvpCount = Bukkit.getScheduler().runTaskTimer(parallelsPVP, new PVPEnable(this), 0L, 20L);

        ArrayList<ParallelsPlayer> remove = new ArrayList<ParallelsPlayer>();
        Iterator<ParallelsPlayer> iterator = players.iterator();

        scoreboard.registerNewObjective("vie", "health").setDisplaySlot(DisplaySlot.BELOW_NAME);
        scoreboard.getObjective("vie").setDisplayName(ChatColor.RED+"♥");

        Collections.shuffle(this.spawns);
        for (Location spawn : this.spawns) {
            if (!iterator.hasNext())
                break;
            ParallelsPlayer vplayer = iterator.next();
            Player player = Bukkit.getPlayer(vplayer.getPlayerID());
            resetPlayer(player);
            if (player == null) {
                remove.add(vplayer);
                continue;
            } else {
                player.teleport(spawn);
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().addItem(ParallelsPVP.getCompass());
                player.getInventory().addItem(ParallelsPVP.getSwap());
                scoreboard.getObjective("vie").getScore(player.getName()).setScore((int)player.getHealth());
                StatsApi.increaseStat(player, "parallelspvp", "played", 1);
            }
        }

        while (iterator.hasNext()) {
            try {
                GameAPI.kickPlayer(iterator.next().getPlayer());
            } catch (Exception e) {

            }
        }

        for (ParallelsPlayer player : remove) {
            players.remove(player);
        }

        RandomEffects eff = new RandomEffects(this);
        this.randomEffects = Bukkit.getScheduler().runTaskTimerAsynchronously(parallelsPVP, eff, 0L, 20L);
    }

    public void enablePVP() {
        this.isPVPEnabled = true;
        if (this.pvpCount != null)
            this.pvpCount.cancel();
        this.pvpCount = null;
        Bukkit.broadcastMessage(ParallelsPVP.pluginTAG+ChatColor.GOLD+" Le PVP est activé ! C'est l'heure du d-d-d-duel !");
    }

    public void startDeathMatch() {
        this.dmCount.cancel();
        this.isDeathmatch = true;
        ArrayList<Player> offline = new ArrayList<Player>();
        Iterator<Location> spawns = this.deathmatchSpawns.iterator();

        if (!isPVPEnabled())
            enablePVP();

        for (ParallelsPlayer ap : this.players) {
            Player player = Bukkit.getPlayer(ap.getPlayerID());
            if (!spawns.hasNext()) {
                Bukkit.broadcastMessage(ChatColor.RED+"Une erreur s'est produite, tous les joueurs ne peuvent pas être transférés en deathmatch.");
                break;
            } else {
                Location spawn = spawns.next();
                player.teleport(spawn);
            }
        }
    }

    public void broadcastSound(Sound sound) {
        for (ParallelsPlayer pl : this.players) {
            Player p = pl.getPlayer();
            if (p != null)
                p.playSound(p.getLocation(), sound, 1, 1);
        }
    }

    public void finish(FinishReason reason) {
        if (randomEffects != null)
            randomEffects.cancel();

        if (reason.equals(FinishReason.WIN)) {
            if (players.size() == 0) {
                resetArena();
                return;
            }

            if (this.dmCount != null)
                this.dmCount.cancel();

            if (players.size() > 1)
                return;

            ParallelsPlayer winner = players.get(0);
            final Player player = Bukkit.getPlayer(winner.getPlayerID());
            if (player == null) {
                resetArena();
                return;
            }

            StatsApi.increaseStat(player, "parallelspvp", "wins", 1);

            Bukkit.broadcastMessage(parallelsPVP.pluginTAG+ChatColor.GREEN+ChatColor.MAGIC+"aaa"+ChatColor.GOLD+" Victoire ! "+ChatColor.GREEN+ChatColor.MAGIC+"aaa"+ChatColor.GOLD+" Bravo a "+ChatColor.LIGHT_PURPLE+player.getName()+ChatColor.GOLD+" !");

            Bukkit.getScheduler().runTaskAsynchronously(parallelsPVP, new Runnable() {
                @Override
                public void run() {
                    CoinsManager.creditJoueur(player.getUniqueId(), 20, true, true, "Victoire !");
                    StarsManager.creditJoueur(player.getUniqueId(), 1, "Victoire !");
                }
            });

            // Feux d'artifice swag
            final int nb = 20;
            Bukkit.getScheduler().scheduleSyncRepeatingTask(parallelsPVP, new Runnable() {
                int compteur = 0;

                public void run() {

                    if (compteur >= nb || player == null) {
                        return;
                    }

                    //Spawn the Firework, get the FireworkMeta.
                    Firework fw = (Firework) player.getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();

                    //Our random generator
                    Random r = new Random();

                    //Get the type
                    int rt = r.nextInt(4) + 1;
                    FireworkEffect.Type type = FireworkEffect.Type.BALL;
                    if (rt == 1) type = FireworkEffect.Type.BALL;
                    if (rt == 2) type = FireworkEffect.Type.BALL_LARGE;
                    if (rt == 3) type = FireworkEffect.Type.BURST;
                    if (rt == 4) type = FireworkEffect.Type.CREEPER;
                    if (rt == 5) type = FireworkEffect.Type.STAR;

                    //Get our random colours
                    int r1i = r.nextInt(17) + 1;
                    int r2i = r.nextInt(17) + 1;
                    Color c1 = Colors.getColor(r1i);
                    Color c2 = Colors.getColor(r2i);

                    //Create our effect with this
                    FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

                    //Then apply the effect to the meta
                    fwm.addEffect(effect);

                    //Generate some random power and set it
                    int rp = r.nextInt(2) + 1;
                    fwm.setPower(rp);

                    //Then apply this to our rocket
                    fw.setFireworkMeta(fwm);

                    compteur++;
                }

            }, 5L, 5L);

            Bukkit.getScheduler().runTaskLater(parallelsPVP, new Runnable() {
                public void run() {
                    resetArena();
                }
            }, 15*20L);
        }
    }

    public void resetArena() {
        this.updateStatus(Status.Stopping);

        for (ParallelsPlayer player : this.players) {
            if (player.getPlayer() != null) {
                parallelsPVP.kickPlayer(player.getPlayer());
            }
        }

        for (ParallelsPlayer player : this.spectators) {
            if (player.getPlayer() != null) {
                parallelsPVP.kickPlayer(player.getPlayer());
            }
        }

        Bukkit.getScheduler().runTaskLater(parallelsPVP, new Runnable() {
            public void run() {
                Bukkit.getServer().shutdown();
            }
        }, 5*20L);
    }

    public void joinSpectators(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 1));
        p.setGameMode(GameMode.CREATIVE);
        p.teleport(this.waitLocation);
        p.getInventory().clear();

        p.sendMessage(ChatColor.GOLD+"Vous rejoignez les spectateurs.");
        this.spectators.add(new ParallelsPlayer(p));
        for (ParallelsPlayer pl : this.players) {
            Player target = Bukkit.getPlayer(pl.getPlayerID());
            target.hidePlayer(p);
        }
    }

    public void respawnSpec(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 1));
        p.setGameMode(GameMode.CREATIVE);
        p.teleport(this.waitLocation);
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.MAGIC + "|||" + ChatColor.GOLD+" Téléportation " + ChatColor.GREEN + ChatColor.MAGIC + "|||");
        compass.setItemMeta(meta);
        p.getInventory().setItem(4, compass);
    }

    public Location getWaitLocation() {
        return waitLocation;
    }

    public boolean isStarted() {
        return status == Status.InGame;
    }

    public void logout(UUID player) {
        ParallelsPlayer arPlayer = new ParallelsPlayer(player);
        if (!isStarted()) {
            players.remove(arPlayer);
            GameAPI.getManager().refreshArena(this);
            return;
        }

        if (spectators.contains(arPlayer))
            spectators.remove(arPlayer);
        else if (players.contains(arPlayer))
            stumpPlayer(player, true);
        GameAPI.getManager().refreshArena(this);
    }

    public void stumpPlayer(UUID player, boolean logout) {
        ParallelsPlayer arPlayer = new ParallelsPlayer(player);

        this.players.remove(arPlayer);
        int left = this.players.size();
        boolean isWon = (left <= 1);

        Player rPlayer = arPlayer.getPlayer();

        StatsApi.increaseStat(player, "parallelspvp", "stumped", 1);
        if (logout && rPlayer != null) {
            Bukkit.broadcastMessage(ParallelsPVP.pluginTAG+ChatColor.RED+" "+rPlayer.getName()+" s'est déconnecté.");
        } else if (rPlayer != null) {
            Bukkit.broadcastMessage(ParallelsPVP.pluginTAG+ChatColor.RED+" "+rPlayer.getName()+" a été éliminé.");
            joinSpectators(rPlayer);
        }

        GameAPI.getManager().refreshArena(this);

        if (!isWon) {
            Bukkit.broadcastMessage(ChatColor.YELLOW+ "Il reste encore "+ChatColor.AQUA+left+ChatColor.YELLOW+" joueurs en vie.");
            if (left <= this.deathmatchSpawns.size() && !this.isDeathmatch && this.dmCount == null) {
                Deathmatch countdown = new Deathmatch(this);
                dmCount = Bukkit.getScheduler().runTaskTimer(parallelsPVP, countdown, 0L, 20L);
            }
        } else {
            finish(FinishReason.WIN);
        }

    }

    @Override
    public int countGamePlayers() {
        return players.size();
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public int getTotalMaxPlayers() {
        return maxPlayers + maxVIP;
    }

    @Override
    public int getVIPSlots() {
        return maxVIP;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public UUID getUUID() {
        return arenaId;
    }

    @Override
    public boolean hasPlayer(UUID player) {
        return players.contains(new ParallelsPlayer(player)) || spectators.contains(new ParallelsPlayer(player));
    }

    @Override
    public String getMapName() {
        return mapName;
    }

    @Override
    public boolean isFamous() {
        return isFamous;
    }

    public enum FinishReason {
        END_OF_TIME,
        WIN,
        NO_PLAYERS;
    }

    public boolean isPlaying(ParallelsPlayer player) {
        return players.contains(player);
    }

    public boolean isPVPEnabled() {
        return isPVPEnabled;
    }

    public void resetPlayer(Player p) {
        p.setHealth(20.0);
        p.setMaxHealth(20.0);
        p.setSaturation(20);
        p.getActivePotionEffects().clear();
        p.setAllowFlight(false);
        p.setGameMode(GameMode.ADVENTURE);
        p.setLevel(0);
        p.setExp(0);
        p.setFlying(false);
        p.setHealthScaled(false);
        p.setFireTicks(0);
        p.getInventory().clear();
        p.getInventory().setHelmet(new ItemStack(Material.AIR));
        p.getInventory().setChestplate(new ItemStack(Material.AIR));
        p.getInventory().setLeggings(new ItemStack(Material.AIR));
        p.getInventory().setBoots(new ItemStack(Material.AIR));
        p.setScoreboard(this.scoreboard);
    }

    public int countPlayers() {
        return players.size();
    }

    public void addPlayer(ParallelsPlayer arplayer) {
        if (spectators.contains(arplayer))
            spectators.remove(arplayer);

        if (!players.contains(arplayer))
            players.add(arplayer);
    }

    public void tpMenu(final Player player) {
        double nb = players.size();
        double nSlots = Math.ceil(nb / 9) * 9;
        ParallelsPVP.menuManager.create(player, ChatColor.GOLD + "Téléportation !", (int) nSlots, new IconMenu.OptionClickEventHandler() {
            @Override
            public void onOptionClick(IconMenu.OptionClickEvent event) {
                if (event.getName().equals("close")) {
                    event.setWillClose(true);
                    event.setWillDestroy(true);
                } else {
                    UUID user = UUID.fromString(event.getName());
                    Player player1 = new ParallelsPlayer(user).getPlayer();
                    if (player1 != null) {
                        player.teleport(player1);
                        player.sendMessage(ChatColor.GREEN + "Téléportation !");
                    } else {
                        player.sendMessage(ChatColor.RED + "Le joueur n'est plus connecté.");
                    }
                    event.setWillClose(true);
                    event.setWillDestroy(true);
                }
            }
        });

        int slot = 0;
        for (ParallelsPlayer p : players) {
            DimensionsManager.Dimension dimension = dimensionsManager.dimensions.get(p.getPlayerID());
            String dimName = ChatColor.DARK_GREEN + dimensionsManager.overworldName;
            if (dimension != null && dimension == DimensionsManager.Dimension.PARALLEL)
                dimName = ChatColor.DARK_RED + dimensionsManager.hardName;


            if (p.getPlayer() == null)
                continue;

            String name = p.getPlayer().getDisplayName();

            ParallelsPVP.menuManager.setOption(player, slot, new ItemStack(Material.STONE_SWORD), name, p.getPlayerID().toString(), new String[]{
                    ChatColor.AQUA + ""+ ((int) Math.ceil(p.getPlayer().getHealth())) + ChatColor.GOLD + " points de vie",
                    ChatColor.GOLD + "Dimension : " + dimName}, false);

            slot++;
        }
        ParallelsPVP.menuManager.show(player);
    }
}
