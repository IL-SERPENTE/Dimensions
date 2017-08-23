package net.samagames.dimensions.arena;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.IGameStatisticsHelper;

import java.util.UUID;

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
