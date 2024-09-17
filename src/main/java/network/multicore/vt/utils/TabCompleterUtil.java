package network.multicore.vt.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * BSD 3-Clause License
 * <p>
 * Copyright (c) 2016 - 2024, Lorenzo Magni
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class TabCompleterUtil {

    /**
     * Get the list of player names starting with the searched characters.
     *
     * @param search       The starting characters of the names searched.
     *                     If null or empty all player names will be returned.
     * @param showVanished Choose to show vanished players (support Supervanish)
     * @return A list of player names.
     */
    public static List<String> getPlayers(@Nullable String search, boolean showVanished) {
        List<String> players = new ArrayList<>();

        if (search == null || search.trim().isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isVanished(player) && !showVanished) continue;
                players.add(player.getName());
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isVanished(player) && !showVanished) continue;
                if (player.getName().toLowerCase().startsWith(search.toLowerCase())) players.add(player.getName());
            }
        }

        return players;
    }

    /**
     * Get the list of player names starting with the searched characters.
     *
     * @param search The starting characters of the names searched.
     *               If null or empty all player names will be returned.
     * @return A list of player names.
     */
    public static List<String> getPlayers(@Nullable String search) {
        return getPlayers(search, true);
    }

    /**
     * Get the list of player display names starting with the searched characters.
     *
     * @param search       The starting characters of the names searched.
     *                     If null or empty all player names will be returned.
     * @param showVanished Choose to show vanished players (support Supervanish)
     * @return A list of player display names.
     */
    public static List<String> getDisplayNames(@Nullable String search, boolean showVanished) {
        List<String> players = new ArrayList<>();

        if (search == null || search.isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isVanished(player) && !showVanished) continue;
                players.add(player.getDisplayName());
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isVanished(player) && !showVanished) continue;
                if (player.getDisplayName().toLowerCase().startsWith(search.toLowerCase()))
                    players.add(player.getDisplayName());
            }
        }

        return players;
    }

    /**
     * Get the list of player display names starting with the searched characters.
     *
     * @param search The starting characters of the names searched.
     *               If null or empty all player names will be returned.
     * @return A list of player display names.
     */
    public static List<String> getDisplayNames(@Nullable String search) {
        return getDisplayNames(search, true);
    }

    /**
     * Get a list of completions starting with the searched characters.
     *
     * @param search      The starting characters of the completions searched.
     *                    If null or empty all completions will be returned.
     * @param completions A list of completions.
     * @return A list of completions.
     */
    public static List<String> getCompletions(@Nullable String search, @NotNull List<String> completions) {
        Objects.requireNonNull(completions);
        if (search == null || search.trim().isEmpty()) return completions;
        List<String> matches = new ArrayList<>();

        for (String completion : completions) {
            if (!completion.toLowerCase().startsWith(search.toLowerCase())) continue;
            matches.add(completion);
        }

        return matches;
    }

    /**
     * Get a list of completions starting with the searched characters.
     *
     * @param search      The starting characters of the completions searched.
     *                    If null or empty all completions will be returned.
     * @param completions A list of completions.
     * @return A list of completions.
     */
    public static List<String> getCompletions(@Nullable String search, @NotNull String... completions) {
        Objects.requireNonNull(completions);
        if (search == null || search.trim().isEmpty()) return Arrays.asList(completions);
        List<String> matches = new ArrayList<>();

        for (String completion : completions) {
            if (!completion.toLowerCase().startsWith(search.toLowerCase())) continue;
            matches.add(completion);
        }

        return matches;
    }

    /**
     * Get the list of world names starting with the searched characters.
     *
     * @param search The starting characters of the names searched.
     *               If null or empty all world names will be returned.
     * @return A list of world names.
     */
    public static List<String> getWorlds(@Nullable String search) {
        List<String> worlds = new ArrayList<>();

        if (search == null || search.trim().isEmpty()) {
            Bukkit.getWorlds().forEach(world -> worlds.add(world.getName()));
        } else {
            Bukkit.getWorlds().forEach(world -> {
                if (world.getName().toLowerCase().startsWith(search.toLowerCase())) worlds.add(world.getName());
            });
        }

        return worlds;
    }

    /**
     * Check if a player is vanished using Supervanish.
     *
     * @param player The player to check.
     * @return true if the player is vanished, false if he's not.
     */
    public static boolean isVanished(@NotNull Player player) {
        Objects.requireNonNull(player);

        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}
