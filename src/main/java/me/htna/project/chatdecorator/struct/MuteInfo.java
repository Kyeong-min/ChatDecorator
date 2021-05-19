package me.htna.project.chatdecorator.struct;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Player mute info class
 */
public class MuteInfo {
    /**
     * The uuid of the source who muted this
     */
    @Getter
    private String muteSourceUuid;

    /**
     * The uuid of the source who unmuted this
     */
    @Getter
    private String unmuteSourceUuid;

    /**
     * Reason of mute
     */
    @Getter
    private String reason;

    /**
     * Mute datetime
     */
    @Getter
    private Instant muteDateTime;

    /**
     * Unmute datetime
     */
    @Getter
    private Instant unmuteDateTime;

    /**
     * Get mute status
     *
     * @return if true, muted
     */
    public boolean isMute() {
        return muteDateTime != null && unmuteDateTime == null;
    }

    /**
     * Verify that the muteinfo is complete.
     *
     * Completed means that it has been muted and unmuted.
     *
     * @return
     */
    public boolean isComplete() {
        return muteDateTime != null && unmuteDateTime != null;
    }

    /**
     * Mute
     *
     * @param sourceUuid Source uuid
     * @param reason     Reason of mute
     * @throws IllegalStateException Already muted
     */
    public void mute(String sourceUuid, String reason) throws IllegalStateException {
        if (muteDateTime != null) {
            throw new IllegalStateException("Already muted");
        }
        this.muteSourceUuid = sourceUuid;
        this.reason = reason;
        muteDateTime = Instant.now();
    }

    /**
     * Unmute
     *
     * @param sourceUuid source uuid
     * @throws IllegalStateException Not muted
     */
    public void unmute(String sourceUuid) throws IllegalStateException {
        if (muteDateTime == null) {
            throw new IllegalStateException("Not muted");
        }
        unmuteSourceUuid = sourceUuid;
        unmuteDateTime = Instant.now();
    }
}
