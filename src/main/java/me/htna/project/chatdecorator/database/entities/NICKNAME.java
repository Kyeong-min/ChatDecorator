package me.htna.project.chatdecorator.database.entities;

import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class NICKNAME {

    @Getter
    @Setter
    private String uuid;

    @Getter
    @Setter
    private String nickname;

    @Getter
    @Setter
    private Instant datetime;

    public NICKNAME() {

    }

    public NICKNAME(ResultSet set) throws SQLException {
        uuid = set.getString(1);
        nickname = set.getString(2);
        datetime = set.getTimestamp(3).toInstant();
    }
}
