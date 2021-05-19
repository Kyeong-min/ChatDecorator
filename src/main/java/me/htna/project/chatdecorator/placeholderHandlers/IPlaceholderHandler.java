package me.htna.project.chatdecorator.placeholderHandlers;

import me.htna.project.chatdecorator.struct.Message;

/**
 * Placeholder handler interface
 */
public interface IPlaceholderHandler {

    /**
     * Return placeholder handler name
     *
     * The returned placeholder handler name must be unique.
     *
     * @return Placeholder name
     */
    String getPlaceholderHandlerName();

    /**
     * Replace the placeholder with the corresponding value.
     *
     * @param placeholder Placeholder key
     * @param message {@link Message}
     * @return corresponding value
     */
    String replace(String placeholder, Message message);

    /**
     * Make sure the placeholder is the target of this handler.
     *
     * @param placeholder Placeholder key
     * @return if true, placeholder is the target of this handler.
     */
    boolean IsTargetPlaceholder(String placeholder);
}
