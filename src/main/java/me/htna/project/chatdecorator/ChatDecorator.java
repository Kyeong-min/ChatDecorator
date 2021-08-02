package me.htna.project.chatdecorator;

import com.google.inject.Inject;
import lombok.Getter;
import me.htna.project.chatdecorator.commands.*;
import me.htna.project.chatdecorator.database.H2Embedded;
import me.htna.project.chatdecorator.database.entities.CHATLOG;
import me.htna.project.chatdecorator.database.entities.MUTEINFO;
import me.htna.project.chatdecorator.database.entities.NICKNAME;
import me.htna.project.chatdecorator.database.entities.USERINFO;
import me.htna.project.chatdecorator.placeholderHandlers.DefaultPlaceholderHandler;
import me.htna.project.chatdecorator.placeholderHandlers.LPPlaceholderHandler;
import me.htna.project.chatdecorator.struct.Message;
import me.htna.project.chatdecorator.struct.MuteInfo;
import me.htna.project.chatdecorator.struct.UserInfo;
import net.luckperms.api.LuckPerms;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Plugin(
        id = "chatdecorator",
        name = "ChatDecorator",
        description = "This plugin is decorate chat format in multi play server.",
        authors = {
                "HATENA"
        }
)
public class ChatDecorator {

    /**
     * Instance
     */
    private static ChatDecorator instance;
    /**
     * LuckPerms provider
     */
    private ProviderRegistration<LuckPerms> luckPerms;
    /**
     * {@link ConfigurationLoader}
     */
    @Inject
    @DefaultConfig(sharedRoot = false)
    @Getter
    private ConfigurationLoader<CommentedConfigurationNode> configManager;
    /**
     * {@link Game}
     */
    @Inject
    private Game game;
    /**
     * {@link Logger}
     */
    @Inject
    @Getter
    private Logger logger;

    @Inject
    private PluginContainer plugin;

    @Getter
    private H2Embedded db;

    public ChatDecorator() {
        ChatDecorator.instance = this;
    }

    public static ChatDecorator getInstance() {
        return instance;
    }

    /**
     * Register commands
     */
    private void registerCommands() {
        logger.debug("ChatDecorator#registerCommands");
        CommandSpec.Builder specBuilder = CommandSpec.builder()
                .description(Text.of("Root of ChatDecorator command"))
                .permission("hatena.chatdecorator")
                .child(new ReloadCommand().buildSelf(), ReloadCommand.ALIAS)
                .child(new ChatLogCommand().buildSelf(), ChatLogCommand.ALIAS)
                .child(new NicknameCommand().buildSelf(), NicknameCommand.ALIAS);

        if (Config.getInstance().isEnableMute()) {
            specBuilder.child(new MuteCommand().buildSelf(), MuteCommand.ALIAS)
                    .child(new UnmuteCommand().buildSelf(), UnmuteCommand.ALIAS)
                    .child(new MuteLogCommand().buildSelf(), MuteLogCommand.ALIAS);
        }

        CommandSpec spec = specBuilder.build();

        Sponge.getCommandManager().register(this, spec, "chatdecorator", "cd");
    }

    /**
     * Generate {@link Text} from Placeholder template string
     *
     * @param template Placeholder template string
     * @param msg      {@link Message}
     * @return {@link Text}
     */
    private Text makeText(String template, Message msg) {
        TemplateParser parser = TemplateParser.getInstance();
        String formatted = parser.parse(template, msg);
        return TextSerializers.FORMATTING_CODE.deserialize(formatted);
    }

    private void initDbAsset() {
        String dbFile = "chatdeco.h2.mv.db";
        Path path = Paths.get(game.getGameDirectory().toString(), "chatdecorator");
        Path filepath = Paths.get(path.toString(), dbFile);
        if (Files.notExists(filepath)) {
            Optional<Asset> asset = plugin.getAsset(dbFile);
            if (!asset.isPresent()) {
                logger.error("DB Asset not founded!!!");
            }

            // check folder
            if (Files.notExists(path)) {
                try {
                    Files.createDirectory(path);
                } catch (IOException e) {
                    logger.error("Failed create db folder");
                    e.printStackTrace();
                    return;
                }
            }

            // copy db file from asset
            try {
                asset.get().copyToFile(filepath);
            } catch (IOException e) {
                logger.error("Failed copy db asset to disk");
                e.printStackTrace();
            }
        }
    }

    /**
     * GameInitializationEvent handler
     *
     * @param event {@link GameInitializationEvent}
     */
    @Listener
    public void onInitialization(GameInitializationEvent event) {
        logger.debug("ChatDecorator#onInitialization");

        Config.getInstance();
        initDbAsset();

        db = new H2Embedded();
        db.connect(Paths.get(game.getGameDirectory().toString(), "chatdecorator", "chatdeco.h2"));

        try {
            TemplateParser.getInstance().addPlaceholder(new DefaultPlaceholderHandler());
        } catch (Exception ex) {
            logger.error("Default placeholder handler add exception: " + ex);
        }

        registerCommands();
    }

    /**
     * GameStartedServerEvent handler
     *
     * @param event {@link GameStartedServerEvent}
     */
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.debug("ChatDecorator#onServerStart");
        try {
            Optional<ProviderRegistration<LuckPerms>> provider = Sponge.getServiceManager().getRegistration(LuckPerms.class);
            if (provider.isPresent()) {
                logger.info("Found luckPerms");
                luckPerms = provider.get();
                LPPlaceholderHandler lpPlaceholder = new LPPlaceholderHandler(luckPerms.getProvider());
                try {
                    TemplateParser.getInstance().addPlaceholder(lpPlaceholder);
                } catch (Exception ex) {
                    logger.error(ex.toString());
                }
            }
        } catch (java.lang.NoClassDefFoundError ex) {
        }
        logger.info("ChatDecorator is run");
    }

    @Listener
    public void onServerStopped(GameStoppedServerEvent event) {
        if (db != null)
            db.disconnect();
    }

    /**
     * ClientConnectionEvent.Join event handler
     *
     * @param event {@link ClientConnectionEvent.Join}
     */
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        logger.debug("ChatDecorator#onJoin");
        Player player = event.getTargetEntity();
        String uuid = player.getUniqueId().toString();

        Instant first = player.firstPlayed().get();
        Instant last = player.lastPlayed().get();

        // Get configuration instance
        Config config = Config.getInstance();
        try {
            Optional<USERINFO> table = db.selectUserInfo(uuid);
            if (table.isPresent()) {
                db.updateUserInfo(uuid, last);
                UserInfo userinfo = UserManager.getInstance().joinUser(uuid, first, last, table.get().getPlaytime());

                // 닉네임 정보 처리
                Optional<NICKNAME> nickname = db.selectNickname(uuid);
                nickname.ifPresent(x -> userinfo.setNickname(x.getNickname()));

                // 뮤트 정보 처리
                List<MUTEINFO> muteInfos = db.selectMuteInfo(uuid);
                List<MuteInfo> muteInfos_ = new ArrayList<>();
                for (MUTEINFO muteInfo : muteInfos)
                    muteInfos_.add(new MuteInfo(muteInfo));

                userinfo.setMuteInfoList(muteInfos_);

                // 뮤트 상태라면 유저에게 통지
                if (userinfo.isMute()) {
                    Message msg = new Message(player);
                    Text text = makeText(config.getChatIgnoreTemplate(), msg);
                    player.sendMessage(text);
                }

                if (config.isShowJoinMessage())    // send join message
                    player.sendMessage(makeText(config.getJoinTemplate(), new Message(player)));
            } else {
                logger.info("Join new player: " + uuid);
                db.insertUserInfo(player.getUniqueId().toString(), first, last);
                UserManager.getInstance().joinUser(uuid, first, last, 0);

                if (config.isShowWelcomeMessage()) // send welcome message
                    player.sendMessage(makeText(config.getWelcomeTemplate(), new Message(player)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * ClientConnectionEvent.Disconnect event handler
     *
     * @param event {@link ClientConnectionEvent.Disconnect}
     */
    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        logger.debug("ChatDecorator#onDisconnect");
        Player player = event.getTargetEntity();
        Optional<UserInfo> userinfo = UserManager.getInstance().exitUser(player);
        userinfo.ifPresent(x -> {
            long elapsed = x.getPlayTime() + Instant.now().getEpochSecond() - x.getLastJoin().getEpochSecond();
            logger.info("Player disconnect, playtime: " + elapsed);
            try {
                db.updateUserPlaytime(x.getUuid(), elapsed);
            } catch (SQLException e) {
                logger.error("User playtime update failed: " + e);
                e.printStackTrace();
            }
        });
    }

    /**
     * MessageChannelEvent.Chat event handler
     * <p>
     * If the event has already been canceled or sender player is not online player,
     * this handler does nothing.
     *
     * @param event  {@link MessageChannelEvent.Chat}
     * @param player {@link Player}
     */
    @Listener
    public void onChat(MessageChannelEvent.Chat event, @First Player player) {
        logger.debug("ChatDecorator#onChat");
        if (event.isCancelled()) {
            insertChatLog(event, player, CHATLOG.Reason.EVENT_CANCEL);
            return;
        }

        if (!Sponge.getServer().getOnlinePlayers().contains(player)) {
            insertChatLog(event, player, CHATLOG.Reason.PLAYER_NOT_ONLINE);
            return;
        }

        try {
            Optional<UserInfo> userInfo = UserManager.getInstance().findUser(player);
            if (!userInfo.isPresent()) {
                logger.warn("Userinfo not found, uuid: " + player.getUniqueId() + ", name: " + player.getName());
                insertChatLog(event, player, CHATLOG.Reason.ERROR);
                return;
            }

            Config config = Config.getInstance();
            Message msg = new Message(event);
            if (config.isEnableMute() && userInfo.get().isMute()) {
                insertChatLog(event, player, CHATLOG.Reason.MUTE);
                Text text = makeText(config.getChatIgnoreTemplate(), msg);
                player.sendMessage(text);
                event.setCancelled(true);
                return;
            }

            Text text = makeText(config.getChatTemplate(), msg);
            event.setMessage(text);
            insertChatLog(event, player, CHATLOG.Reason.SUCCESS);
        } catch (Exception e) {
            logger.error("ChatDecorator#onChat occur exception: " + e);
            if (e.getCause() != null)
                e.getCause().printStackTrace();
        }
    }

    private void insertChatLog(MessageChannelEvent.Chat event, @First Player player, CHATLOG.Reason reason) {
        try {
            db.insertChatLog(player.getUniqueId().toString(), event.getRawMessage().toPlain(), Instant.now(), reason);
        } catch (SQLException e) {
            logger.error("Chat log write error: " + e);
            e.printStackTrace();
        }
    }
}
