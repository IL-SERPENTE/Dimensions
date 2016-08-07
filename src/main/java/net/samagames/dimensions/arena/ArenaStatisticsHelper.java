package net.samagames.dimensions.arena;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.IGameStatisticsHelper;

import java.util.UUID;

public class ArenaStatisticsHelper implements IGameStatisticsHelper
{
    @Override
    public void increasePlayedTime(UUID uuid, long playedTime)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getDimensionStatistics().incrByPlayedTime(playedTime);
    }

    @Override
    public void increasePlayedGames(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getDimensionStatistics().incrByPlayedGames(1);
    }

    @Override
    public void increaseWins(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getDimensionStatistics().incrByWins(1);
    }

    public void increaseKills(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getDimensionStatistics().incrByKills(1);
    }

    public void increaseDeaths(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getDimensionStatistics().incrByDeaths(1);
    }
}
