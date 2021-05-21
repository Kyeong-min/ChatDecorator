package me.htna.project.chatdecorator.database.entities;

import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * USERINFO Table entity
 */
public class USERINFO {

    @Getter
    @Setter
    private String uuid;

    @Getter
    @Setter
    private Instant join;

    @Getter
    @Setter
    private Instant last;

    @Getter
    @Setter
    private long playtime;

    public USERINFO() {

    }

    public USERINFO(ResultSet set) throws SQLException {
        uuid = set.getString(1);
        join = set.getTimestamp(2).toInstant();
        last = set.getTimestamp(3).toInstant();
        playtime = set.getLong(4);
    }
}
