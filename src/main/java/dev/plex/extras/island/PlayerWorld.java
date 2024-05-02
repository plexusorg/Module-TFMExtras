package dev.plex.extras.island;

import com.google.common.collect.Lists;
import dev.plex.extras.TFMExtras;
import dev.plex.extras.island.info.IslandPermissions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Taah
 * @since 9:26 PM [24-08-2023]
 */
@AllArgsConstructor
//@SQLTable("player_worlds")
@Getter
@Setter
@Accessors(fluent = true)
public class PlayerWorld
{
//    @PrimaryKey
    private final UUID owner;
    private final List<UUID> members;
    private IslandPermissions editPermission;
    private IslandPermissions visitPermission;

    public boolean addMember(UUID member)
    {
        if (members.contains(member))
        {
            return false;
        }
        CompletableFuture.runAsync(() -> TFMExtras.getModule().getIslandHandler().insertMember(this.owner, member));
        this.members.add(member);
        return true;
    }

    public boolean removeMember(UUID member)
    {
        if (!members.contains(member))
        {
            return false;
        }
        CompletableFuture.runAsync(() -> TFMExtras.getModule().getIslandHandler().deleteMember(this.owner, member));
        this.members.remove(member);
        return true;
    }
}
