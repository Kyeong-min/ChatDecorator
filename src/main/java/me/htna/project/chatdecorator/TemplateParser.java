package me.htna.project.chatdecorator;

import me.htna.project.chatdecorator.placeholderHandlers.IPlaceholderHandler;
import me.htna.project.chatdecorator.struct.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template parser
 */
public class TemplateParser {

    /**
     * Instance
     */
    private static TemplateParser instance;

    /**
     * Singleton instance
     * @return instance
     */
    public static TemplateParser getInstance() {
        if (instance == null)
            instance = new TemplateParser();

        return instance;
    }

    /**
     * Placeholder handler list
     */
    private List<IPlaceholderHandler> placeholders;

    /**
     * Compiled placeholder pattern
     */
    private final Pattern pattern;

    /**
     * CTOR
     */
    private TemplateParser() {
        pattern = Pattern.compile("%[^%]*%");
        placeholders = new ArrayList<>();
    }

    /**
     * Check placeholder is already added.
     *
     * @param placeholder placeholder handler
     * @return if true, placeholder already added.
     */
    private boolean isContainedPlaceholder(IPlaceholderHandler placeholder) {
        return placeholders.stream().anyMatch(x -> x.getPlaceholderHandlerName().equals(placeholder.getPlaceholderHandlerName()));
    }

    /**
     * Add placeholder handler
     *
     * @param placeholder placeholder handler
     * @throws Exception Same placeholder is already added.
     */
    public void addPlaceholder(IPlaceholderHandler placeholder) throws Exception {
        if (isContainedPlaceholder(placeholder))
            throw new Exception("Placeholder " + placeholder.getPlaceholderHandlerName() + " is already added");

        placeholders.add(placeholder);
    }

    /**
     * Replace placeholder
     *
     * @param placeholder Placeholder key
     * @param message {@link Message}
     * @return corresponding value
     */
    private String replace(String placeholder, Message message) {
        // ChatDecorator.getInstance().getLogger().debug("TemplateParser#replace, handler count: " + placeholders.size());
        return placeholders
                .stream()
                .filter(x -> {
                    // ChatDecorator.getInstance().getLogger().debug("TemplateParser#replace, Filtered placeholder handler: " + x.getPlaceholderHandlerName());
                    return x.IsTargetPlaceholder(placeholder);
                })
                .findFirst()
                .map(x -> {
                    // ChatDecorator.getInstance().getLogger().debug("TemplateParser#replace, Selected placeholder handler: " + x.getPlaceholderHandlerName());
                    return x.replace(placeholder, message);
                })
                .orElse(null);
    }

    /**
     * Parse template
     *
     * @param template template string
     * @param message {@link Message}
     * @return formatted string
     */
    public String parse(String template, Message message) {
        StringBuilder sb = new StringBuilder(template);
        List<Object> valueList = new ArrayList<>();
        Matcher matcher = pattern.matcher(template);
        while(matcher.find()) {
            String placeholder = matcher.group();

            int start = sb.indexOf(placeholder);
            int end = start + placeholder.length();
            sb.replace(start, end, "%s");

            String correspondingValue = replace(placeholder, message);
            if (correspondingValue == null) {
                // if corresponding value is empty
                Config config = Config.getInstance();
                String defaultPlaceholderCorrespondingValue = config.getDefaultPlaceholderCorrespondingValue();

                if (defaultPlaceholderCorrespondingValue.isEmpty()) {
                    correspondingValue = placeholder;
                } else {
                    correspondingValue = defaultPlaceholderCorrespondingValue;
                }
            }

            valueList.add(correspondingValue);
        }

        return String.format(sb.toString(), valueList.toArray());
    }
}
