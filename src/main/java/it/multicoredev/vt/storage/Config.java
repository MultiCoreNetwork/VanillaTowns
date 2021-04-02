package it.multicoredev.vt.storage;

import com.google.gson.annotations.SerializedName;
import it.multicoredev.mbcore.spigot.config.JsonConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright © 2020 - 2021 by Lorenzo Magni
 * This file is part of VanillaTowns.
 * VanillaTowns is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class Config extends JsonConfig {
    @SerializedName("broadcast_town_creation")
    public Boolean broadcastTownCreation;
    @SerializedName("broadcast_town_deletion")
    public Boolean broadcastTownDeletion;
    @SerializedName("broadcast_town_rename")
    public Boolean broadcastTownRename;
    @SerializedName("teleport_timer")
    public Integer teleportTimer;
    @SerializedName("log_towns_operations")
    public Boolean logTowns;
    @SerializedName("dimension_blacklist_for_home")
    public List<String> dimBlacklist;
    public Colors colors;
    public Strings strings;

    public Config() {
        init();
    }

    @Override
    protected void init() {
        if (broadcastTownCreation == null) broadcastTownCreation = true;
        if (broadcastTownDeletion == null) broadcastTownDeletion = true;
        if (teleportTimer == null) teleportTimer = 5;
        if (logTowns == null) logTowns = true;
        if (dimBlacklist == null) dimBlacklist = Arrays.asList("world_nether", "world_the_end");
        if (colors == null) colors = new Colors();
        if (strings == null) strings = new Strings();
    }

    public static class Colors extends JsonConfig {
        public String leader;
        public String admin;
        public String member;

        public Colors() {
            init();
        }

        @Override
        protected void init() {
            if (leader == null) leader = "&c";
            if (admin == null) admin = "&6";
            if (member == null) member = "&b";
        }
    }

    public static class Strings extends JsonConfig {
        public String admin;
        @SerializedName("admin_demoted")
        public String adminDemoted;
        @SerializedName("admin_demoted_staff")
        public String adminDemotedStaff;
        @SerializedName("admin_demoted_target")
        public String adminDemotedTarget;
        @SerializedName("admin_promoted")
        public String adminPromoted;
        @SerializedName("admin_promoted_staff")
        public String adminPromotedStaff;
        @SerializedName("admin_promoted_target")
        public String adminPromotedTarget;
        @SerializedName("allow_player_deposit")
        public String allowPlayerDeposit;
        @SerializedName("allow_player_deposit_target")
        public String allowPlayerDepositTarget;
        @SerializedName("allow_player_withdraw")
        public String allowPlayerWithdraw;
        @SerializedName("allow_player_withdraw_target")
        public String allowPlayerWithdrawTarget;
        @SerializedName("already_in_town")
        public String alreadyInTown;
        @SerializedName("already_in_town_staff")
        public String alreadyInTownStaff;
        @SerializedName("already_in_your_town")
        public String alreadyInYourTown;
        @SerializedName("balance_chargeback")
        public String balanceChargeback;
        public String baltop;
        @SerializedName("baltop_head")
        public String baltopHead;
        @SerializedName("baltop_tail")
        public String baltopTail;
        @SerializedName("blacklisted_dimension")
        public String blacklistedDim;
        @SerializedName("cannot_deposit")
        public String cannotDeposit;
        @SerializedName("cannot_invite")
        public String cannotInvite;
        @SerializedName("cannotWithdraw")
        public String cannotWithdraw;
        @SerializedName("chat_format")
        public String chatFormat;
        @SerializedName("deposit_success")
        public String depositSuccess;
        @SerializedName("home_format")
        public String homeFormat;
        @SerializedName("home_removed")
        public String homeRemoved;
        @SerializedName("home_set")
        public String homeSet;
        @SerializedName("insufficient_money")
        public String insufficientMoney;
        @SerializedName("insufficient_permissions")
        public String insufficientPerms;
        @SerializedName("insufficient_town_money")
        public String insufficientTownMoney;
        public String jailed;
        public String leader;
        @SerializedName("leader_transfer")
        public String leaderTransfer;
        @SerializedName("leader_transfer_staff")
        public String leaderTransferStaff;
        @SerializedName("leave_denied")
        public String leaveDenied;
        public String member;
        @SerializedName("name_not_available")
        public String nameNotAvailable;
        @SerializedName("no_home")
        public String noHome;
        @SerializedName("no_home_staff")
        public String noHomeStaff;
        @SerializedName("no_invites")
        public String noInvites;
        @SerializedName("no_role")
        public String noRole;
        @SerializedName("no_town")
        public String noTown;
        @SerializedName("not_admin")
        public String notAdmin;
        @SerializedName("not_in_that_town")
        public String notInThatTown;
        @SerializedName("not_in_town")
        public String notInTown;
        @SerializedName("not_in_your_town")
        public String notInYourTown;
        @SerializedName("not_leader")
        public String notLeader;
        @SerializedName("not_player")
        public String notPlayer;
        @SerializedName("not_set")
        public String notSet;
        @SerializedName("not_to_the_leader")
        public String notToTheLeader;
        @SerializedName("player_added_to_town")
        public String playerAddedToTown;
        @SerializedName("player_invite_received")
        public String playerInviteReceived;
        @SerializedName("player_invite_sent")
        public String playerInviteSent;
        @SerializedName("player_join")
        public String playerJoin;
        @SerializedName("player_join_members")
        public String playerJoinMembers;
        @SerializedName("player_kicked")
        public String playerKicked;
        @SerializedName("player_kicked_members")
        public String playerKickedMembers;
        @SerializedName("player_kicked_staff")
        public String playerKickedStaff;
        @SerializedName("player_left")
        public String playerLeft;
        @SerializedName("player_left_members")
        public String playerLeftMembers;
        @SerializedName("player_not_found")
        public String playerNotFound;
        @SerializedName("plugin_reloaded")
        public String pluginReloaded;
        @SerializedName("teleport_countdown")
        public String teleportCountdown;
        @SerializedName("town_balance")
        public String townBalance;
        @SerializedName("town_created")
        public String townCreated;
        @SerializedName("town_created_broadcast")
        public String townCreatedBC;
        @SerializedName("town_deleted")
        public String townDeleted;
        @SerializedName("town_deleted_broadcast")
        public String townDeletedBC;
        @SerializedName("town_not_found")
        public String townNotFound;
        @SerializedName("town_renamed")
        public String townRenamed;
        @SerializedName("town_renamed_broadcast")
        public String townRenamedBC;
        @SerializedName("transaction_error")
        public String transactionError;
        @SerializedName("withdrew_success")
        public String withdrawSuccess;

        @SerializedName("town_info_self")
        public List<String> townInfoSelf;
        @SerializedName("town_info_other")
        public List<String> townInfoOther;
        @SerializedName("town_info_staff")
        public List<String> townInfoStaff;

        @SerializedName("help_message")
        public List<String> helpMessage;
        @SerializedName("staff_help_message")
        public List<String> staffHelpMessage;

        public Strings() {
            init();
        }

        @Override
        protected void init() {
            if (admin == null) admin = "Admin";
            if (adminDemoted == null) adminDemoted = "&e{player} &bis now a simple member.";
            if (adminDemotedStaff == null) adminDemotedStaff = "&e{player} &bis now a simple member of the town &e{town}&b.";
            if (adminDemotedTarget == null) adminDemotedTarget = "&bYou are now a simple member of your town.";
            if (adminPromoted == null) adminPromoted = "&e{player} &bis now an admin.";
            if (adminPromotedStaff == null) adminPromotedStaff = "&e{player} &bis now an admin of the town &e{town}&b.";
            if (adminPromotedTarget == null) adminPromotedTarget = "&bYou are now an admin in your town.";
            if (allowPlayerDeposit == null) allowPlayerDeposit = "&e{player} &bcan now deposit money to town bank.";
            if (allowPlayerDepositTarget == null) allowPlayerDepositTarget = "&bYou can now deposit money to town bank.";
            if (allowPlayerWithdraw == null) allowPlayerWithdraw = "&e{player} &bcan't deposit anymore to town bank.";
            if (allowPlayerWithdrawTarget == null) allowPlayerWithdrawTarget = "&bYou can't deposit anymore to town bank.";
            if (alreadyInTown == null) alreadyInTown = "&cYou are already in a town.";
            if (alreadyInTownStaff == null) alreadyInTownStaff = "&cPlayer is already in a town.";
            if (alreadyInYourTown == null) alreadyInYourTown = "&cThis player is already in your town.";
            if (balanceChargeback == null) balanceChargeback = "&e{money} &b added to your balance from your town bank.";
            if (baltop == null) baltop = "&e{position}. &b&l{town} &r- &e{balance}";
            if (baltopHead == null) baltopHead = "&7&m------&e Towns &7&m------";
            if (baltopTail == null) baltopTail = "&7&m------&e Towns &7&m------";
            if (blacklistedDim == null) blacklistedDim = "&cYou cannot set your town home in this dimension.";
            if (cannotDeposit == null) cannotDeposit = "&cYou don't have the permission to deposit money to your town bank.";
            if (cannotInvite == null) cannotInvite = "&cYou can't invite that player. He/She hasn't the permission to join a town.";
            if (cannotWithdraw == null) cannotWithdraw = "&cYou don't have the permission to withdraw money from your town bank.";
            if (chatFormat == null) chatFormat = "&dTOWN: &6{player} &e>&r {message}";
            if (depositSuccess == null) depositSuccess = "&e{money} &bdeposited to your town bank.";
            if (homeFormat == null) homeFormat = "&6W: &e{world} &6X: &e{x} &6Y: &e{y} &6Z: &e{z}";
            if (homeRemoved == null) homeRemoved = "&bHome removed";
            if (homeSet == null) homeSet = "&bHome set";
            if (insufficientMoney == null) insufficientMoney = "&cYou don't have enough money.";
            if (insufficientPerms == null) insufficientPerms = "&cInsufficient permissions.";
            if (insufficientTownMoney == null) insufficientTownMoney = "&cYour town bank doesn't have enough money.";
            if (jailed == null) jailed = "&cYou are jailed.";
            if (leader == null) leader = "Leader";
            if (leaderTransfer == null) leaderTransfer = "&e{player} &bis now the leader of your town.";
            if (leaderTransferStaff == null) leaderTransferStaff = "&e{player} &bis now the leader of town &e{town}&b.";
            if (leaveDenied == null) leaveDenied = "&cAs a leader you cannot leave the town. You can delete it of give it to someone else.";
            if (member == null) member = "Member";
            if (nameNotAvailable == null) nameNotAvailable = "&cTown name not available.";
            if (noHome == null) noHome = "&cYour town doesn't have a home.";
            if (noHomeStaff == null) noHomeStaff = "&cThis town doesn't have a home.";
            if (noInvites == null) noInvites = "&cYou don't have any invite.";
            if (noRole == null) noRole = "None";
            if (noTown == null) noTown = "None";
            if (notAdmin == null) notAdmin = "&cYou must be at least a town admin to use this command.";
            if (notInThatTown == null) notInThatTown = "&cThis player is not in that town.";
            if (notInTown == null) notInTown = "&cYou are not in a town.";
            if (notInYourTown == null) notInYourTown = "&cThis player is not in your town.";
            if (notLeader == null) notLeader = "&cYou must be the leader of the town to use this command.";
            if (notPlayer == null) notPlayer = "&cYou must be a player to run this command.";
            if (notSet == null) notSet = "Not set";
            if (notToTheLeader == null) notToTheLeader = "&cThis operation cannot be done on the leader of the town";
            if (playerAddedToTown == null) playerAddedToTown = "&e{player} &bhas been added to town &e{town}&b.";
            if (playerInviteReceived == null) playerInviteReceived = "!j[{'text':'------------------------','color':'dark_aqua'},{'text':'\\n'},{'text':'You have been invited to ','color':'aqua'},{'text':'{town}','color':'yellow'},{'text':' by ','color':'aqua'},{'text':'{player}','color':'yellow'},{'text':'.\\nClick ','color':'aqua'},{'text':'here','color':'yellow','clickEvent':{'action':'run_command','value':'/town join'}},{'text':' to accept the invite.','color':'aqua'},{'text':'\\n'},{'text':'------------------------','color':'dark_aqua'}]";
            if (playerInviteSent == null) playerInviteSent = "&bYou invited &e{player} &bto join your town.";
            if (playerJoin == null) playerJoin = "&bYou joined the town &e{town}&b.";
            if (playerJoinMembers == null) playerJoinMembers = "&e{player} &bjoined your town.";
            if (playerKicked == null) playerKicked = "&bYou have been kicked from the town &e{town}&b.";
            if (playerKickedMembers == null) playerKickedMembers = "&e{player} &bhas been kicked from your town.";
            if (playerKickedStaff == null) playerKickedStaff = "&e{player} &bhas been kicked from the town &e{town}&b.";
            if (playerLeft == null) playerLeft = "&bYou left the town &e{town}&b.";
            if (playerLeftMembers == null) playerLeftMembers = "&e{player} &bleft your town.";
            if (playerNotFound == null) playerNotFound = "&cPlayer not found";
            if (pluginReloaded == null) pluginReloaded = "&bVanillaTowns reloaded in &3{time}&bms";
            if (teleportCountdown == null) teleportCountdown = "&bYou will be teleported in &e{time} &bseconds.";
            if (townBalance == null) townBalance = "&bTown balance: &e{money}";
            if (townCreated == null) townCreated = "&bYou created the town &e{town}&b.";
            if (townCreatedBC == null) townCreatedBC = "&e{player} &bcreated the town &e{town}&b.";
            if (townDeleted == null) townDeleted = "&bYou deleted your town.";
            if (townDeletedBC == null) townDeletedBC = "&e{player} &bdeleted the town &e{town}&b.";
            if (townNotFound == null) townNotFound = "&cTown not found.";
            if (townRenamed == null) townRenamed = "&bYour town is now known as &e{town}&b.";
            if (townRenamedBC == null) townRenamedBC = "&e{player} &brenamed the town &e{town_old} &b in &e{town_new}&b.";
            if (transactionError == null) transactionError = "&cTransaction error.";
            if (withdrawSuccess == null) withdrawSuccess = "&e{money} &bwithdrawn from your town bank.";

            if (townInfoSelf == null) townInfoSelf = Arrays.asList(
                    "&7&m----&r &e&l{town}&r &7&m----",
                    "&b&lBalance: &e{balance}",
                    "&b&lLeader: &e{leader}",
                    "&b&lAdmins: &e{admins}",
                    "&b&lMembers: &e{members}",
                    "&b&lHome: &e{home}"
            );
            if (townInfoOther == null) townInfoOther = Arrays.asList(
                    "&7&m----&r &e&l{town}&r &7&m----",
                    "&b&lBalance: &e{balance}",
                    "&b&lLeader: &e{leader}",
                    "&b&lAdmins: &e{admins}",
                    "&b&lMembers: &e{members}",
                    "&b&lHome: &e{home}"
            );
            if (townInfoStaff == null) townInfoStaff = Arrays.asList(
                    "&7&m----&r &e&l{town}&r &7&m----",
                    "&b&lBalance: &e{balance}",
                    "&b&lLeader: &e{leader}",
                    "&b&lAdmins: &e{admins}",
                    "&b&lMembers: &e{members}",
                    "&b&lHome: &e{home}"
            );

            if (helpMessage == null) helpMessage = Arrays.asList(
                    "&7&m----&r &e&lVanilla Towns&r &7&m----",
                    "&b/town [town] &f- &eSee the info of your town or a named town.",
                    "&b/town help &f- &eGet this help message.",
                    "&b/town create <name> &f- &eCrate a new town.",
                    "&b/town invite <player> &f- &eInvite a player to your town.",
                    "&b/town join &f- &eAccept an invite to join a town.",
                    "&b/town leave &f- &eLeave your town.",
                    "&b/town kick <player> &f- &eKick a player from your town.",
                    "&b/town rename <name> &f- &eRename your town.",
                    "&b/town give <player> &f- &eTransfer the leader role to someone else.",
                    "&b/town delete &f- &eDelete your town.",
                    "&b/town balance &f- &eSee the balance of your town bank.",
                    "&b/town deposit <amount> &f- &eDeposit money to your town bank.",
                    "&b/town withdraw <amount> &f- &eWithdraw money from your town bank.",
                    "&b/town baltop &f- &eShow the top 10 towns.",
                    "&b/town sethome &f- &eSet the town home.",
                    "&b/town home &f- &eTeleport to the town home.",
                    "&b/town delhome &f- &eDelete the town home.",
                    "&b/town user setAdmin <player> &f- &eMake a member admin of your town",
                    "&b/town user delAdmin <player> &f- &eMake an admin of your town a regular member",
                    "&b/town user deposit <player> allow &f- &eGrant the permission to deposit money",
                    "&b/town user deposit <player> deny &f- &eRevoke the permission to deposit money",
                    "&b/town user withdraw <player> allow &f- &eGrant the permission to withdraw money",
                    "&b/town user withdraw <player> deny &f- &eRevoke the permission to withdraw money",
                    "&b/townchat <message> &f- &eSend a message to all members of your town"
            );
            if (staffHelpMessage == null) staffHelpMessage = Arrays.asList(
                    "&7&m----&r &e&lVanilla Towns&r &7&m----",
                    "&b/vanillatowns reload &f- &eReload the plugin.",
                    "&b/vanillatowns invite <player> <town> &f- &eInvite a player to join a town.",
                    "&b/vanillatowns join <player> <town> &f- &eAdd a player to a town.",
                    "&b/vanillatowns kick <player> <town> &f- &eKick a player from a town.",
                    "&b/vanillatowns rename <name> <town> &f- &eRename a town.",
                    "&b/vanillatowns delete <town> &f- &eDelete a town.",
                    "&b/vanillatowns setLeader <player> <town> &f- &ePromote a player to town leader.",
                    "&b/vanillatowns setAdmin <player> <town> &f- &ePromote a player to town admin.",
                    "&b/vanillatowns setMember <player> <town> &f- &eDemote an admin to simple member.",
                    "&b/vanillatowns setHome <town> &f- &eSet the town home of a town.",
                    "&b/vanillatowns home <town> &f- &eTeleport to the town home of a town.",
                    "&b/vanillatowns delHome <town> &f- &eDelete the town home of a town."
            );
        }
    }
}
