package net.socialhangover.interactivebooks.handler;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import me.lucko.helper.Events;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import net.socialhangover.interactivebooks.IBook;
import net.socialhangover.interactivebooks.InteractiveBooksPlugin;
import net.socialhangover.interactivebooks.locale.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

@RequiredArgsConstructor
public class PlayerListener implements TerminableModule {

    private final InteractiveBooksPlugin plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    Player player = e.getPlayer();

                    String openBookId;
                    List<String> booksToGiveIds;
                    if (player.hasPlayedBefore()) {
                        openBookId = plugin.getConfig().getString("open-book-on-join");
                        booksToGiveIds = plugin.getConfig().getStringList("books-on-join");
                    } else {
                        openBookId = plugin.getConfig().getString("open-book-on-first-join");
                        booksToGiveIds = plugin.getConfig().getStringList("books-on-first-join");
                    }

                    if (openBookId != null && plugin.getBook(openBookId) != null) {
                        IBook book = plugin.getBook(openBookId);
                        if (book != null && book.hasPermission(player)) {
                            book.open(player);
                        }
                    }

                    booksToGiveIds.forEach(id -> {
                        IBook book = plugin.getBook(id);
                        if (book != null) {
                            player.getInventory().addItem(book.getTaggedItem(player));
                        }
                    });
                })
                .bindWith(consumer);

        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.useItemInHand() != Event.Result.DENY && e.getHand() != null)
                .filter(e -> e.hasItem() && e.getItem().getType() == Material.WRITTEN_BOOK)
                .filter(e -> e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                .filter(e -> plugin.getConfig().getBoolean("update-books-on-use"))
                .handler(e -> {
                    ItemStack item = e.getItem();
                    NBTItem nbti = new NBTItem(item);
                    if (!nbti.hasKey(IBook.BOOK_ID_KEY)) {
                        return;
                    }
                    IBook book = plugin.getBook(nbti.getString(IBook.BOOK_ID_KEY));
                    if (book == null) {
                        return;
                    }
                    Player player = e.getPlayer();
                    ItemStack bookItem = book.getTaggedItem(player);
                    bookItem.setAmount(item.getAmount());
                    player.getInventory().setItem(e.getHand(), bookItem);
                })
                .bindWith(consumer);

        Events.subscribe(PlayerCommandPreprocessEvent.class)
                .handler(e -> {
                    IBook book = plugin.getByCommand(e.getMessage().split(" ", 2)[0].replaceFirst("/", "").toLowerCase());
                    if (book == null) {
                        return;
                    }

                    Player player = e.getPlayer();
                    if (!book.hasPermission(player)) {
                        Message.NO_PERMISSION.send(player, plugin.getLocaleManager());
                        return;
                    }

                    book.open(player);
                    e.setCancelled(true);
                })
                .bindWith(consumer);
    }
}
