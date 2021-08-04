package me.htna.project.chatdecorator;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatDecorator global configuration
 *
 * !!! IMPORTANT: 절대 Config 인스턴스 또는 필드를 캐싱하지 말 것 !!!
 * 만약 스펀지 플러그인 리로드를 하거나 ChatDecorator 플러그인의 커멘드로 전역설정을 재설정하면
 * Config 클래스의 싱글턴 인스턴트를 완전히 새로 작성하고 대체하므로
 * 이 인스턴스 혹은 필드를 캐싱하면 리로드(재설정)된 내용이 반영되지 않을 수 있음
 *
 * 캐싱한다면 reload시에 적절하게 캐싱을 다시 할 필요가 있음
 *
 */
public class Config {

    public static class KEYSTORE {
        public static final String GLOBAL = "global";
        public static final String DEFAULTPLACEHOLDERCORRESPONDINGVALUE = "defaultPlaceholderCorrespondingValue";
        public static final String DATETIMEFORMATTER = "dateTimeFormatter";
        public static final String DATEFORMATTER = "dateFormatter";
        public static final String TIMEFORMATTER = "timeFormatter";

        public static final String CHATDECORATE = "chatDecorate";
        public static final String USECHATDECORATOR = "useChatDecorator";
        public static final String CHATTEMPLATE = "chatTemplate";

        public static final String WELCOMEMESSAGE = "welcomeMessage";
        public static final String SHOWWELCOMEMESSAGE = "showWelcomeMessage";
        public static final String WELCOMETEMPLATE = "welcomeTemplate";

        public static final String JOINMESSAGE = "joinMessage";
        public static final String SHOWJOINMESSAGE = "showJoinMessage";
        public static final String JOINTEMPLATE = "joinTemplate";

        public static final String MUTE = "mute";
        public static final String ENABLEMUTE = "enableMute";
        public static final String MUTETEMPLATE = "muteTemplate";
        public static final String UNMUTETEMPLATE = "unmuteTemplate";
        public static final String CHATIGNORETEMPLATE = "chatIgnoreTemplate";

        public static final String TABDECO = "tabDecoration";
        public static final String ENABLETABDECO = "enableTabDeco";
        public static final String HEADER = "header";
        public static final String FOOTER = "footer";
        public static final String REFRESHRATE = "refreshRate";
        public static final String ENTRYTEMPLATE = "entryTemplate";
    }

    private static Config instance;
    public static Config getInstance() {
        if (instance == null)
            instance = new Config();

        return instance;
    }

    @Getter
    private boolean isLoadCompleted;

    /**
     * Default placeholder corresponding value
     * if this value is empty string, replace to placeholder string.
     */
    @Getter
    private String defaultPlaceholderCorrespondingValue;

    @Getter
    private String dateTimeFormatter;

    @Getter
    private String dateFormatter;

    @Getter
    private String timeFormatter;

    @Getter
    private boolean useChatDecorator;

    @Getter
    private String chatTemplate;

    @Getter
    private boolean showWelcomeMessage;

    @Getter
    private String welcomeTemplate;

    @Getter
    private boolean showJoinMessage;

    @Getter
    private String joinTemplate;

    @Getter
    private boolean enableMute;

    @Getter
    private String muteTemplate;

    @Getter
    private String unmuteTemplate;

    @Getter
    private String chatIgnoreTemplate;

    @Getter
    private boolean enableTabDeco;

    @Getter
    private List<String> headerTemplateList;

    @Getter
    private List<String> footerTemplateList;

    @Getter
    private String entryTemplate;

    @Getter
    private int refreshRate;

    private Config() {
        isLoadCompleted = false;
        load();
    }

    private void loadDefault(boolean save) {
        ChatDecorator plugin = ChatDecorator.getInstance();
        plugin.getLogger().info("Load default configuration");
        defaultPlaceholderCorrespondingValue = "";
        dateTimeFormatter = "yyyy-MM-dd HH:mm:ss";
        dateFormatter = "yyyy-MM-dd";
        timeFormatter = "HH:mm:ss";

        useChatDecorator = true;
        chatTemplate = "<%username%> : %message%";

        showWelcomeMessage = false;
        welcomeTemplate = "";

        showJoinMessage = false;
        joinTemplate = "";
        isLoadCompleted = true;

        enableMute = true;
        muteTemplate = "<SERVER> : You were muted by %mute_source_name%, reason: %mute_reason%";
        unmuteTemplate = "<SERVER> : You were unmuted by %unmute_source_name%";
        chatIgnoreTemplate = "<SERVER> : You were muted by %mute_source_name% at %mute_datetime%, reason: %mute_reason%";

        enableTabDeco = false;
        headerTemplateList = new ArrayList<>();
        footerTemplateList = new ArrayList<>();
        entryTemplate = "%username%";
        refreshRate = 1000;

        if (save)
            save();
    }

    /**
     * Load configuration.
     */
    public void load() {
        ChatDecorator plugin = ChatDecorator.getInstance();
        plugin.getLogger().info("Load configuration");
        try {
            ConfigurationNode configRoot = plugin.getConfigManager().load();
            if (configRoot.isEmpty()) {
                plugin.getLogger().info("Configuration is empty, load default configuration");
                loadDefault(true);
                return;
            }

            ConfigurationNode globalNode = configRoot.getNode(KEYSTORE.GLOBAL);
            defaultPlaceholderCorrespondingValue = globalNode.getNode(KEYSTORE.DEFAULTPLACEHOLDERCORRESPONDINGVALUE).getString();
            dateTimeFormatter = globalNode.getNode(KEYSTORE.DATETIMEFORMATTER).getString();
            dateFormatter = globalNode.getNode(KEYSTORE.DATEFORMATTER).getString();
            timeFormatter = globalNode.getNode(KEYSTORE.TIMEFORMATTER).getString();

            ConfigurationNode decorateNode = configRoot.getNode(KEYSTORE.CHATDECORATE);
            useChatDecorator = decorateNode.getNode(KEYSTORE.USECHATDECORATOR).getBoolean();
            chatTemplate = decorateNode.getNode(KEYSTORE.CHATTEMPLATE).getString();

            ConfigurationNode welcomeMsgNode = configRoot.getNode(KEYSTORE.WELCOMEMESSAGE);
            showWelcomeMessage = welcomeMsgNode.getNode(KEYSTORE.SHOWWELCOMEMESSAGE).getBoolean();
            welcomeTemplate = welcomeMsgNode.getNode(KEYSTORE.WELCOMETEMPLATE).getString();

            ConfigurationNode joinMsgNode = configRoot.getNode(KEYSTORE.JOINMESSAGE);
            showJoinMessage = joinMsgNode.getNode(KEYSTORE.SHOWJOINMESSAGE).getBoolean();
            joinTemplate = joinMsgNode.getNode(KEYSTORE.JOINTEMPLATE).getString();

            ConfigurationNode muteNode = configRoot.getNode(KEYSTORE.MUTE);
            enableMute = muteNode.getNode(KEYSTORE.ENABLEMUTE).getBoolean();
            muteTemplate = muteNode.getNode(KEYSTORE.MUTETEMPLATE).getString();
            unmuteTemplate = muteNode.getNode(KEYSTORE.UNMUTETEMPLATE).getString();
            chatIgnoreTemplate = muteNode.getNode(KEYSTORE.CHATIGNORETEMPLATE).getString();

            ConfigurationNode tabDecoNode = configRoot.getNode(KEYSTORE.TABDECO);
            enableTabDeco = tabDecoNode.getNode(KEYSTORE.ENABLETABDECO).getBoolean();
            headerTemplateList = tabDecoNode.getNode(KEYSTORE.HEADER).getList(TypeToken.of(String.class));
            footerTemplateList = tabDecoNode.getNode(KEYSTORE.FOOTER).getList(TypeToken.of(String.class));
            entryTemplate = tabDecoNode.getNode(KEYSTORE.ENTRYTEMPLATE).getString();
            refreshRate = tabDecoNode.getNode(KEYSTORE.REFRESHRATE).getInt();

            isLoadCompleted = true;
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("An error occurred while loading this configuration: " + ex);
            plugin.getLogger().warn("Load default configurations.");

            loadDefault(false);
        }
    }

    /**
     * Save configuration.
     */
    public void save() {
        ChatDecorator plugin = ChatDecorator.getInstance();
        plugin.getLogger().info("Save configuration");
        try {
            ConfigurationNode configRoot = plugin.getConfigManager().load();

            ConfigurationNode globalNode = configRoot.getNode(KEYSTORE.GLOBAL);
            globalNode.getNode(KEYSTORE.DEFAULTPLACEHOLDERCORRESPONDINGVALUE).setValue(defaultPlaceholderCorrespondingValue);
            globalNode.getNode(KEYSTORE.DATETIMEFORMATTER).setValue(dateTimeFormatter);
            globalNode.getNode(KEYSTORE.DATEFORMATTER).setValue(dateFormatter);
            globalNode.getNode(KEYSTORE.TIMEFORMATTER).setValue(timeFormatter);

            ConfigurationNode decorateNode = configRoot.getNode(KEYSTORE.CHATDECORATE);
            decorateNode.getNode(KEYSTORE.USECHATDECORATOR).setValue(useChatDecorator);
            decorateNode.getNode(KEYSTORE.CHATTEMPLATE).setValue(chatTemplate);

            ConfigurationNode welcomeMsgNode = configRoot.getNode(KEYSTORE.WELCOMEMESSAGE);
            welcomeMsgNode.getNode(KEYSTORE.SHOWWELCOMEMESSAGE).setValue(showWelcomeMessage);
            welcomeMsgNode.getNode(KEYSTORE.WELCOMETEMPLATE).setValue(welcomeTemplate);

            ConfigurationNode joinMsgNode = configRoot.getNode(KEYSTORE.JOINMESSAGE);
            joinMsgNode.getNode(KEYSTORE.SHOWJOINMESSAGE).setValue(showJoinMessage);
            joinMsgNode.getNode(KEYSTORE.JOINTEMPLATE).setValue(joinTemplate);

            ConfigurationNode muteNode = configRoot.getNode(KEYSTORE.MUTE);
            muteNode.getNode(KEYSTORE.ENABLEMUTE).setValue(enableMute);
            muteNode.getNode(KEYSTORE.MUTETEMPLATE).setValue(muteTemplate);
            muteNode.getNode(KEYSTORE.UNMUTETEMPLATE).setValue(unmuteTemplate);
            muteNode.getNode(KEYSTORE.CHATIGNORETEMPLATE).setValue(chatIgnoreTemplate);

            ConfigurationNode tabDecoNode = configRoot.getNode(KEYSTORE.TABDECO);
            tabDecoNode.getNode(KEYSTORE.ENABLETABDECO).setValue(enableTabDeco);
            tabDecoNode.getNode(KEYSTORE.HEADER).setValue(headerTemplateList);
            tabDecoNode.getNode(KEYSTORE.FOOTER).setValue(footerTemplateList);
            tabDecoNode.getNode(KEYSTORE.ENTRYTEMPLATE).setValue(entryTemplate);
            tabDecoNode.getNode(KEYSTORE.REFRESHRATE).setValue(refreshRate);

            plugin.getConfigManager().save(configRoot);
        } catch (IOException ex) {
            plugin.getLogger().error("An error occurred while saving this configuration: " + ex);
            plugin.getLogger().warn("Load default configurations.");
        }
    }

    /**
     * Reload configuration
     */
    public boolean reload() {
        Config config = new Config();
        if (config.isLoadCompleted()) {
            instance = config;
            return true;
        }
        else {
            return false;
        }
    }
}
