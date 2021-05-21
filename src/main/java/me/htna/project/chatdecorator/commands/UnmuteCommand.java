package me.htna.project.chatdecorator.commands;

import me.htna.project.chatdecorator.ChatDecorator;
import me.htna.project.chatdecorator.Config;
import me.htna.project.chatdecorator.TemplateParser;
import me.htna.project.chatdecorator.UserManager;
import me.htna.project.chatdecorator.struct.Message;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

public class UnmuteCommand extends BaseCommand{

    public final static String SUBPERMISSION = "unmute";
    public final static String[] ALIAS = {"unmute"};

    public UnmuteCommand() {
        super(ALIAS, "유저의 뮤트를 해제합니다.", SUBPERMISSION);

        elementList = new ArrayList<>();
        elementList.add(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String sourceUuid = getCommandSourceUuid(src);
        Player player = args.<Player>getOne("player").get();

        String uuid = player.getUniqueId().toString();

        ChatDecorator.getInstance().getLogger().info(
                new StringBuilder().append("Execute unmute command: ")
                        .append(sourceUuid).append(" -> ").append(uuid)
                        .toString());

        boolean result = UserManager.getInstance().unmuteUser(
                uuid, sourceUuid);
        if (result) {
            try {
                ChatDecorator.getInstance().getDb().unmute(uuid, sourceUuid, Instant.now());
            } catch (SQLException e) {
                ChatDecorator.getInstance().getLogger().error("MuteCommand insert sql failed: " + e);
                e.printStackTrace();
            }
            src.sendMessage(Text.of("해당 유저의 뮤트를 해제했습니다."));
            Message msg = new Message(player);
            String formatted = TemplateParser.getInstance().parse(Config.getInstance().getUnmuteTemplate(), msg);
            Text text = TextSerializers.FORMATTING_CODE.deserialize(formatted);
            player.sendMessage(text);

            ChatDecorator.getInstance().getLogger().info("Unmute success");
            return CommandResult.success();
        } else {
            src.sendMessage(Text.of("뮤트를 해제하지 못했습니다. 유저가 뮤트 상태가 아니거나 존재하지 않습니다."));
            ChatDecorator.getInstance().getLogger().info("Unmute success");
            return CommandResult.empty();
        }
    }
}
