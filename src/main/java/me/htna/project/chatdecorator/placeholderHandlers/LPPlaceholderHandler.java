package me.htna.project.chatdecorator.placeholderHandlers;

import me.htna.project.chatdecorator.ChatDecorator;
import me.htna.project.chatdecorator.struct.Message;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.spongepowered.api.entity.living.player.Player;

import java.util.OptionalInt;

/**
 * LuckPerms placeholder handler
 *
 *** Group ***
 * %LP.group_displayname% = Display name
 * %LP.group_weight% = weight
 * %LP.group_name% = group name
 *
 *** User ***
 * // TODO:
 * %LP.user_
 */
public class LPPlaceholderHandler implements IPlaceholderHandler {

    /**
     * LuckPerms instance
     */
    private LuckPerms lp;

    /**
     * Parse group type placeholder
     *
     * @param key placeholder key
     * @param group {@link Group}
     * @return corresponding value
     */
    private String parseGroup(String key, Group group) {
        if (group == null)
            return null;

        switch (key) {
            case "displayname": return group.getDisplayName();
            case "weight":
                OptionalInt weight = group.getWeight();
                if (weight.isPresent())
                    return String.valueOf(weight.getAsInt());
                else
                    return null;
            case "name":
                return group.getName();
        }

        return null;
    }

    /**
     * Parse placeholder
     *
     * @param placeholder placeholder key
     * @param message {@link Message}
     * @return corresponding value
     */
    private String parse(String placeholder, Message message) {
        ChatDecorator.getInstance().getLogger().debug("LPPlaceholder#parse: " + placeholder);

        String[] p = placeholder.split("_");
        ChatDecorator.getInstance().getLogger().debug("LPPlaceholder#parse, Major: " + p[0] + ", miner: " + p[1]);

        User user = lp.getPlayerAdapter(Player.class).getUser(message.getPlayer());
        switch (p[0]) {
            // case "user": return parseUser(p[1], user);
            case "group": return parseGroup(p[1], lp.getGroupManager().getGroup(user.getPrimaryGroup()));
        }

        return null;
    }

    /**
     * CTOR
     *
     * @param lp {@link LuckPerms}
     */
    public LPPlaceholderHandler(LuckPerms lp) {
        this.lp = lp;
    }

    /**
     * Return placeholder name
     *
     * @return Placeholder name
     */
    @Override
    public String getPlaceholderHandlerName() {
        ChatDecorator.getInstance().getLogger().debug("LPPlaceholder#getPlaceholderName");
        return "LuckPerms";
    }

    /**
     * Replace the placeholder with the corresponding value.
     *
     * @param placeholder Placeholder key
     * @param message {@link Message}
     * @return corresponding value
     */
    @Override
    public String replace(String placeholder, Message message) {
        ChatDecorator.getInstance().getLogger().debug("LPPlaceholder#replace: " + placeholder);
        String s = placeholder.split("\\.")[1];
        String key = s.substring(0, s.length() - 1);

        return parse(key, message);
    }

    /**
     * Make sure the placeholder is the target of this handler.
     *
     * @param placeholder Placeholder key
     * @return if true, placeholder is the target of this handler.
     */
    @Override
    public boolean IsTargetPlaceholder(String placeholder) {
        ChatDecorator.getInstance().getLogger().debug("LPPlaceholder#IsTargetPlaceholder: " + placeholder);
        String[] split = placeholder.split("\\.");
        if (split.length <= 1)
            return false;
        return split[0].equals("%LP");
    }
}
