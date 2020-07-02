package net.socialhangover.interactivebooks.handler;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import me.lucko.helper.Commands;
import me.lucko.helper.command.tabcomplete.CompletionSupplier;
import me.lucko.helper.command.tabcomplete.TabCompleter;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.Players;
import net.socialhangover.interactivebooks.IBook;
import net.socialhangover.interactivebooks.InteractiveBooksPlugin;
import net.socialhangover.interactivebooks.locale.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CommandHandler implements TerminableModule {

    private static final List<String> completions = ImmutableList.of("open", "create", "give", "reload");

    private final InteractiveBooksPlugin plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Commands.create()
                .tabHandler(c -> {
                    TabCompleter completer = TabCompleter.create()
                            .at(0, CompletionSupplier.startsWith(getFilteredTabComplete(c.sender())));

                    String root = c.arg(0).parse(String.class).orElse(null);
                    if (root == null) {
                        return completer.complete(c.args());
                    }

                    if (root.equalsIgnoreCase("open")) {
                        return completer.at(1, CompletionSupplier.startsWith(getBookIds(c.sender()))).complete(c.args());
                    }

                    if (root.equalsIgnoreCase("give")) {
                        return completer.at(1, CompletionSupplier.startsWith(Players::names))
                                .at(2, CompletionSupplier.startsWith(getBookIds(c.sender()))).complete(c.args());
                    }

                    if (root.equalsIgnoreCase("create")) {
                        return completer.at(1, CompletionSupplier.startsWith("areallycoolbook", "ataleofadeveloper"))
                                .complete(c.args());
                    }
                    return completer.complete(c.args());
                })
                .handler(c -> {
                    String root = c.arg(0).parse(String.class).orElse(null);
                    if (root == null) {
                        return;
                    }

                    CommandSender sender = c.sender();

                    if (root.equalsIgnoreCase("open")) {
                        if (!(sender instanceof Player)) {
                            Message.NO_CONSOLE.send(sender, plugin.getLocaleManager());
                            return;
                        }

                        if (!sender.hasPermission("interactivebooks.command.open")) {
                            Message.NO_PERMISSION.send(sender, plugin.getLocaleManager());
                            return;
                        }

                        String name = c.arg(1).parse(String.class).orElse(null);
                        if (name == null) {
                            Message.INVALID_USAGE.send(sender, plugin.getLocaleManager(), "/open <name>");
                            return;
                        }
                        IBook book = plugin.getBook(name);
                        if (book == null) {
                            Message.MISSING_BOOK.send(sender, plugin.getLocaleManager());
                            return;
                        }
                        book.open((Player) sender);
                    }

                    if (root.equalsIgnoreCase("create")) {
                        if (!sender.hasPermission("interactivebooks.command.create")) {
                            Message.NO_PERMISSION.send(sender, plugin.getLocaleManager());
                            return;
                        }

                        String name = c.arg(1).parse(String.class).orElse(null);
                        if (name == null) {
                            Message.INVALID_USAGE.send(sender, plugin.getLocaleManager(), "/create <name>");
                            return;
                        }
                        (plugin.createTemplate(name) != null ? Message.FILE_CREATE : Message.FILE_EXISTS).send(sender, plugin.getLocaleManager(), name);
                        return;
                    }

                    if (root.equalsIgnoreCase("give")) {
                        if (!sender.hasPermission("interactivebooks.command.give")) {
                            Message.NO_PERMISSION.send(sender, plugin.getLocaleManager());
                            return;
                        }

                        Player player = c.arg(1).parse(Player.class).orElse(null);
                        if (player == null) {
                            Message.INVALID_USAGE.send(sender, plugin.getLocaleManager(), "/give <player> <name>");
                            return;
                        }

                        String name = c.arg(2).parse(String.class).orElse(null);
                        if (name == null) {
                            Message.INVALID_USAGE.send(sender, plugin.getLocaleManager(), "/give <player> <name>");
                            return;
                        }

                        IBook book = plugin.getBook(name);
                        if (book == null) {
                            Message.MISSING_BOOK.send(player, plugin.getLocaleManager());
                            return;
                        }
                        player.getInventory().addItem(book.getTaggedItem(player));
                        return;
                    }

                    if (root.equalsIgnoreCase("reload")) {
                        if (!sender.hasPermission("interactivebooks.command.reload")) {
                            Message.NO_PERMISSION.send(sender, plugin.getLocaleManager());
                            return;
                        }

                        plugin.reload();
                        Message.RELOAD.send(sender, plugin.getLocaleManager());
                    }
                })
                .registerAndBind(consumer, "ibooks", "interactivebooks", "ib");
    }

    private List<String> getFilteredTabComplete(CommandSender sender) {
        List<String> values = new ArrayList<>();
        for (String s : completions) {
            if (sender.hasPermission("interactivebooks.command." + s)) {
                values.add(s);
            }
        }
        return values;
    }

    private List<String> getBookIds(CommandSender sender) {
        List<String> values = new ArrayList<>();
        for (IBook book : plugin.getBooks()) {
            if (book.hasPermission(sender)) {
                values.add(book.getId());
            }
        }
        return values;
    }
}
