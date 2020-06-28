package net.socialhangover.interactivebooks;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.lucko.helper.Events;
import me.lucko.helper.reflect.MinecraftVersion;
import me.lucko.helper.reflect.MinecraftVersions;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import net.socialhangover.interactivebooks.util.BooksUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerListener implements TerminableModule {

    private static final boolean MC_AFTER_1_14 = MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersions.v1_14);

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    String openBookId;
                    List<String> booksToGiveIds;
                    if (e.getPlayer().hasPlayedBefore()) {
                        openBookId = InteractiveBooksPlugin.getInstance().getConfig().getString("open_book_on_join");
                        booksToGiveIds = InteractiveBooksPlugin.getInstance().getConfig().getStringList("books_on_join");
                    } else {
                        openBookId = InteractiveBooksPlugin.getInstance().getConfig().getString("open_book_on_first_join");
                        booksToGiveIds = InteractiveBooksPlugin.getInstance().getConfig().getStringList("books_on_first_join");
                    }
                    if (openBookId != null && InteractiveBooksPlugin.getBook(openBookId) != null && e.getPlayer().hasPermission("interactivebooks.open." + openBookId)) {
                        IBook book = InteractiveBooksPlugin.getBook(openBookId);
                        if (book != null) {
                            if (MC_AFTER_1_14)
                                book.open(e.getPlayer());
                            else
                                Bukkit.getScheduler().runTask(InteractiveBooksPlugin.getInstance(), () -> book.open(e.getPlayer()));
                        }
                    }

                    booksToGiveIds.forEach(id -> {
                        IBook book = InteractiveBooksPlugin.getBook(id);
                        if (book != null)
                            e.getPlayer().getInventory().addItem(book.getItem(e.getPlayer()));
                    });
                })
                .bindWith(consumer);

        Events.subscribe(PlayerInteractEvent.class)
                .handler(e -> {
                    if (e.useItemInHand().equals(Event.Result.DENY))
                        return;
                    if (!e.getAction().equals(Action.RIGHT_CLICK_AIR) && !e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                        return;
                    if (!InteractiveBooksPlugin.getInstance().getConfig().getBoolean("update_books_on_use"))
                        return;
                    if (!BooksUtils.getItemInMainHand(e.getPlayer()).getType().equals(Material.WRITTEN_BOOK))
                        return;
                    NBTItem nbti = new NBTItem(BooksUtils.getItemInMainHand(e.getPlayer()));
                    if (!nbti.hasKey("InteractiveBooks|Book-Id"))
                        return;
                    IBook book = InteractiveBooksPlugin.getBook(nbti.getString("InteractiveBooks|Book-Id"));
                    if (book == null)
                        return;
                    ItemStack bookItem = book.getItem(e.getPlayer());
                    bookItem.setAmount(BooksUtils.getItemInMainHand(e.getPlayer()).getAmount());
                    BooksUtils.setItemInMainHand(e.getPlayer(), bookItem);
                })
                .bindWith(consumer);

        Events.subscribe(PlayerCommandPreprocessEvent.class)
                .handler(e -> {
                    String command = e.getMessage().split(" ", 2)[0].replaceFirst("/", "").toLowerCase();
                    IBook iBook = null;
                    for (IBook book : InteractiveBooksPlugin.getBooks().values())
                        if (book.getOpenCommands().contains(command)) {
                            iBook = book;
                            break;
                        }
                    if (iBook == null)
                        return;
                    if (e.getPlayer().hasPermission("interactivebooks.open." + iBook.getId()))
                        iBook.open(e.getPlayer());
                    else
                        e.getPlayer().sendMessage("§cYou don't have permission to open this book.");

                    e.setCancelled(true);
                })
                .bindWith(consumer);
    }
}
