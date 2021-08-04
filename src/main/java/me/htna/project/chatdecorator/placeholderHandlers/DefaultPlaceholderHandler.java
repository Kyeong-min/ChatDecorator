package me.htna.project.chatdecorator.placeholderHandlers;

import me.htna.project.chatdecorator.Config;
import me.htna.project.chatdecorator.HardwareMonitor;
import me.htna.project.chatdecorator.UserManager;
import me.htna.project.chatdecorator.struct.Message;
import me.htna.project.chatdecorator.struct.MuteInfo;
import me.htna.project.chatdecorator.struct.UserInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

/**
 * Built-in default placeholder handler
 * <p>
 * username
 * nicknames
 * joindate
 * lastdate
 * locale
 * loc_x
 * loc_y
 * loc_z
 * maxhealth
 * currenthealth
 * message
 * <p>
 * server_datetime
 * server_date
 * server_time
 * server_ping
 * server_tps
 * server_usecpu
 * server_usecpu_percent
 * server_usemem_gb
 * server_freemem_gb
 * server_totalmem_gb
 * server_usemem_mb
 * server_freemem_mb
 * server_totalmem_mb
 * server_player_count
 * <p>
 * mute_source_name
 * mute_datetime
 * mute_reason
 * unmute_source_name
 * unmute_datetime
 * <p>
 * first_join_datetime
 * last_join_datetime
 * playtime_total_minute
 * playtime_session_minute
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
    private String getLastMuteSourceName(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(x -> UserManager.getInstance().getName(x.getMuteSourceUuid())).orElse(null);
    }

    /**
     * Replace placeholder %mute_datetime%
     *
     * @param target {@link Player}
     * @return Mute datetime string
     */
    private String getLastMuteDateTime(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(x -> getDateTimeString(x.getMuteDateTime(), Config.getInstance().getDateTimeFormatter())).orElse(null);
    }

    /**
     * Replace placeholder %mute_reason%
     *
     * @param target {@link Player}
     * @return Mute reason
     */
    private String getLastMuteReason(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(MuteInfo::getReason).orElse(null);
    }

    /**
     * Replace placeholder %unmute_source_name%
     *
     * @param target {@link Player}
     * @return Unmute source name
     */
    private String getLastUnmuteSourceName(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(x -> UserManager.getInstance().getName(x.getUnmuteSourceUuid())).orElse(null);
    }

    /**
     * Replace placeholder %unmute_datetime%
     *
     * @param target {@link Player}
     * @return Unmute datetime string
     */
    private String getLastUnmuteDateTime(Player target) {
        Optional<MuteInfo> muteInfo = getLastMuteInfo(target);
        return muteInfo.map(x -> getDateTimeString(x.getUnmuteDateTime(), Config.getInstance().getDateTimeFormatter())).orElse(null);
    }

    /**
     * Replase plaseholder %tps%
     *
     * @return TPS
     */
    private double getServerTPS() {
        return Sponge.getServer().getTicksPerSecond();
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
            case "nickname":
                return message.getUserInfo().getNickname();
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
            case "server_ping":
                return String.valueOf(message.getPlayer().getConnection().getLatency());
            case "server_player_count":
                return String.valueOf(Sponge.getServer().getOnlinePlayers().size());
            case "server_datetime":
                return getNowDateTimeString(Config.getInstance().getDateTimeFormatter());
            case "server_date":
                return getNowDateString(Config.getInstance().getDateFormatter());
            case "server_time":
                return getNowTimeString(Config.getInstance().getTimeFormatter());
            case "server_tps":
                return String.format("%.1f", getServerTPS());
            case "server_usecpu":
                return String.format("%.2f", HardwareMonitor.getInstance().getSystemCpuLoad());
            case "server_usecpu_percent":
                return String.format("%.2f %%", HardwareMonitor.getInstance().getSystemCpuLoad() * 100);
            case "server_freemem_gb":
                return String.format("%.2f", (double)(HardwareMonitor.getInstance().getSystemFreeMemory() / 1024f / 1024f / 1024f));
            case "server_totalmem_gb":
                return String.format("%.2f", (double)(HardwareMonitor.getInstance().getSystemTotalMemory() / 1024f / 1024f / 1024f));
            case "server_usemem_gb":
                return String.format("%.2f", (double)(HardwareMonitor.getInstance().getSystemUseMemory() / 1024f / 1024f / 1024f));
            case "server_freemem_mb":
                return String.format("%.2f", (double)(HardwareMonitor.getInstance().getSystemFreeMemory() / 1024f /  1024f));
            case "server_totalmem_mb":
                return String.format("%.2f", (double)(HardwareMonitor.getInstance().getSystemTotalMemory() / 1024f /  1024f));
            case "server_usemem_mb":
                return String.format("%.2f", (double)(HardwareMonitor.getInstance().getSystemUseMemory() / 1024f /  1024f));
            case "mute_source_name":
                return getLastMuteSourceName(message.getPlayer());
            case "mute_reason":
                return getLastMuteReason(message.getPlayer());
            case "mute_datetime":
                return getLastMuteDateTime(message.getPlayer());
            case "unmute_source_name":
                return getLastUnmuteSourceName(message.getPlayer());
            case "unmute_datetime":
                return getLastUnmuteDateTime(message.getPlayer());
            case "first_join_datetime":
                return getDateTimeString(message.getUserInfo().getFirstJoin(), Config.getInstance().getDateTimeFormatter());
            case "last_join_datetime":
                return getDateTimeString(message.getUserInfo().getLastJoin(), Config.getInstance().getDateTimeFormatter());
            case "playtime_total_minute":
                return String.valueOf((int) (message.getUserInfo().getPlayTime() / 60.0f));
            case "playtime_session_minute":
                return String.valueOf((int) (message.getUserInfo().getJoinElapsedTime() / 60.0f));
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
        return placeholder.split("\\.").length == 1;
    }
}
