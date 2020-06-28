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

                    if (root.equalsIgnoreCase("open")) {
                        if (!(c.sender() instanceof Player)) {
                            c.reply("you must be a player to run this command"); // todo locale
                            return;
                        }

                        String name = c.arg(1).parse(String.class).orElse(null);
                        if (name == null) {
                            c.reply("missing book name"); // todo locale
                            return;
                        }
                        IBook book = plugin.getBook(name);
                        if (book == null) {
                            c.reply("missing book"); // todo locale
                            return;
                        }
                        book.open((Player) c.sender());
                    }

                    if (root.equalsIgnoreCase("create")) {
                        String name = c.arg(1).parse(String.class).orElse(null);
                        if (name == null) {
                            c.reply("missing filename"); // todo locale
                            return;
                        }
                        if (plugin.createTemplate(name) != null) {
                            c.reply("file created. use /ibooks reload to load"); // todo locale
                        } else {
                            c.reply("file name exists?"); // todo locale
                        }
                        return;
                    }

                    if (root.equalsIgnoreCase("give")) {
                        Player player = c.arg(1).parseOrFail(Player.class);
                        String name = c.arg(2).parse(String.class).orElse(null);
                        if (name == null) {
                            c.reply("missing book name"); // todo locale
                            return;
                        }
                        IBook book = plugin.getBook(name);
                        if (book == null) {
                            c.reply("missing book"); // todo locale
                            return;
                        }
                        player.getInventory().addItem(book.getTaggedItem(player));
                        return;
                    }

                    if (root.equalsIgnoreCase("reload")) {
                        plugin.reload();
                        c.reply("plugin reloaded :)");
                    }
                })
                .registerAndBind(consumer, "ibooks", "interactivebooks", "ib");
    }

    private List<String> getFilteredTabComplete(CommandSender sender) {
        List<String> values = new ArrayList<>();
        for (String s : completions) {
            if (sender.hasPermission("interactivebooks.command" + s)) {
                values.add(s);
            }
        }
        return values;
    }

    private List<String> getBookIds(CommandSender sender) {
        List<String> values = new ArrayList<>();
        for (IBook book : plugin.getBooks()) {
            if (sender.hasPermission("interactivebooks.open" + book.getId())) {
                values.add(book.getId());
            }
        }
        return values;
    }
}
