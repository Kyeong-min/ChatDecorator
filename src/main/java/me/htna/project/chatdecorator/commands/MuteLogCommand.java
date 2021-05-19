package me.htna.project.chatdecorator.commands;

import me.htna.project.chatdecorator.ChatDecorator;
import me.htna.project.chatdecorator.Config;
import me.htna.project.chatdecorator.UserManager;
import me.htna.project.chatdecorator.placeholderHandlers.BasePlaceholderHandler;
import me.htna.project.chatdecorator.struct.MuteInfo;
import me.htna.project.chatdecorator.struct.UserInfo;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MuteLogCommand extends BaseCommand {

    public final static String SUBPERMISSION = "mutelog";
    public final static String[] ALIAS = {"mutelog"};

    // private final static String mute_placeholder_format = "[&4M&r] (&b%mute_datetime%&r) (&6%mute_source_name%&r)";
    // private final static String unmute_placeholder_format = "[&2U&r] (&b%unmute_datetime%&r) (&6%unmute_source_name%&r)";

    public MuteLogCommand() {
        super(ALIAS, "Unmute user", SUBPERMISSION);

        elementList = new ArrayList<>();
        elementList.add(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String sourceUuid = getCommandSourceUuid(src);
        Player player = args.<Player>getOne("player").get();

        ChatDecorator.getInstance().getLogger().info(
                new StringBuilder().append("Execute MuteLog command: ")
                        .append(sourceUuid).append(" -> ").append(player.getUniqueId())
                        .toString());

        UserManager userManager = UserManager.getInstance();
        Optional<UserInfo> userInfo = userManager.findUser(player);
        if (!userInfo.isPresent()) {
            src.sendMessage(Text.of("유저를 찾을 수 없습니다."));
            return CommandResult.empty();
        }

        List<MuteInfo> muteInfoList = userInfo.get().getMuteInfoList();
        String datetimeFormat = Config.getInstance().getDateTimeFormatter();
        int count = 0;
        for (MuteInfo muteInfo : muteInfoList) {
            StringBuilder sb = new StringBuilder()
                    .append("&c#").append(++count).append("&r ")
                    .append("[&eM&r] (&9").append(BasePlaceholderHandler.getDateTimeString(muteInfo.getMuteDateTime(), datetimeFormat))
                    .append("&r) (&6").append(userManager.getName(muteInfo.getMuteSourceUuid())).append("&r) : &a").append(muteInfo.getReason()).append("&r");
            if (muteInfo.isComplete()) {
                sb.append(" -> ")
                        .append("[&eU&r] (&9").append(BasePlaceholderHandler.getDateTimeString(muteInfo.getUnmuteDateTime(), datetimeFormat))
                        .append("&r) (&6").append(userManager.getName(muteInfo.getUnmuteSourceUuid())).append("&r)");
            }

            Text text = TextSerializers.FORMATTING_CODE.deserialize(sb.toString());
            src.sendMessage(text);
        }

        return CommandResult.success();
    }
}
