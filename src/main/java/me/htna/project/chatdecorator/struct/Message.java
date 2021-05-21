package me.htna.project.chatdecorator.struct;

import lombok.Getter;
import lombok.var;
import me.htna.project.chatdecorator.UserManager;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Instant;

public class Message {

    @Getter
    private MessageChannelEvent.Chat raw;

    @Getter
    private Player player;

    @Getter
    private UserInfo userInfo;

    @Getter
    private String userName;

    @Getter
    private Instant joinDate;

    @Getter
    private Instant lastDate;

    @Getter
    private String locale;

    @Getter
    private String loc_X;

    @Getter
    private String loc_Y;

    @Getter
    private String loc_Z;

    @Getter
    private String maxHealth;

    @Getter
    private String currentHealth;

    @Getter
    private String message;

    public Message(Player player) {
        if (player == null)
            return;

        this.player = player;
        var userInfo = UserManager.getInstance().findUser(player);
        if (userInfo.isPresent())
            this.userInfo = userInfo.get();

        userName = player.getName();

        JoinData joinData = player.getJoinData();
        joinDate = joinData.firstPlayed().exists() ? joinData.firstPlayed().get() : null;
        lastDate = joinData.lastPlayed().exists() ? joinData.lastPlayed().get() : null;

        locale = player.getLocale().toString();

        Location<World> loc = player.getLocation();
        loc_X = String.valueOf(loc.getX());
        loc_Y = String.valueOf(loc.getY());
        loc_Z = String.valueOf(loc.getZ());

        HealthData healthData = player.getHealthData();
        maxHealth = healthData.maxHealth().exists() ? healthData.maxHealth().get().toString() : null;
        currentHealth = healthData.health().exists() ? healthData.health().get().toString() : null;
    }

    public Message(MessageChannelEvent.Chat chat) {
        this(chat.getCause().first(Player.class).orElse(null));
        raw = chat;

        message = chat.getRawMessage().toPlain();
    }

    public Message(String userName, String message) {
        this.userName = userName;
        this.message = message;
    }
}
