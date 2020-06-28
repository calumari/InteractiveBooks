package net.socialhangover.interactivebooks.handler;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Events;
import me.lucko.helper.reflect.MinecraftVersion;
import me.lucko.helper.reflect.MinecraftVersions;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import net.socialhangover.interactivebooks.IBook;
import net.socialhangover.interactivebooks.InteractiveBooksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nonnull;
import java.util.List;

@RequiredArgsConstructor
public class PlayerListener implements TerminableModule {

    private static final boolean MC_AFTER_1_14 = MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersions.v1_14);

    private final InteractiveBooksPlugin plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    String openBookId;
                    List<String> booksToGiveIds;
                    if (e.getPlayer().hasPlayedBefore()) {
                        openBookId = plugin.getConfig().getString("open_book_on_join");
                        booksToGiveIds = plugin.getConfig().getStringList("books_on_join");
                    } else {
                        openBookId = plugin.getConfig().getString("open_book_on_first_join");
                        booksToGiveIds = plugin.getConfig().getStringList("books_on_first_join");
                    }
                    if (openBookId != null && plugin.getBook(openBookId) != null && e.getPlayer().hasPermission("interactivebooks.open." + openBookId)) {
                        IBook book = plugin.getBook(openBookId);
                        if (book != null) {
                            if (MC_AFTER_1_14)
                                book.open(e.getPlayer());
                            else
                                Bukkit.getScheduler().runTask(plugin, () -> book.open(e.getPlayer()));
                        }
                    }

                    booksToGiveIds.forEach(id -> {
                        IBook book = plugin.getBook(id);
                        if (book != null)
                            e.getPlayer().getInventory().addItem(book.getItem(e.getPlayer()));
                    });
                })
                .bindWith(consumer);

//        Events.subscribe(PlayerInteractEvent.class)
//                .handler(e -> {
//                    if (e.useItemInHand().equals(Event.Result.DENY))
//                        return;
//                    if (!e.getAction().equals(Action.RIGHT_CLICK_AIR) && !e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
//                        return;
//                    if (!plugin.getConfig().getBoolean("update_books_on_use"))
//                        return;
//                    if (!BooksUtils.getItemInMainHand(e.getPlayer()).getType().equals(Material.WRITTEN_BOOK))
//                        return;
//                    NBTItem nbti = new NBTItem(BooksUtils.getItemInMainHand(e.getPlayer()));
//                    if (!nbti.hasKey("InteractiveBooks|Book-Id"))
//                        return;
//                    IBook book = plugin.getBook(nbti.getString("InteractiveBooks|Book-Id"));
//                    if (book == null)
//                        return;
//                    ItemStack bookItem = book.getItem(e.getPlayer());
//                    bookItem.setAmount(BooksUtils.getItemInMainHand(e.getPlayer()).getAmount());
//                    BooksUtils.setItemInMainHand(e.getPlayer(), bookItem);
//                })
//                .bindWith(consumer);

        Events.subscribe(PlayerCommandPreprocessEvent.class)
                .handler(e -> {
                    IBook book = plugin.getByCommand(e.getMessage().split(" ", 2)[0].replaceFirst("/", "").toLowerCase());
                    if (book == null) {
                        return;
                    }

                    Player player = e.getPlayer();
                    if (!book.hasPermission(player)) {
                        player.sendMessage("you don't have permission"); // todo locale
                        return;
                    }

                    book.open(player);
                    e.setCancelled(true);
                })
                .bindWith(consumer);
    }
}
