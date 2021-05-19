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

import java.util.ArrayList;
import java.util.UUID;

public class MuteCommand extends BaseCommand{

    public final static String SUBPERMISSION = "mute";
    public final static String[] ALIAS = {"mute"};

    public MuteCommand() {
        super(ALIAS, "유저를 뮤트합니다.", SUBPERMISSION);

        elementList = new ArrayList<>();
        elementList.add(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))));
        elementList.add(GenericArguments.remainingJoinedStrings(Text.of("reason")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String sourceUuid = getCommandSourceUuid(src);
        Player player = args.<Player>getOne("player").get();
        String reason = args.<String>getOne("reason").get();

        ChatDecorator.getInstance().getLogger().info(
                new StringBuilder().append("Execute mute command: ")
                .append(sourceUuid).append(" -> ").append(player.getUniqueId())
                .append(" (").append(reason).append(')').toString());
        
        boolean result = UserManager.getInstance().muteUser(
                player.getUniqueId().toString(),
                sourceUuid, reason);
        if (result) {
            src.sendMessage(Text.of("해당 유저를 뮤트했습니다."));
            Message msg = new Message(player);
            String formatted = TemplateParser.getInstance().parse(Config.getInstance().getMuteTemplate(), msg);
            Text text = TextSerializers.FORMATTING_CODE.deserialize(formatted);
            player.sendMessage(text);

            ChatDecorator.getInstance().getLogger().info("Mute success");
            return CommandResult.success();
        } else {
            src.sendMessage(Text.of("뮤트하지 못했습니다. 유저가 이미 뮤트 상태이거나 존재하지 않습니다."));
            ChatDecorator.getInstance().getLogger().info("Mute failed");
            return CommandResult.empty();
        }
    }
}
