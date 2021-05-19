package me.htna.project.chatdecorator;

import com.google.inject.Inject;
import lombok.Getter;
import me.htna.project.chatdecorator.commands.MuteCommand;
import me.htna.project.chatdecorator.commands.MuteLogCommand;
import me.htna.project.chatdecorator.commands.ReloadCommand;
import me.htna.project.chatdecorator.commands.UnmuteCommand;
import me.htna.project.chatdecorator.placeholderHandlers.DefaultPlaceholderHandler;
import me.htna.project.chatdecorator.placeholderHandlers.LPPlaceholderHandler;
import me.htna.project.chatdecorator.struct.Message;
import me.htna.project.chatdecorator.struct.UserInfo;
import net.luckperms.api.LuckPerms;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.Instant;
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
     * {@link Logger}
     */
    @Inject
    @Getter
    private Logger logger;

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
                .child(new ReloadCommand().buildSelf(), ReloadCommand.ALIAS);

        if (Config.getInstance().isEnableMute()) {
            specBuilder.child(new MuteCommand().buildSelf(), MuteCommand.ALIAS)
                    .child(new UnmuteCommand().buildSelf(), UnmuteCommand.ALIAS)
                    .child(new MuteLogCommand().buildSelf(), MuteLogCommand.ALIAS);
        }

        CommandSpec spec = specBuilder.build();

        Sponge.getCommandManager().register(this, spec, "chatdecorator", "cd", "CD");
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

    /**
     * GameInitializationEvent handler
     *
     * @param event {@link GameInitializationEvent}
     */
    @Listener
    public void onInitialization(GameInitializationEvent event) {
        logger.debug("ChatDecorator#onInitialization");
        Config.getInstance();

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
        ConfigurationNode node = configManager.createEmptyNode(ConfigurationOptions.defaults());
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
        logger.info("ChatDecorator is run");
    }

    /**
     * ClientConnectionEvent.Join event handler
     *
     * @param event {@link ClientConnectionEvent.Join}
     */
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        logger.debug("ChatDecorator#onJoin");

        // Get player instance.
        Player player = event.getTargetEntity();
        UserManager.getInstance().joinUser(player);

        // Get configuration instance
        Config config = Config.getInstance();

        if (config.isShowWelcomeMessage()) {
            // Get player's first and last join instant.
            Instant first = player.firstPlayed().get();
            Instant last = player.lastPlayed().get();

            if (first.equals(last)) {
                // send welcome message
                Text text = makeText(config.getWelcomeTemplate(), new Message(player));
                player.sendMessage(text);
                return;
            }
        }

        if (config.isShowJoinMessage()) {
            // send join message
            Text text = makeText(config.getJoinTemplate(), new Message(player));
            player.sendMessage(text);
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
        UserManager.getInstance().exitUser(player);
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
        if (event.isCancelled() || !Sponge.getServer().getOnlinePlayers().contains(player))
            return;

        try {
            Optional<UserInfo> userInfo = UserManager.getInstance().findUser(player);
            if (!userInfo.isPresent()) {
                logger.warn("Userinfo not found, uuid: " + player.getUniqueId() + ", name: " + player.getName());
                return;
            }

            Config config = Config.getInstance();
            Message msg = new Message(event);
            if (config.isEnableMute() && userInfo.get().isMute()) {
                Text text = makeText(config.getChatIgnoreTemplate(), msg);
                player.sendMessage(text);
                event.setCancelled(true);
                return;
            }

            Text text = makeText(config.getChatTemplate(), msg);
            event.setMessage(text);
        } catch (Exception e) {
            logger.error("ChatDecorator#onChat occur exception: " + e);
            if (e.getCause() != null)
                e.getCause().printStackTrace();
        }
    }
}
