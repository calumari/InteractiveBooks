package net.leonardo_dgs.interactivebooks;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.lucko.helper.reflect.MinecraftVersion;
import net.leonardo_dgs.interactivebooks.util.BooksUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class PlayerListener implements Listener {

    private static final boolean MC_AFTER_1_14;

    static
    {
        MinecraftVersion runningVersion = MinecraftVersion.getRuntimeVersion();
        MinecraftVersion v1_14 = MinecraftVersion.parse("1.14");
        MC_AFTER_1_14 = runningVersion.equals(v1_14) || runningVersion.isAfter(v1_14);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        String openBookId;
        List<String> booksToGiveIds;
        if (event.getPlayer().hasPlayedBefore())
        {
            openBookId = InteractiveBooks.getInstance().getConfig().getString("open_book_on_join");
            booksToGiveIds = InteractiveBooks.getInstance().getConfig().getStringList("books_on_join");
        }
        else
        {
            openBookId = InteractiveBooks.getInstance().getConfig().getString("open_book_on_first_join");
            booksToGiveIds = InteractiveBooks.getInstance().getConfig().getStringList("books_on_first_join");
        }
        if (openBookId != null && InteractiveBooks.getBook(openBookId) != null && event.getPlayer().hasPermission("interactivebooks.open." + openBookId))
        {
            IBook book = InteractiveBooks.getBook(openBookId);
            if (book != null)
            {
                if(MC_AFTER_1_14)
                    book.open(event.getPlayer());
                else
                    Bukkit.getScheduler().runTask(InteractiveBooks.getInstance(), () -> book.open(event.getPlayer()));
            }
        }

        booksToGiveIds.forEach(id ->
        {
            IBook book = InteractiveBooks.getBook(id);
            if (book != null)
                event.getPlayer().getInventory().addItem(book.getItem(event.getPlayer()));
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.useItemInHand().equals(Event.Result.DENY))
            return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;
        if (!InteractiveBooks.getInstance().getConfig().getBoolean("update_books_on_use"))
            return;
        if (!BooksUtils.getItemInMainHand(event.getPlayer()).getType().equals(Material.WRITTEN_BOOK))
            return;
        NBTItem nbti = new NBTItem(BooksUtils.getItemInMainHand(event.getPlayer()));
        if (!nbti.hasKey("InteractiveBooks|Book-Id"))
            return;
        IBook book = InteractiveBooks.getBook(nbti.getString("InteractiveBooks|Book-Id"));
        if (book == null)
            return;
        ItemStack bookItem = book.getItem(event.getPlayer());
        bookItem.setAmount(BooksUtils.getItemInMainHand(event.getPlayer()).getAmount());
        BooksUtils.setItemInMainHand(event.getPlayer(), bookItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String command = event.getMessage().split(" ", 2)[0].replaceFirst("/", "").toLowerCase();
        IBook iBook = null;
        for (IBook book : InteractiveBooks.getBooks().values())
            if (book.getOpenCommands().contains(command))
            {
                iBook = book;
                break;
            }
        if (iBook == null)
            return;
        if (event.getPlayer().hasPermission("interactivebooks.open." + iBook.getId()))
            iBook.open(event.getPlayer());
        else
            event.getPlayer().sendMessage("§cYou don't have permission to open this book.");

        event.setCancelled(true);
    }

}
