package me.htna.project.chatdecorator.struct;

import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;

import java.time.Instant;
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
     * User's nickname
     */
    @Getter
    private String nickname;

    /**
     * First join datetime
     */
    @Getter
    private Instant firstJoin;

    /**
     * Last Join datetime
     */
    @Getter
    private Instant lastJoin;

    /**
     * Playtime second
     */
    private long playTime;

    /**
     * Get user's connection status
     */
    @Getter
    private boolean isJoined;

    private List<MuteInfo> muteInfoList;

    /**
     * CTor
     *
     * @param uuid Player's uuid string
     */
    public UserInfo(String uuid, Instant first, Instant last, long playTime) {
        this.uuid = uuid;
        this.firstJoin = first;
        this.lastJoin = last;
        this.playTime = playTime;
        this.nickname = "";

        muteInfoList = new ArrayList<>();
    }

    /**
     * Player joins the server
     *
     * @param time
     * @param playTime
     */
    public void join(Instant time, long playTime) {
        this.lastJoin = time;
        this.playTime = playTime;
        this.isJoined = true;
    }

    /**
     * Player exits from server.
     */
    public void exit() {
        this.isJoined = false;
    }

    /**
     * get play time
     *
     * @return
     */
    public long getPlayTime() {
        return playTime + Instant.now().getEpochSecond() - getLastJoin().getEpochSecond();
    }

    /**
     * get join elapsed time latest session
     *
     * @return
     */
    public long getJoinElapsedTime() {
        return Instant.now().getEpochSecond() - getLastJoin().getEpochSecond();
    }

    /**
     * Set user nickname
     *
     * @param nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public void setMuteInfoList(List<MuteInfo> muteinfos) {
        this.muteInfoList = muteinfos;
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
