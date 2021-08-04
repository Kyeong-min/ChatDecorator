package me.htna.project.chatdecorator.commands;

import me.htna.project.chatdecorator.ChatDecorator;
import me.htna.project.chatdecorator.Config;
import me.htna.project.chatdecorator.TabDecorationManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public class ReloadCommand extends BaseCommand {

    public final static String SUBPERMISSION = "reload";
    public final static String[] ALIAS = {"reload"};

    public ReloadCommand() {
        super(ALIAS, "CharDecorator 설정을 리로드합니다.", SUBPERMISSION);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        ChatDecorator.getInstance().getLogger().info("Execute reload command");
        boolean result = Config.getInstance().reload();
        if (result) {
            // tab decoration 리로드
            TabDecorationManager.getInstance().reload();

            src.sendMessage(Text.of("ChatDecorator 설정을 리로드하였습니다."));
            ChatDecorator.getInstance().getLogger().info("Reload success");
            return CommandResult.success();
        } else {
            src.sendMessage(Text.of("ChatDecorator 설정을 리로드하지 못했습니다."));
            ChatDecorator.getInstance().getLogger().info("Reload failed");
            return CommandResult.empty();
        }
    }
}
