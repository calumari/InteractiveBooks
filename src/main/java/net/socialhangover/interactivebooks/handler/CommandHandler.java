package net.socialhangover.interactivebooks.handler;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import net.socialhangover.interactivebooks.InteractiveBooksPlugin;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class CommandHandler implements TerminableModule {

    private final InteractiveBooksPlugin plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Commands.create()
                .handler(e -> {

                })
                .registerAndBind(consumer, "ibooks", "interactivebooks", "ib");
    }
}
