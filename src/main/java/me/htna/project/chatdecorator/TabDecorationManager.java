package me.htna.project.chatdecorator;

import lombok.var;
import me.htna.project.chatdecorator.struct.Message;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TabDecorationManager {

    private static TabDecorationManager instance;
    public static TabDecorationManager getInstance() {
        if (instance == null)
            instance = new TabDecorationManager();

        return instance;
    }

    private Task task;

    private boolean enabled;
    private String entryTemplate;

    private List<String> headerList;
    private List<String> footerList;

    private int headerCount;
    private int footerCount;

    private int headerPos;
    private int footerPos;

    private Map<UUID, Text> entryList;

    private TabDecorationManager() {
        reload();
    }

    private void convertEntryName(Player player) {
        Message msg = new Message(player);
        TemplateParser parser = TemplateParser.getInstance();

        Text text = TextSerializers.FORMATTING_CODE.deserialize(parser.parse(entryTemplate, msg));
        entryList.put(player.getUniqueId(), text);
    }

    private void convertEntryNameAll() {
        Sponge.getServer().getOnlinePlayers().stream().forEach(p -> {
           convertEntryName(p);
        });
    }

    private String getHeaderTemplate(int pos) {
        if (pos < headerCount)
            return headerList.get(pos);

        return "";
    }

    private String getFooterTemplate(int pos) {
        if (pos < footerCount)
            return footerList.get(pos);

        return "";
    }

    public void addUser(Player player) {
        convertEntryName(player);
    }

    public void removeUser(Player player) {
        entryList.remove(player.getUniqueId().toString());
    }

    public void reload() {
        if (task != null) {
            stopTask();
        }
        Config config = Config.getInstance();
        enabled = config.isEnableTabDeco();

        entryTemplate = config.getEntryTemplate();
        headerList = config.getHeaderTemplateList();
        footerList = config.getFooterTemplateList();

        headerCount = headerList.size();
        footerCount = footerList.size();

        headerPos = 0;
        footerPos = 0;

        entryList = new HashMap<>();
        convertEntryNameAll();

        if (enabled)
            runTask();
    }

    public void runTask() {
        if (task != null) {
            ChatDecorator.getInstance().getLogger().warn("Tab decoration task already run");
            return;
        }

        Task.Builder taskBuilder = Task.builder().execute(() -> {
            final TemplateParser parser = TemplateParser.getInstance();

            String headerTemplate = getHeaderTemplate(headerPos);
            String footerTemplate = getFooterTemplate(footerPos);

            Server server = Sponge.getServer();
            var players = server.getOnlinePlayers();
            players.stream().forEach(p -> {
                Message msg = new Message(p);
                Text header = TextSerializers.FORMATTING_CODE.deserialize(parser.parse(headerTemplate, msg));
                Text footer = TextSerializers.FORMATTING_CODE.deserialize(parser.parse(footerTemplate, msg));

                var tabList = p.getTabList();
                tabList.setHeaderAndFooter(header, footer);

                var entities = tabList.getEntries();
                entities.forEach(entry -> {
                    GameProfile profile = entry.getProfile();
                    UUID uuid = profile.getUniqueId();

                    Optional<Text> displayName = entry.getDisplayName();
                    Text displayName2 = entryList.get(uuid);

                    if (!displayName.isPresent() || !displayName.equals(displayName2))
                        entry.setDisplayName(entryList.get(uuid));
                });
            });

            headerPos++;
            if (headerPos >= headerCount)
                headerPos = 0;

            footerPos++;
            if (footerPos >= footerCount)
                footerPos = 0;
        });

        task = taskBuilder.async().interval(Config.getInstance().getRefreshRate(), TimeUnit.MILLISECONDS)
                .name("Tab decoration task").submit(ChatDecorator.getInstance().getPlugin());

        ChatDecorator.getInstance().getLogger().info("Run Tab decoration task");
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;

            ChatDecorator.getInstance().getLogger().info("Stop Tab decoration task");
        }
    }

}
