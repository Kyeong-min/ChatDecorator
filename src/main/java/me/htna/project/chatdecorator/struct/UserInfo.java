package me.htna.project.chatdecorator.struct;

import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserInfo {
    /**
     * User's uuid
     */
    @Getter
    private final String uuid;

    /**
     * Get user's connection status
     */
    @Getter
    private boolean isJoined;

    private final List<MuteInfo> muteInfoList;

    /**
     * CTor
     *
     * @param player {@link Player}
     */
    public UserInfo(Player player) {
        this(player.getUniqueId());
    }

    /**
     * CTor
     *
     * @param uuid Player's uuid
     */
    public UserInfo(UUID uuid) {
        this(uuid.toString());
    }

    /**
     * CTor
     *
     * @param uuid Player's uuid string
     */
    public UserInfo(String uuid) {
        this.uuid = uuid;
        muteInfoList = new ArrayList<>();
    }

    /**
     * Player joins the server
     */
    public void join() {
        this.isJoined = true;
    }

    /**
     * Player exits from server.
     */
    public void exit() {
        this.isJoined = false;
    }

    /**
     * Get last mute info
     *
     * @return {@link Optional} Last mute info
     */
    public Optional<MuteInfo> getLastMuteInfo() {
        return muteInfoList.stream().reduce((f, s) -> s);
    }

    /**
     * Get mute info list
     *
     * @return Mute info list
     */
    public List<MuteInfo> getMuteInfoList() {
        return muteInfoList;
    }

    /**
     * Return whether the current user is muted.
     *
     * @return
     */
    public boolean isMute() {
        return getLastMuteInfo().map(MuteInfo::isMute).orElse(false);
    }

    /**
     * Mute this user
     *
     * @param sourceUuid The uuid of the source who muted this user
     * @param reason    Reason for mute
     * @return if true, success mute
     */
    public boolean mute(String sourceUuid, String reason) {
        if (isMute())
            return false;

        MuteInfo muteInfo = new MuteInfo();
        try {
            muteInfo.mute(sourceUuid, reason);
        } catch (IllegalStateException ex) {
            return false;
        }
        muteInfoList.add(muteInfo);
        return true;
    }

    /**
     * Unmute the user
     *
     * @param sourceUuid The uuid of the source who unmuted this user
     * @return If true, unmute success
     */
    public boolean unmute(String sourceUuid) {
        if (!isMute())
            return false;

        return getLastMuteInfo().map(x -> {
            x.unmute(sourceUuid);
            return true;
        }).orElse(false);
    }
}
