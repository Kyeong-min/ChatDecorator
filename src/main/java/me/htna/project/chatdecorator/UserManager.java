package me.htna.project.chatdecorator;

import me.htna.project.chatdecorator.struct.UserInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Online user manager class
 */
public class UserManager {
    public static final String IDENT_SERVER_CONSOLE = "UUID_SERVER_CONSOLE";
    public static final String IDENT_UNKNOWN = "UUID_UNKNOWN";

    private static UserManager instance;
    private final Map<String, UserInfo> userInfoList;

    /**
     * CTor
     */
    private UserManager() {
        userInfoList = new HashMap<>();

        // put default user info (server console and unknwon)
        userInfoList.put(IDENT_SERVER_CONSOLE, new UserInfo(IDENT_SERVER_CONSOLE, Instant.now(), Instant.now(), 0));
        userInfoList.put(IDENT_UNKNOWN, new UserInfo(IDENT_UNKNOWN, Instant.now(), Instant.now(), 0));
    }

    public static UserManager getInstance() {
        if (instance == null)
            instance = new UserManager();

        return instance;
    }

    /**
     * Add userinfo
     *
     * @param uuid player uuid
     * @param first first join instant
     * @param last last join instant
     * @param playTime playtime
     * @return {@link UserInfo}
     */
    private UserInfo addUser(String uuid, Instant first, Instant last, long playTime) {
        UserInfo userInfo = new UserInfo(uuid, first, last, playTime);
        userInfoList.put(userInfo.getUuid(), userInfo);
        return userInfo;
    }

    /**
     * Player joins to server
     *
     * @param uuid
     * @param first
     * @param last
     * @param playTime
     */
    public UserInfo joinUser(String uuid, Instant first, Instant last, long playTime) {
        Optional<UserInfo> userInfo = findUser(uuid);
        UserInfo info;
        if (userInfo.isPresent()) {
            info = userInfo.get();
        } else {
            info = addUser(uuid, first, last, playTime);
        }
        info.join(last, playTime);
        return info;
    }

    /**
     * Player exits from server
     *
     * @param player {@link Player}
     * @return Logout userinfo
     */
    public Optional<UserInfo> exitUser(Player player) {
        Optional<UserInfo> userInfo = findUser(player);
        userInfo.ifPresent(UserInfo::exit);
        return userInfo;
    }

    /**
     * Find user info
     *
     * @param player {@link Player}
     * @return {@link Optional} UserInfo
     */
    public Optional<UserInfo> findUser(Player player) {
        return findUser(player.getUniqueId().toString());
    }

    /**
     * Find user info
     *
     * @param uuid player unique id
     * @return {@link Optional} UserInfo
     */
    public Optional<UserInfo> findUser(String uuid) {
        return Optional.ofNullable(userInfoList.get(uuid));
    }

    /**
     * Mute user
     *
     * @param uuid       Target player uuid
     * @param sourceUuid Source uuid
     * @param reason     Reason of mute
     * @return if true, success mute
     */
    public boolean muteUser(String uuid, String sourceUuid, String reason) {
        Optional<UserInfo> userInfo = findUser(uuid);
        return userInfo.map(x -> x.mute(sourceUuid, reason)).orElse(false);
    }

    /**
     * Unmute user
     *
     * @param targetUuid Player's uuid
     * @param sourceUuid Source uuid
     * @return if true, success unmute
     */
    public boolean unmuteUser(String targetUuid, String sourceUuid) {
        Optional<UserInfo> userInfo = findUser(targetUuid);
        return userInfo.map(x -> x.unmute(sourceUuid)).orElse(false);
    }

    public String getName(String uuid) {
        if (uuid.equals(IDENT_SERVER_CONSOLE)) {
            return "Server console";
        } else if (uuid.equals(IDENT_UNKNOWN)) {
            return "Unknown source";
        }

        UUID uid = UUID.fromString(uuid);

        Optional<Player> player = Sponge.getServer().getPlayer(uid);
        if (player.isPresent())
            return player.get().getName();

        // 온라인 플레이어 데이터에서 찾을 수 없었으므로 오프라인 데이터에서 찾는다.
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        if (!userStorage.isPresent())
            return null;

        return userStorage.get().get(uid).map(User::getName).orElse(null);
    }

    /**
     * Get user name
     *
     * @param userInfo {@link UserInfo}
     * @return user name
     */
    public String getName(UserInfo userInfo) {
        return getName(userInfo.getUuid());
    }

    /**
     * Set nickname
     *
     * @param uuid
     * @param nickname
     * @return
     */
    public boolean setNickname(String uuid, String nickname) {
        Optional<UserInfo> userInfo = findUser(uuid);
        if (!userInfo.isPresent())
            return false;

        userInfo.get().setNickname(nickname);
        return true;
    }

    /**
     * Get nickname
     *
     * @param uuid
     * @return
     */
    public String getNickname(String uuid) {
        if (uuid.equals(IDENT_SERVER_CONSOLE)) {
            return "Console";
        } else if (uuid.equals(IDENT_UNKNOWN)) {
            return "Unknown";
        }

        Optional<UserInfo> userInfo = findUser(uuid);
        if (!userInfo.isPresent())
            return null;

        return userInfo.get().getNickname();
    }
}
