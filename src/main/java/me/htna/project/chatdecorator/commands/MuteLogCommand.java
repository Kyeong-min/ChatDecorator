package me.htna.project.chatdecorator.commands;

import me.htna.project.chatdecorator.ChatDecorator;
import me.htna.project.chatdecorator.Config;
import me.htna.project.chatdecorator.UserManager;
import me.htna.project.chatdecorator.struct.MuteInfo;
import me.htna.project.chatdecorator.struct.UserInfo;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MuteLogCommand extends BaseCommand{

    public final static String SUBPERMISSION = "mutelog";
    public final static String[] ALIAS = {"mutelog"};

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

        src.sendMessage(Text.of("Not implements"));
        return CommandResult.success();

        /*
        Config config = Config.getInstance();
        List<MuteInfo> muteInfoList = userInfo.get().getMuteInfoList();
        muteInfoList.forEach(x -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Mute source: ").append(userManager.getName(userInfo.get().getUuid()));
            // sb.append(" Mute DateTime: ").append(config.)
        });
        return null;
        */
    }
}
