package net.samagames.dimensions.arena;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.*;
import net.samagames.api.gui.AbstractGui;
import net.samagames.dimensions.Dimensions;
import net.samagames.dimensions.tasks.*;
import net.samagames.dimensions.utils.Colors;
import net.samagames.dimensions.utils.Utils;
import net.samagames.tools.Titles;
import net.samagames.tools.scoreboards.VObjective;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.stream.Collectors;

public class Arena extends Game<APlayer>
{

    private Dimensions plugin;

    private List<Location> spawns = new ArrayList<>();
    private List<Location> deathmatchSpawns = new ArrayList<>();

    private Location waitLocation;

    private DimensionsManager dimensionsManager;
    boolean isDeathmatch = false;
    private BukkitTask dmCount = null;
    private BukkitTask pvpCount = null;
    private List<Material> allowed = new ArrayList<>();
    private BukkitTask randomEffects = null;
    private boolean inGame = false;
    private Map<UUID, UUID> targets = new HashMap<>(); // <Utilisateur : cible>

    private BukkitTask gameTimer;
    private BukkitTask preparationTimer;

    //Scoreboards
    private boolean isPVPEnabled = false;
    private Scoreboard scoreboard;
    private VObjective objectiveTab;

    public Arena(Dimensions plugin)
    {
        super("dimensions", "Dimensions", "", APlayer.class);
        this.plugin = plugin;

        this.allowed.add(Material.TNT);
        this.allowed.add(Material.WORKBENCH);
        this.allowed.add(Material.FURNACE);
        this.allowed.add(Material.CAKE);
        this.allowed.add(Material.CAKE_BLOCK);

        loadConfig();

        this.scoreboard = this.plugin.getServer().getScoreboardManager().getNewScoreboard();

        this.objectiveTab = new VObjective("TabKills", "kills");
        this.objectiveTab.setLocation(VObjective.ObjectiveLocation.LIST);

    }

    public boolean canBreak(Material madeOf)
    {
        return this.allowed.contains(madeOf);
    }

    private void loadConfig()
    {
        IGameProperties properties = SamaGamesAPI.get().getGameManager().getGameProperties();

        this.dimensionsManager = new DimensionsManager(this,
                properties.getConfig("dimension-diff", new JsonPrimitive(1000)).getAsInt(),
                properties.getConfig("overworld-name", new JsonPrimitive("Unknown")).getAsString(),
                properties.getConfig("hard-name", new JsonPrimitive("Unknown")).getAsString());
        this.waitLocation = Utils.srt2Loc(properties.getConfig("wait-spawn", new JsonPrimitive("0;0;0;0;0")).getAsString());

        /* Variables d'arène */
        int maxPlayers = properties.getMaxSlots();

        for (JsonElement jsonElements : properties.getConfig("Blocks", new JsonArray()).getAsJsonArray())
            this.allowed.add(Material.matchMaterial(jsonElements.getAsString()));

        for (JsonElement jsonElements : properties.getConfig("Spawns", new JsonArray()).getAsJsonArray())
            this.spawns.add(Utils.srt2Loc(jsonElements.getAsString()));

        for (JsonElement jsonElements : properties.getConfig("Deathmatchspawns", new JsonArray()).getAsJsonArray())
            this.deathmatchSpawns.add(Utils.srt2Loc(jsonElements.getAsString()));

        if (this.spawns.size() < maxPlayers)
            this.plugin.getLogger().severe("ATTENTION : pas assez de spawns, nombre de joueurs max réduit a " + maxPlayers);
    }

    public DimensionsManager getDimensionsManager()
    {
        return this.dimensionsManager;
    }


    /*
     Methods related to game management
     */

    @Override
    public void handleLogin(Player player)
    {
        super.handleLogin(player);

        player.setScoreboard(this.scoreboard);

        player.setGameMode(GameMode.ADVENTURE);
        //To hide exp bar xD
        //player.setLevel(-10);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bm = (BookMeta)book.getItemMeta();
        bm.setAuthor("SamaGames");
        bm.setTitle("Règles du jeu");
        ArrayList<String> pages = new ArrayList<>();
        // Typo?
        pages.add(ChatColor.GOLD + "Bienvenue dans " + ChatColor.DARK_AQUA + "Dimensions"+ChatColor.DARK_GREEN + " ! \n\n > Sommaire : " + ChatColor.BLACK + "\n\n P.2: Principe du jeu \n P.3: Dimensions\n P.6: Fonctionnement\n\n\n" + ChatColor.BLACK+"Maps : Amalgar");
        pages.add(ChatColor.DARK_GREEN + "Principe du jeu :" + ChatColor.BLACK + "\n\nLe but du jeu est de trouver un maximum de stuff dans les coffres puis de tuer les autres joueurs afin de rester le dernier en vie.");
        pages.add(ChatColor.DARK_GREEN + "Dimensions :" + ChatColor.BLACK + "\n\nLe jeu s'organise autour de deux dimensions. Changez grâce a l'Ender Eye. \nDécouvrez dans les pages suivantes les secrets de chacune....");
        pages.add(ChatColor.DARK_RED + "Hard Dimension :" + ChatColor.BLACK + "\n\nCette dimension contient des coffres avec du meilleur stuff. Cependant, il n'y a pas de regen de vie et certains effets peuvent vous frapper aléatoirement...");
        pages.add(ChatColor.DARK_GREEN + "Overworld :" + ChatColor.BLACK + "\n\nC'est la dimension par défaut. Ici, aucun effet. Cependant, vous y trouverez moins de coffres et moins de stuff...");
        pages.add(ChatColor.DARK_GREEN + "Fonctionnement :" + ChatColor.BLACK + "\n\n" + ChatColor.GOLD + "Le stuff : \n" + ChatColor.BLACK + "Pendant 3 minutes, le PVP est désactivé. Profitez en bien pour vous stuffer au maximum !\n" + ChatColor.GOLD + "Le PVP :" + ChatColor.BLACK + "\nLorque le PVP s'active, n'ayez aucune pitié pour rester le dernier en vie.");
        bm.setPages(pages);
        // Lets fix the typo
        book.setItemMeta(bm);
        player.getInventory().setItem(0, book);
        player.teleport(getWaitLocation());
    }

    @Override
    public void startGame()
    {
        super.startGame();
        for (APlayer aPlayer : this.gamePlayers.values())
            this.objectiveTab.addReceiver(aPlayer.getPlayerIfOnline());

        this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.GOLD + "Préparation du jeu !", true);
        this.preparationTimer = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new PreparingCountdown(this), 0L, 20L);

        this.scoreboard.registerNewObjective("vie", "health").setDisplaySlot(DisplaySlot.BELOW_NAME);
        this.scoreboard.getObjective("vie").setDisplayName(ChatColor.RED + "♥");

        List<APlayer> remove = new ArrayList<>();
        Iterator<APlayer> iterator = this.gamePlayers.values().iterator();
        Collections.shuffle(this.spawns);

        for (Location spawn : this.spawns)
        {
            if (!iterator.hasNext())
                break ;
            APlayer gamePlayer = iterator.next();
            Player player = gamePlayer.getPlayerIfOnline();
            resetPlayer(player);
            if (player == null)
                remove.add(gamePlayer);
            else
            {
                /*
                        while (!spawn.getBlock().isEmpty() && spawn.getY() < 200.0)
                        {
                            spawn.setY(spawn.getY() + 1.0);
                        }
                 */
                player.teleport(spawn);
                player.setGameMode(GameMode.SURVIVAL);
                this.scoreboard.getObjective("vie").getScore(player.getName()).setScore(20);
            }
        }

        while (iterator.hasNext())
        {
            try
            {
                SamaGamesAPI.get().getGameManager().kickPlayer(iterator.next().getPlayerIfOnline(), null);
            }
            catch (Exception ignored) {}
        }

        for (GamePlayer player : remove)
        {
            try
            {
                SamaGamesAPI.get().getGameManager().kickPlayer(player.getPlayerIfOnline(), null);
            }
            catch (Exception ignored) {}
        }
    }

    public void start()
    {
        try
        {
            this.preparationTimer.cancel();
            this.preparationTimer = null;
            this.plugin.getLogger().info("Cancelled thread");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.inGame = true;

        this.gameTimer = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new Runnable()
        {
            int time = 0;

            @Override
            public void run()
            {
                ++this.time;

                for (APlayer aPlayer : Arena.this.gamePlayers.values())
                {
                    aPlayer.getObjectiveInfo().setLine(0, ChatColor.AQUA + "");
                    aPlayer.getObjectiveInfo().setLine(2, ChatColor.YELLOW + "");
                    aPlayer.getObjectiveInfo().setLine(3, ChatColor.GRAY + "Joueurs: " + ChatColor.WHITE + getConnectedPlayers());
                    aPlayer.getObjectiveInfo().setLine(4, ChatColor.GRAY + "");
                    aPlayer.getObjectiveInfo().setLine(5, ChatColor.GRAY + "Kills: " + ChatColor.WHITE + aPlayer.getKills());
                    aPlayer.getObjectiveInfo().setLine(6, ChatColor.BLACK + "");
                    aPlayer.getObjectiveInfo().setLine(7, ChatColor.WHITE + Utils.secondsToString(this.time));
                    aPlayer.getObjectiveInfo().updateLines();
                }
            }
        }, 20L, 20L);

        this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.GOLD + "La partie commence. Bonne chance !", true);
        this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.GOLD + "Le PVP sera activé dans 3 minutes.", true);

        this.pvpCount = this.plugin.getServer().getScheduler().runTaskTimer(plugin, new PVPEnable(this), 0L, 20L);

        this.plugin.getServer().getScheduler().runTask(this.plugin, () ->
        {
            for (GamePlayer player : this.gamePlayers.values())
            {
                Player pl = player.getPlayerIfOnline();
                if (pl != null)
                {
                    resetPlayer(pl);
                    pl.setGameMode(GameMode.SURVIVAL);
                    pl.getInventory().setItem(7, Dimensions.getCompass());
                    pl.getInventory().setItem(8, Dimensions.getSwap());
                    pl.setExp(0);
                    pl.setLevel(0);
                }
            }
        });

        RandomEffects eff = new RandomEffects(this);
        this.randomEffects = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, eff, 0L, 20L);
    }

    public void enablePVP()
    {
        this.isPVPEnabled = true;
        if (this.pvpCount != null)
            this.pvpCount.cancel();
        this.pvpCount = null;

        this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.GOLD + "Le PVP est activé ! C'est l'heure du d-d-d-duel !", true);
        this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () ->
        {
            for (GamePlayer player : getInGamePlayers().values())
            {
                Player bPlayer = player.getPlayerIfOnline();
                Player target = getNewTarget(player.getUUID());
                if (target == null)
                    continue;
                Dimensions.interactListener.targetPlayer(bPlayer, target);
                bPlayer.sendMessage(this.coherenceMachine.getGameTag() + ChatColor.GOLD + "Votre cible est " + target.getDisplayName() + ChatColor.GOLD + ". Tuez le pour gagner un bonus de coins !");
                bPlayer.sendMessage(this.coherenceMachine.getGameTag() + ChatColor.GOLD + "Votre boussole pointe désormais vers ce joueur. Faites clic gauche avec votre boussole pour la pointer vers lui à nouveau !");
            }
        }, 600L);
    }

    public boolean isDeathmatch()
    {
        return this.isDeathmatch;
    }

    public void startDeathMatch()
    {
        this.dmCount.cancel();
        this.isDeathmatch = true;

        Iterator<Location> spawns = this.deathmatchSpawns.iterator();

        if (!isPVPEnabled())
            enablePVP();

        for (GamePlayer ap : getInGamePlayers().values())
        {
            Player player = ap.getPlayerIfOnline();
            if (!spawns.hasNext())
            {
                this.plugin.getServer().broadcastMessage(ChatColor.RED + "Une erreur s'est produite, tous les joueurs ne peuvent pas être transférés en deathmatch.");
                break ;
            }
            else if(player != null)
            {
                Location spawn = spawns.next();
                player.teleport(spawn);
            }
        }
    }

    public void broadcastSound(Sound sound)
    {
        for (GamePlayer pl : this.gamePlayers.values())
        {
            Player p = pl.getPlayerIfOnline();
            if (p != null)
                p.playSound(p.getLocation(), sound, 1, 1);
        }
    }

    private void finish()
    {
        if (randomEffects != null)
            randomEffects.cancel();

        if (this.gameTimer != null)
            this.gameTimer.cancel();

        if (this.getInGamePlayers().isEmpty())
        {
            this.handleGameEnd();
            return ;
        }

        if (this.dmCount != null)
            this.dmCount.cancel();

        if (this.getInGamePlayers().size() > 1)
            return ;

        APlayer winner = this.getInGamePlayers().values().iterator().next();
        final Player player = winner.getPlayerIfOnline();
        if (player == null)
        {
            this.handleGameEnd();
            return ;
        }

        Titles.sendTitle(player, 5, 80, 5, ChatColor.GOLD + "Victoire !", ChatColor.GREEN + "Vous gagnez la partie en " + ChatColor.AQUA + winner.getKills() + ChatColor.GREEN + " kills !");
        for (final Player p : this.plugin.getServer().getOnlinePlayers())
        {
            if (p.getUniqueId().equals(player.getUniqueId()))
                continue ;
            Titles.sendTitle(p, 5, 80, 5, ChatColor.GOLD + "Fin de partie !", ChatColor.GREEN + "Bravo à " + player.getDisplayName());
        }

        try
        {
            SamaGamesAPI.get().getStatsManager().getPlayerStats(player.getUniqueId()).getDimensionStatistics().incrByKills(1);
        }
        catch (Exception ignored){}

        this.coherenceMachine.getTemplateManager().getPlayerWinTemplate().execute(player, winner.getKills());
        //Bukkit.broadcastMessage(plugin.pluginTAG+ChatColor.GREEN+ChatColor.MAGIC+"aaa"+ChatColor.GOLD+" Victoire ! "+ChatColor.GREEN+ChatColor.MAGIC+"aaa"+ChatColor.GOLD+" Bravo a "+ChatColor.LIGHT_PURPLE+player.getName()+ChatColor.GOLD+" !");

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
        {
            addCoins(player, 60, "Victoire !");
            addStars(player, 3, "Victoire !");
        });

        // Feux d'artifice swag
        final int nb = 20;
        this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable()
        {
            int compteur = 0;

            @Override
            public void run()
            {
                if (this.compteur >= nb)
                    return ;

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

                this.compteur++;
            }
        }, 5L, 5L);

        this.handleGameEnd();
    }

    private void joinSpectators(Player p)
    {
        GamePlayer player = this.getPlayer(p.getUniqueId());
        if (player != null)
            player.setSpectator();
    }

    public void respawnSpec(Player p)
    {
        this.joinSpectators(p);
    }

    public Location getWaitLocation()
    {
        return this.waitLocation;
    }

    public boolean isInGame()
    {
        return this.inGame;
    }

    @Override
    public void handleLogout(Player player)
    {
        stumpPlayer(player);
        super.handleLogout(player);
        this.gameManager.refreshArena();
    }

    public void stumpPlayer(final Player player)
    {
        Dimensions.interactListener.unregisterTask(player);

        if (this.getStatus() != Status.IN_GAME || (player != null && this.isSpectator(player)))
            return ;

        int left = getInGamePlayers().size() - 1;
        boolean isWon = (left <= 1);

        //We check if player doesn't suicide
        if (player != null && !(player.getKiller() != null && player.getKiller().getUniqueId().equals(player.getUniqueId())))
        {
            if (left == 2)
                addCoins(player, 20, "Troisième !");
            else if (left == 1)
            {
                addCoins(player, 40, "Second !");
                addStars(player, 1, "Vous y êtes presque !");
            }

            try
            {
                SamaGamesAPI.get().getStatsManager().getPlayerStats(player.getUniqueId()).getDimensionStatistics().incrByKills(1);
            }
            catch (Exception ignored) {}
        }

        if (player != null && player.isOnline())
        {
            this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.RED + player.getName() + " a été éliminé.", true);
            joinSpectators(player);
        }

        if (!isWon)
        {
            this.plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + left + ChatColor.YELLOW + " joueurs en vie.");
            if (left <= this.deathmatchSpawns.size() && !this.isDeathmatch && this.dmCount == null)
            {
                Deathmatch countdown = new Deathmatch(this);
                this.dmCount = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, countdown, 0L, 20L);
            }

            if (player == null)
                return ;

            final ArrayList<UUID> ids = new ArrayList<>();
            for (UUID plid : getTargetedBy(player.getUniqueId()))
            {
                ids.add(plid);
                this.targets.remove(plid);
            }

            if (!isDeathmatch())
            {
                this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () ->
                {
                    if (this.status != Status.IN_GAME)
                        return ;
                    for (UUID plid : ids)
                    {
                        GamePlayer pl = getPlayer(plid);
                        if (pl == null)
                            continue ;
                        Player p = pl.getPlayerIfOnline();
                        if (p == null)
                            continue ;

                        Player target = getNewTarget(pl.getUUID());
                        Dimensions.interactListener.targetPlayer(p, target);
                        p.sendMessage(this.coherenceMachine.getGameTag() + ChatColor.GOLD + " Votre cible est " + target.getDisplayName() + ChatColor.GOLD + ". Tuez le pour gagner un bonus de coins !");
                        p.sendMessage(this.coherenceMachine.getGameTag() + ChatColor.GOLD + " Votre boussole pointe désormais vers ce joueur. Faites clic gauche avec votre boussole pour la pointer vers lui à nouveau !");
                    }
                }, 200L);
            }
        }
        else
            finish();
    }

    public boolean isPlaying(Player p)
    {
        return this.hasPlayer(p) && !this.isSpectator(p);
    }

    public boolean isPVPEnabled()
    {
        return this.isPVPEnabled;
    }

    private void resetPlayer(Player p)
    {
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
    }

    public void tpMenu(final Player player)
    {
        double nb = getConnectedPlayers();
        double nSlots = Math.ceil(nb / 9) * 9;
        SamaGamesAPI.get().getGuiManager().openGui(player, new AbstractGui()
        {
            @Override
            public void display(Player player)
            {
                this.inventory = Arena.this.plugin.getServer().createInventory(null, (int) nSlots, ChatColor.GOLD + "Téléportation !");

                int slot = 0;
                for (GamePlayer p : getInGamePlayers().values())
                {
                    DimensionsManager.Dimension dimension = Arena.this.dimensionsManager.dimensions.get(p.getUUID());
                    String dimName = ChatColor.DARK_GREEN + Arena.this.dimensionsManager.overworldName;
                    if (dimension != null && dimension == DimensionsManager.Dimension.PARALLEL)
                        dimName = ChatColor.DARK_RED + Arena.this.dimensionsManager.hardName;

                    Player bPlayer = p.getPlayerIfOnline();
                    if (bPlayer == null)
                        continue ;

                    String name = bPlayer.getDisplayName();
                    this.setSlotData(name,
                            new ItemStack(Material.STONE_SWORD),
                            slot,
                            new String[]
                                    {
                                        ChatColor.AQUA + "" + ((int) Math.ceil(bPlayer.getHealth())) + ChatColor.GOLD + " points de vie",
                                        ChatColor.GOLD + "Dimension : " + dimName
                                    },
                            bPlayer.getUniqueId().toString());
                    slot++;
                }

                player.openInventory(this.inventory);
            }

            @Override
            public void onClick(Player player, ItemStack stack, String action)
            {
                if (!action.equals("close"))
                {
                    UUID user = UUID.fromString(action);
                    Player player1 = getPlayer(user).getPlayerIfOnline();
                    if (player1 != null)
                    {
                        player.teleport(player1);
                        player.sendMessage(ChatColor.GREEN + "Téléportation !");
                    } else
                        player.sendMessage(ChatColor.RED + "Le joueur n'est plus connecté.");
                }

                SamaGamesAPI.get().getGuiManager().closeGui(player);
            }
        });
    }

    VObjective getObjectiveTab()
    {
        return this.objectiveTab;
    }

    public UUID getTarget(UUID player)
    {
        return this.targets.get(player);
    }

    public List<UUID> getTargetedBy(UUID target)
    {
        return this.targets.keySet().stream().filter(key -> this.targets.get(key) != null && this.targets.get(key).equals(target)).collect(Collectors.toList());
    }

    public Player getNewTarget(UUID player)
    {
        return this.getNewTarget(player, 0);
    }

    private Player getNewTarget(UUID player, int redundency)
    {
        Random rnd = new Random();

        List<GamePlayer> players = new ArrayList<>(getInGamePlayers().values());
        GamePlayer target = players.get(rnd.nextInt(players.size()));

        if (target.getUUID().equals(player) || target.getPlayerIfOnline() == null)
        {
            if (redundency <= 15)
                return getNewTarget(player, redundency + 1);
            else
                return null;
        }

        this.targets.put(player, target.getUUID());
        return target.getPlayerIfOnline();
    }

    public Dimensions getPlugin()
    {
        return this.plugin;
    }
}
