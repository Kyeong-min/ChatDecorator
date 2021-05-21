package me.htna.project.chatdecorator.database.entities;

import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class MUTEINFO {
    @Getter
    @Setter
    private int idx;
    @Getter
    @Setter
    private String uuid;
    @Getter
    @Setter
    private String mute_source_uuid;
    @Getter
    @Setter
    private Instant mute_datetime;
    @Getter
    @Setter
    private String reason;
    @Getter
    @Setter
    private Optional<String> unmute_source_uuid;
    @Getter
    @Setter
    private Optional<Instant> unmute_datetime;

    public MUTEINFO(){}
    public MUTEINFO(ResultSet set) throws SQLException {
        idx = set.getInt(1);
        uuid = set.getString(2);
        mute_source_uuid = set.getString(3);
        mute_datetime = set.getTimestamp(4).toInstant();
        reason = set.getString(5);
        unmute_source_uuid = Optional.ofNullable(set.getString(6));
        Optional<Timestamp> opt = Optional.ofNullable(set.getTimestamp(7));
        if (opt.isPresent()) {
            unmute_datetime = Optional.ofNullable(opt.get().toInstant());
        } else {
            unmute_datetime = Optional.empty();
        }
    }
}
