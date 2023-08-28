package dev.plex.extras.hook.slime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.plex.storage.annotation.PrimaryKey;
import dev.plex.storage.annotation.SQLTable;
import lombok.Data;
import org.bukkit.GameRule;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Taah
 * @since 9:26 PM [24-08-2023]
 */
@Data
@SQLTable("player_worlds")
public class PlayerWorld
{
    @PrimaryKey
    private final UUID owner;
    private final List<UUID> members = Lists.newArrayList();
    private boolean anyoneCanEdit;
    private boolean anyoneCanVisit;
}
