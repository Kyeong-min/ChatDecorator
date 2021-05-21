package me.htna.project.chatdecorator.commands;

import me.htna.project.chatdecorator.ChatDecorator;
import me.htna.project.chatdecorator.Config;
import me.htna.project.chatdecorator.database.entities.CHATLOG;
import me.htna.project.chatdecorator.placeholderHandlers.BasePlaceholderHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatLogCommand extends BaseCommand {

    public final static String SUBPERMISSION = "log";
    public final static String[] ALIAS = {"log"};

    public ChatLogCommand() {
        super(ALIAS, "대상 유저의 채팅 로그를 출력합니다.", SUBPERMISSION);

        elementList = new ArrayList<>();
        elementList.add(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))));
        elementList.add(GenericArguments.dateTime(Text.of("datetime")));
        elementList.add(GenericArguments.integer(Text.of("take")));
    }

    private String convertReasonToFormatString(CHATLOG.Reason reason) {
        switch (reason) {
            case ERROR:
                return "&4ERROR";
            case SUCCESS:
                return "&rSUCCESS";
            case MUTE:
                return "&cMUTE";
            case EVENT_CANCEL:
                return "&5CANCEL";
            case PLAYER_NOT_ONLINE:
                return "&1NOT ONLINE";
        }

        return reason.toString();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String sourceUuid = getCommandSourceUuid(src);
        Player player = args.<Player>getOne("player").get();
        LocalDateTime datetime = args.<LocalDateTime>getOne("datetime").get();
        int take = args.<Integer>getOne("take").get();

        ChatDecorator.getInstance().getLogger().info(
                new StringBuilder().append("Execute chatlog command: ")
                        .append(sourceUuid).append(" -> ").append(player.getUniqueId())
                        .append(", ").append(datetime).append(", ").append(take).toString());

        Instant instant = datetime.toInstant(OffsetDateTime.now().getOffset());
        List<CHATLOG> logs;
        try {
            logs = ChatDecorator.getInstance().getDb().selectChatLog(player.getUniqueId().toString(), instant, take);
        } catch (SQLException e) {
            ChatDecorator.getInstance().getLogger().error("ChtLogCommand execute failed: " + e);
            e.printStackTrace();
            return CommandResult.empty();
        }

        String format = Config.getInstance().getDateTimeFormatter();
        int count = 0;
        for (CHATLOG log : logs) {
            StringBuilder sb = new StringBuilder()
                    .append("&c#").append(++count).append("&r ")
                    .append("[&2").append(BasePlaceholderHandler.getDateTimeString(log.getDatetime(), format)).append("&r] ")
                    .append(log.getContent()).append(" >> ").append(convertReasonToFormatString(log.getResult()));

            Text text = TextSerializers.FORMATTING_CODE.deserialize(sb.toString());
            src.sendMessage(text);
        }

        return CommandResult.successCount(count);
    }
}
