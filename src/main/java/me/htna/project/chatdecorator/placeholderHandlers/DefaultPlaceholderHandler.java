package me.htna.project.chatdecorator.placeholderHandlers;

import me.htna.project.chatdecorator.ChatDecorator;
import me.htna.project.chatdecorator.Config;
import me.htna.project.chatdecorator.UserManager;
import me.htna.project.chatdecorator.struct.Message;
import me.htna.project.chatdecorator.struct.MuteInfo;
import me.htna.project.chatdecorator.struct.UserInfo;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

/**
 * Built-in default placeholder handler
 * <p>
 * username
 * joindate
 * lastdate
 * locale
 * loc_x
 * loc_y
 * loc_z
 * maxhealth
 * currenthealth
 * message
 * serverdatetime
 * serverdate
 * servertime
 * <p>
 * mute_source_name
 * mute_datetime
 * mute_reason
 * unmute_source_name
 * unmute_datetime
 */
public class DefaultPlaceholderHandler extends BasePlaceholderHandler {

    /**
     * Get target player's last mute info
     *
     * @param target {@link Player}
     * @return Last mute info
     */
    private Optional<MuteInfo> getLastMuteInfo(Player target) {
        Optional<UserInfo> userInfo = UserManager.getInstance().findUser(target);
        if (!userInfo.isPresent())
            return Optional.empty();

        Optional<MuteInfo> muteInfo = userInfo.get().getLastMuteInfo();
        if (!muteInfo.isPresent())
            return Optional.empty();

        return muteInfo;
    }

    /**
     * Replace placeholder %mute_source_name%
     *
     * @param target {@link Player}
     * @return Mute source name
     */
    private String getMuteSourceName(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(x -> UserManager.getInstance().getName(x.getMuteSourceUuid())).orElse(null);
    }

    /**
     * Replace placeholder %mute_datetime%
     *
     * @param target {@link Player}
     * @return Mute datetime string
     */
    private String getMuteDateTime(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(x -> getDateTimeString(x.getMuteDateTime(), Config.getInstance().getDateTimeFormatter())).orElse(null);
    }

    /**
     * Replace placeholder %mute_reason%
     *
     * @param target {@link Player}
     * @return Mute reason
     */
    private String getMuteReason(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(MuteInfo::getReason).orElse(null);
    }

    /**
     * Replace placeholder %unmute_source_name%
     *
     * @param target {@link Player}
     * @return Unmute source name
     */
    private String getUnmuteSourceName(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(x -> UserManager.getInstance().getName(x.getUnmuteSourceUuid())).orElse(null);
    }

    /**
     * Replace placeholder %unmute_datetime%
     *
     * @param target {@link Player}
     * @return Unmute datetime string
     */
    private String getUnmuteDateTime(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(x -> getDateTimeString(x.getUnmuteDateTime(), Config.getInstance().getDateTimeFormatter())).orElse(null);
    }

    /**
     * Parse and replace placeholder
     *
     * @param placeholder Placeholder key
     * @param message     {@link Message}
     * @return corresponding value
     */
    private String parse(String placeholder, Message message) {
        String key = placeholder.toLowerCase().substring(1, placeholder.length() - 1);
        switch (key) {
            case "username":
                return message.getUserName();
            case "joindate":
                return getDateTimeString(message.getJoinDate(), Config.getInstance().getDateTimeFormatter());
            case "lastdate":
                return getDateTimeString(message.getLastDate(), Config.getInstance().getDateTimeFormatter());
            case "locale":
                return message.getLocale();
            case "loc_x":
                return message.getLoc_X();
            case "loc_y":
                return message.getLoc_Y();
            case "loc_z":
                return message.getLoc_Z();
            case "maxhealth":
                return message.getMaxHealth();
            case "currenthealth":
                return message.getCurrentHealth();
            case "message":
                return message.getMessage();
            case "serverdatetime":
                return getNowDateTimeString(Config.getInstance().getDateTimeFormatter());
            case "serverdate":
                return getNowDateString(Config.getInstance().getDateFormatter());
            case "servertime":
                return getNowTimeString(Config.getInstance().getTimeFormatter());
            case "mute_source_name":
                return getMuteSourceName(message.getPlayer());
            case "mute_reason":
                return getMuteReason(message.getPlayer());
            case "mute_datetime":
                return getMuteDateTime(message.getPlayer());
            case "unmute_source_name":
                return getUnmuteSourceName(message.getPlayer());
            case "unmute_datetime":
                return getUnmuteDateTime(message.getPlayer());
        }

        return null;
    }

    /**
     * Return placeholder name
     *
     * @return Placeholder name
     */
    @Override
    public String getPlaceholderHandlerName() {
        ChatDecorator.getInstance().getLogger().debug("DefaultPlaceholder#getPlaceholderName");
        return "Default";
    }

    /**
     * Replace the placeholder with the corresponding value.
     *
     * @param placeholder Placeholder key
     * @param message     {@link Message}
     * @return corresponding value
     */
    @Override
    public String replace(String placeholder, Message message) {
        ChatDecorator.getInstance().getLogger().debug("DefaultPlaceholder#replace: " + placeholder);
        return parse(placeholder, message);
    }

    /**
     * Make sure the placeholder is the target of this handler.
     *
     * @param placeholder Placeholder key
     * @return if true, placeholder is the target of this handler.
     */
    @Override
    public boolean IsTargetPlaceholder(String placeholder) {
        ChatDecorator.getInstance().getLogger().debug("DefaultPlaceholder#IsTargetPlaceHolder: " + placeholder);
        return placeholder.split("\\.").length == 1;
    }
}
