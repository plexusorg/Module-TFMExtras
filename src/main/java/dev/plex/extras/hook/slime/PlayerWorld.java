package dev.plex.extras.hook.slime;

import com.google.common.collect.Lists;
import dev.plex.storage.annotation.PrimaryKey;
import dev.plex.storage.annotation.SQLTable;
import java.util.List;
import java.util.UUID;
import lombok.Data;

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
