package dev.plex.extras.island.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.plex.Plex;
import dev.plex.extras.island.PlayerWorld;
import dev.plex.extras.island.info.IslandPermissions;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Taah
 * @since 10:53 PM [05-01-2024]
 */
public class IslandHandler
{
    //TODO: Create Table statements
    private static final String LOAD_ISLANDS = "SELECT * FROM `islands`;";
    private static final String LOAD_MEMBERS = "SELECT * FROM `island_members`;";
    private static final String INSERT_ISLAND = "INSERT INTO `islands` (`owner`, `editPermission`, `visitPermission`, `interactPermission`) VALUES(?, ?, ?, ?);";
    private static final String INSERT_MEMBER = "INSERT INTO `island_members` (`uuid`, `island_owner_uuid`) VALUES(?, ?);";
    private static final String DELETE_ISLAND = "DELETE FROM `islands` WHERE `owner`=?;";
    private static final String DELETE_MEMBER = "DELETE FROM `island_members` WHERE `uuid`=? AND `island_owner_uuid`=?;";
    private static final String UPDATE_ISLAND = "UPDATE `islands` SET `editPermission`=?, `visitPermission`=?, `interactPermission`=? WHERE `owner`=?;";
    private static final String CREATE_ISLANDS_TABLE = "CREATE TABLE IF NOT EXISTS `islands` (`owner` VARCHAR(36) NOT NULL PRIMARY KEY, `editPermission` VARCHAR(10), `visitPermission` VARCHAR(10), `interactPermission` VARCHAR(10));";
    private static final String CREATE_MEMBERS_TABLE = "CREATE TABLE IF NOT EXISTS `island_members` (`uuid` VARCHAR(36), `island_owner_uuid` VARCHAR(36));";

    @Getter
    @Accessors(fluent = true)
    private final Map<UUID, PlayerWorld> loadedIslands = Maps.newHashMap();

    public void createTables()
    {
        try (Connection connection = Plex.get().getSqlConnection().getCon())
        {
            connection.prepareStatement(CREATE_ISLANDS_TABLE).execute();
            connection.prepareStatement(CREATE_MEMBERS_TABLE).execute();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    public void loadIslands()
    {
        // Member to Islands Mapping
        final Map<UUID, List<UUID>> mappedMembers = Maps.newHashMap();

        try (Connection connection = Plex.get().getSqlConnection().getCon())
        {
            final PreparedStatement loadIslands = connection.prepareStatement(LOAD_ISLANDS);
            final PreparedStatement loadMembers = connection.prepareStatement(LOAD_MEMBERS);

            final ResultSet membersQuery = loadMembers.executeQuery();
            while (membersQuery.next())
            {
                final UUID uuid = UUID.fromString(membersQuery.getString("uuid"));
                List<UUID> islands = mappedMembers.getOrDefault(uuid, Lists.newArrayList());
                islands.add(UUID.fromString(membersQuery.getString("island_owner_uuid")));
                mappedMembers.put(uuid, islands);
            }


            final ResultSet islandsQuery = loadIslands.executeQuery();
            while (islandsQuery.next())
            {
                final UUID owner = UUID.fromString(islandsQuery.getString("owner"));
                final List<UUID> members = mappedMembers.entrySet().stream().filter(uuiduuidEntry -> uuiduuidEntry.getValue().stream().anyMatch(owners -> owners.equals(owner))).map(Map.Entry::getKey).toList();
                final IslandPermissions editPerm = IslandPermissions.valueOf(islandsQuery.getString("editPermission").toUpperCase());
                final IslandPermissions visitPerm = IslandPermissions.valueOf(islandsQuery.getString("visitPermission").toUpperCase());

                final String interactPermission = islandsQuery.getString("interactPermission");

                final IslandPermissions interactPerm = interactPermission == null ? IslandPermissions.NOBODY : IslandPermissions.valueOf(interactPermission.toUpperCase());
                loadedIslands.put(owner, new PlayerWorld(owner, members, editPerm, visitPerm, interactPerm));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void insertIsland(PlayerWorld world)
    {
        try (Connection connection = Plex.get().getSqlConnection().getCon())
        {
            final PreparedStatement statement = connection.prepareStatement(INSERT_ISLAND);
            statement.setString(1, world.owner().toString());
            statement.setString(2, world.editPermission().name());
            statement.setString(3, world.visitPermission().name());
            statement.setString(4, world.interactPermission().name());

            statement.execute();

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void insertMember(UUID islandOwner, UUID member)
    {
        try (Connection connection = Plex.get().getSqlConnection().getCon())
        {
            final PreparedStatement statement = connection.prepareStatement(INSERT_MEMBER);
            statement.setString(1, member.toString());
            statement.setString(2, islandOwner.toString());
            statement.execute();

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void deleteMember(UUID islandOwner, UUID member)
    {
        try (Connection connection = Plex.get().getSqlConnection().getCon())
        {
            final PreparedStatement statement = connection.prepareStatement(DELETE_MEMBER);
            statement.setString(1, member.toString());
            statement.setString(2, islandOwner.toString());
            statement.execute();

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void deleteIsland(UUID islandOwner)
    {
        try (Connection connection = Plex.get().getSqlConnection().getCon())
        {
            final PreparedStatement statement = connection.prepareStatement(DELETE_ISLAND);
            statement.setString(1, islandOwner.toString());
            statement.execute();

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void updateIsland(PlayerWorld world)
    {
        try (Connection connection = Plex.get().getSqlConnection().getCon())
        {
            final PreparedStatement statement = connection.prepareStatement(UPDATE_ISLAND);
            statement.setString(1, world.editPermission().name());
            statement.setString(2, world.visitPermission().name());
            statement.setString(3, world.interactPermission().name());
            statement.setString(4, world.owner().toString());

            statement.executeUpdate();

        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

}
