package net.socialhangover.interactivebooks.locale;

import lombok.Getter;
import me.minidigger.minimessage.bungee.MiniMessageParser;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

public enum Message {
    RELOAD("<green>Locale and config reloaded!"),
    NO_CONSOLE("<red>Only players are able to use this command."),
    NO_PERMISSION("<red>You are not able to use this command."),
    INVALID_USAGE("<red>&cInvalid usage. Try: {}."),
    MISSING_BOOK("<red>That book does not exist."),
    FILE_CREATE("<green>Book created! Use <click:suggest_command:/ibooks reload>/ibooks reload</click> to load it."),
    FILE_EXISTS("<red>A file already exists with the name '{}'.");

    @Getter
    private final String message;

    Message(String message) {
        this.message = rewritePlaceholders(message);
    }

    public void send(CommandSender sender, @Nullable LocaleManager localeManager, Object... objects) {
        sender.spigot().sendMessage(MiniMessageParser.parseFormat(format(localeManager, objects)));
    }

    private String format(LocaleManager localeManager, Object... objects) {
        return replacePlaceholders(this.getTranslatedMessage(localeManager).replace("\\n", "\n"), objects);
    }

    private String getTranslatedMessage(@Nullable LocaleManager localeManager) {
        String prefix = null;
        if (localeManager != null) {
            prefix = localeManager.getTranslation(this);
        }
        if (prefix == null) {
            prefix = this.getMessage();
        }
        return prefix;
    }

    public static String rewritePlaceholders(String input) {
        int i = 0;
        while (input.contains("{}")) {
            input = input.replaceFirst("\\{}", "{" + i++ + "}");
        }
        return input;
    }

    private static String replacePlaceholders(String s, Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            Object o = objects[i];
            s = s.replace("{" + i + "}", String.valueOf(o));
        }
        return s;
    }
}
