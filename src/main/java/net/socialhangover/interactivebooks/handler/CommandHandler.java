package net.socialhangover.interactivebooks.handler;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import me.lucko.helper.Commands;
import me.lucko.helper.command.tabcomplete.CompletionSupplier;
import me.lucko.helper.command.tabcomplete.TabCompleter;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import net.socialhangover.interactivebooks.InteractiveBooksPlugin;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CommandHandler implements TerminableModule {

    private static final List<String> completions = ImmutableList.of("open", "give", "reload");

    private final InteractiveBooksPlugin plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Commands.create()
                .tabHandler(c -> TabCompleter.create()
                        .at(0, CompletionSupplier.startsWith(getFilteredTabComplete(c.sender())))
                        .complete(c.args()))
                .handler(c -> {
                    String root = c.arg(0).parse(String.class).orElse(null);
                    if (root == null) {
                        return;
                    }
                    switch (root.toLowerCase()) {
                        case "reload":
                            plugin.reload();
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
}
