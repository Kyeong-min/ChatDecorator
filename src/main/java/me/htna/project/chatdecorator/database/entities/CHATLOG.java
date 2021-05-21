package me.htna.project.chatdecorator.database.entities;

import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

public class CHATLOG {
    @Getter
    @Setter
    String uuid;

    @Getter
    @Setter
    Instant datetime;

    @Getter
    @Setter
    String content;

    @Getter
    @Setter
    Reason result;

    public CHATLOG() {

    }

    public CHATLOG(ResultSet rs) throws SQLException {
        uuid = rs.getString(1);
        datetime = rs.getTimestamp(2).toInstant();
        content = rs.getString(3);
        result = Reason.valueOf(rs.getInt(4)).orElse(Reason.ERROR);
    }

    public enum Reason {
        ERROR(-1),
        SUCCESS(0),
        MUTE(1),
        EVENT_CANCEL(2),
        PLAYER_NOT_ONLINE(3);

        @Getter
        private final int value;

        Reason(int value) {
            this.value = value;
        }

        public static Optional<Reason> valueOf(int value) {
            return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
        }

        @Override
        public String toString() {
            return name();
        }
    }

}
