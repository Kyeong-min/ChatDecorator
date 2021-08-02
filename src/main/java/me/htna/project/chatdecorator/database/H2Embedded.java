package me.htna.project.chatdecorator.database;

import lombok.var;
import me.htna.project.chatdecorator.ChatDecorator;
import me.htna.project.chatdecorator.database.entities.CHATLOG;
import me.htna.project.chatdecorator.database.entities.MUTEINFO;
import me.htna.project.chatdecorator.database.entities.NICKNAME;
import me.htna.project.chatdecorator.database.entities.USERINFO;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class H2Embedded {
    private static final String DRIVER = "org.h2.Driver";
    private static final String CONNECTION = "jdbc:h2:";
    private static final String USER = "";
    private static final String PASSWORD = "";
    private final Logger logger;
    private Connection connection;

    public H2Embedded() {
        logger = ChatDecorator.getInstance().getLogger();
    }

    private Timestamp convertInstantToTimestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    private Instant convertTimestampToInstant(Timestamp timestamp) {
        return timestamp.toInstant();
    }

    /**
     * Insert nickname info
     *
     * @param uuid
     * @param nickname
     * @throws SQLException
     */
    public void insertNickname(String uuid, String nickname) throws SQLException {
        String qry = "INSERT INTO NICKNAME (UUID, NICKNAME, DATETIME) VALUES (?, ?, ?)";
        PreparedStatement pstat = null;
        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, uuid);
            pstat.setString(2, nickname);
            pstat.setTimestamp(3, convertInstantToTimestamp(Instant.now()));

            int result = pstat.executeUpdate();
            if (result == 0)
                logger.error("Nickname info insert failed");

        } catch (SQLException e) {
            throw e;
        } finally {
            if (pstat != null)
                pstat.close();
        }
    }

    /**
     * Update nickname
     *
     * @param uuid
     * @param nickname
     * @throws SQLException
     */
    public void updateNickname(String uuid, String nickname) throws SQLException {
        String qry = "UPDATE NICKNAME SET NICKNAME = ?, DATETIME = ? WHERE UUID = ?";
        PreparedStatement pstat = null;
        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, nickname);
            pstat.setTimestamp(2, convertInstantToTimestamp(Instant.now()));
            pstat.setString(3, uuid);

            int result = pstat.executeUpdate();
            if (result == 0)
                logger.error("Update nickname failed");
        } catch (SQLException e) {
            throw e;
        } finally {
            if (pstat != null)
                pstat.close();
        }
    }

    /**
     * Insert or update nickname
     *
     * @param uuid
     * @param nickname
     * @throws SQLException
     */
    public void insertOrUpdateNickname(String uuid, String nickname) throws SQLException {
        var table = selectNickname(uuid);
        if (table.isPresent()) {
            updateNickname(uuid, nickname);
        } else {
            insertNickname(uuid, nickname);
        }
    }

    /**
     * Select nickname
     *
     * @param uuid
     * @return
     * @throws SQLException
     */
    public Optional<NICKNAME> selectNickname(String uuid) throws SQLException {
        String qry = "SELECT * FROM NICKNAME WHERE UUID = ?";
        PreparedStatement pstat = null;
        ResultSet rs = null;
        NICKNAME nickname = null;

        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, uuid);

            rs = pstat.executeQuery();
            if (rs.next())
                nickname = new NICKNAME(rs);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (pstat != null)
                pstat.close();
        }

        return Optional.ofNullable(nickname);
    }

    /**
     * Insert mute info
     *
     * @param uuid
     * @param sourceUuid
     * @param mute_datetime
     * @param reason
     * @throws SQLException
     */
    public void insertMuteInfo(String uuid, String sourceUuid, Instant mute_datetime, String reason) throws SQLException {
        String qry = "INSERT INTO MUTEINFO (UUID, MUTE_SOURCE, MUTE_DATE, REASON) VALUES (?, ?, ?, ?)";
        PreparedStatement pstat = null;
        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, uuid);
            pstat.setString(2, sourceUuid);
            pstat.setTimestamp(3, convertInstantToTimestamp(mute_datetime));
            pstat.setString(4, reason);

            int result = pstat.executeUpdate();
            if (result == 0)
                logger.error("Mute info insert failed");

        } catch (SQLException e) {
            throw e;
        } finally {
            if (pstat != null)
                pstat.close();
        }
    }

    /**
     * Unmute query
     *
     * @param uuid target uuid
     * @param sourceUuid source uuid
     * @param unmute_datetime unmute datetime
     * @throws SQLException sql exception
     */
    public void unmute(String uuid, String sourceUuid, Instant unmute_datetime) throws SQLException {
        String qry = "UPDATE MUTEINFO SET UNMUTE_SOURCE = ?, UNMUTE_DATE = ? WHERE UUID = ?";
        PreparedStatement pstat = null;
        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, sourceUuid);
            pstat.setTimestamp(2, convertInstantToTimestamp(unmute_datetime));
            pstat.setString(3, uuid);

            int result = pstat.executeUpdate();
            if (result == 0)
                logger.error("Mute info update failed");
        } catch (SQLException e) {
            throw e;
        } finally {
            if (pstat != null)
                pstat.close();
        }
    }

    /**
     * Select mute info
     *
     * @param uuid target uuid
     * @return {@MUTEINFO} list
     * @throws SQLException sql exception
     */
    public List<MUTEINFO> selectMuteInfo(String uuid) throws SQLException {
        String qry = "SELECT * FROM MUTEINFO WHERE UUID = ? ORDER BY IDX ASC";
        PreparedStatement pstat = null;
        ResultSet rs = null;
        List<MUTEINFO> infos = new ArrayList<>();

        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, uuid);

            rs = pstat.executeQuery();
            while (rs.next()) {
                MUTEINFO muteinfo = new MUTEINFO(rs);
                infos.add(muteinfo);
            }

        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (pstat != null)
                pstat.close();
        }

        return infos;
    }

    /**
     * Insert chat log
     *
     * @param uuid     Sender uuid
     * @param content  Chat
     * @param datetime Send datetime
     * @param reason   0 is the chat success, other is the reason for failure
     * @throws SQLException sql exception
     */
    public void insertChatLog(String uuid, String content, Instant datetime, CHATLOG.Reason reason) throws SQLException {
        String qry = "INSERT INTO CHATLOG VALUES (?, ?, ?, ?)";
        PreparedStatement pstat = null;
        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, uuid);
            pstat.setObject(2, convertInstantToTimestamp(datetime));
            pstat.setString(3, content);
            pstat.setInt(4, reason.getValue());

            int result = pstat.executeUpdate();
            if (result == 0)
                logger.error("User playtime update failed");

        } catch (SQLException e) {
            throw e;
        } finally {
            if (pstat != null)
                pstat.close();
        }
    }

    /**
     * Select chat log
     *
     * @param uuid    Sender uuid
     * @param instant take log start datetime
     * @param take    take count
     * @return {@link CHATLOG}
     * @throws SQLException sql seception
     */
    public List<CHATLOG> selectChatLog(String uuid, Instant instant, int take) throws SQLException {
        String qry = "SELECT * FROM CHATLOG WHERE UUID = ? AND DATETIME >= ? ORDER BY DATETIME ASC LIMIT ?";
        PreparedStatement pstat = null;
        ResultSet rs = null;
        List<CHATLOG> logs = new ArrayList<>();

        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, uuid);
            pstat.setTimestamp(2, convertInstantToTimestamp(instant));
            pstat.setInt(3, take);

            rs = pstat.executeQuery();
            while (rs.next()) {
                CHATLOG chatlog = new CHATLOG(rs);
                logs.add(chatlog);
            }

        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (pstat != null)
                pstat.close();
        }

        return logs;
    }

    /**
     * Select user info
     *
     * @param uuid user uuid
     * @return {@link USERINFO}
     * @throws SQLException sql exception
     */
    public Optional<USERINFO> selectUserInfo(String uuid) throws SQLException {
        String qry = "SELECT * FROM USERINFO WHERE UUID = ?";
        PreparedStatement pstat = null;
        ResultSet rs = null;
        USERINFO userinfo = null;

        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, uuid);

            rs = pstat.executeQuery();
            if (rs.next()) {
                userinfo = new USERINFO(rs);
            }

        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (pstat != null)
                pstat.close();
        }

        return Optional.ofNullable(userinfo);
    }

    /**
     * Select user playtime
     *
     * @param uuid uuid
     * @return playtime (second)
     * @throws SQLException
     */
    public long selectUserPlayTime(String uuid) throws SQLException {
        String qry = "SELECT PLAY_TIME FROM USERINFO WHERE UUID = ?";
        PreparedStatement pstat = null;
        ResultSet rs = null;

        long result = -1;
        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, uuid);

            rs = pstat.executeQuery();
            if (rs.next())
                result = rs.getLong(1);

        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (pstat != null)
                pstat.close();
        }

        return result;
    }

    /**
     * Insert user info
     *
     * @param uuid User uuid
     * @param join User first join datetime
     * @param last User last join datetime
     */
    public void insertUserInfo(String uuid, Instant join, Instant last) throws SQLException {
        String qry = "INSERT INTO USERINFO VALUES(?, ?, ?, 0)";
        PreparedStatement pstat = null;
        try {
            pstat = connection.prepareStatement(qry);
            pstat.setString(1, uuid);
            pstat.setObject(2, convertInstantToTimestamp(join));
            pstat.setObject(3, convertInstantToTimestamp(last));

            int result = pstat.executeUpdate();
            if (result == 0)
                logger.error("User playtime update failed");

        } catch (SQLException e) {
            throw e;
        } finally {
            if (pstat != null)
                pstat.close();
        }
    }

    /**
     * Update user info
     *
     * @param uuid User uuid
     * @param last User last datetime
     * @throws SQLException sql exception
     */
    public void updateUserInfo(String uuid, Instant last) throws SQLException {
        String qry = "UPDATE USERINFO SET LAST_TIMESTAMP = ? WHERE UUID = ?";
        PreparedStatement pstat = null;
        try {
            pstat = connection.prepareStatement(qry);
            pstat.setObject(1, convertInstantToTimestamp(last));
            pstat.setString(2, uuid);

            int result = pstat.executeUpdate();
            if (result == 0)
                logger.error("User playtime update failed");

        } catch (SQLException e) {
            throw e;
        } finally {
            if (pstat != null)
                pstat.close();
        }
    }

    /**
     * Update user playtime
     *
     * @param uuid     uuid
     * @param playtime playtime
     * @throws SQLException
     */
    public void updateUserPlaytime(String uuid, long playtime) throws SQLException {
        String qry = "UPDATE USERINFO SET PLAY_TIME = ? WHERE UUID = ?";
        PreparedStatement pstat = null;
        try {
            pstat = connection.prepareStatement(qry);
            pstat.setLong(1, playtime);
            pstat.setString(2, uuid);

            int result = pstat.executeUpdate();
            if (result == 0)
                logger.error("User playtime update failed");

        } catch (SQLException e) {
            throw e;
        } finally {
            if (pstat != null)
                pstat.close();
        }
    }

    /**
     * Create connection to database
     *
     * @param file Database file path
     */
    public void connect(Path file) {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            logger.error("H2FileDB#connect ClassNotFoundException");
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(CONNECTION + file + ";TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0", USER, PASSWORD);
        } catch (SQLException e) {
            logger.error("H2FileDB#connect SQLException");
            e.printStackTrace();
        }
    }

    /**
     * Disconnection
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            ChatDecorator.getInstance().getLogger().error("H2FileDB#disconnect SQLException");
            e.printStackTrace();
        }
    }
}
