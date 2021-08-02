package me.htna.project.chatdecorator.commands;

import me.htna.project.chatdecorator.ChatDecorator;
import me.htna.project.chatdecorator.UserManager;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

import java.sql.SQLException;
import java.util.ArrayList;

public class NicknameCommand extends BaseCommand {

    public final static String SUBPERMISSION = "nickname";
    public final static String[] ALIAS = {"nickname"};

    public NicknameCommand() {
        super(ALIAS, "닉네임을 설정합니다.", SUBPERMISSION);

        elementList = new ArrayList<>();
        elementList.add(GenericArguments.remainingJoinedStrings(Text.of("nickname")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String sourceUuid = getCommandSourceUuid(src);
        String nickname = args.<String>getOne("nickname").get();

        if (StringUtils.isEmpty(nickname)) {
            src.sendMessage(Text.of("닉네임이 올바르지 않습니다."));
            return CommandResult.empty();
        }

        ChatDecorator.getInstance().getLogger().info(
                new StringBuilder().append("Execute nickname command: ")
                        .append(sourceUuid).append(" : ").append(nickname).toString());

        boolean result = UserManager.getInstance().setNickname(sourceUuid, nickname);
        if (result) {
            try {
                ChatDecorator.getInstance().getDb().insertOrUpdateNickname(sourceUuid, nickname);
            } catch (SQLException e) {
                ChatDecorator.getInstance().getLogger().error("Nickname command insert sql failed: " + e);
                e.printStackTrace();
            }
            src.sendMessage(Text.of("닉네임이 변경되었습니다: " + nickname));
            return CommandResult.success();
        } else {
            src.sendMessage(Text.of("닉네임 설정에 실패했습니다. 유저가 올바르지 않습니다."));
            return CommandResult.empty();
        }
    }
}
