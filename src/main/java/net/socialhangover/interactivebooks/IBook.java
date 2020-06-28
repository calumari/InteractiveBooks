package net.socialhangover.interactivebooks;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import me.lucko.helper.text3.Text;
import me.minidigger.minimessage.bungee.MiniMessageParser;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode
public class IBook {

//    private static final String bookIdKey = "InteractiveBooks|Book-Id";

    @Getter
    private final String id;

    @Getter
    private final List<String> pages;

    @Getter
    private final List<String> commands;

    public IBook(String id, YamlConfiguration config) {
        this.id = id;
        this.pages = ImmutableList.copyOf(config.getStringList("pages"));

        Set<String> commands = new HashSet<>();
        if (config.getString("commands") != null) {
            commands.addAll(Arrays.asList(config.getString("commands").toLowerCase().split(" ")));
        } else {
            config.getStringList("commands").forEach(s -> commands.add(s.toLowerCase()));
        }
        this.commands = ImmutableList.copyOf(commands);
    }

    public void open(Player player) {
        player.openBook(getItem(player));
    }

    public ItemStack getItem() {
        return getItem(null);
    }

    public ItemStack getItem(@Nullable Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("test");
        meta.setAuthor("test");
        meta.setLore(Arrays.asList("", ""));
        List<BaseComponent[]> pages = new ArrayList<>();
        for (String page : this.pages) {
            pages.add(MiniMessageParser.parseFormat(Text.setPlaceholders(player, page)));
        }
        meta.spigot().setPages(pages);
        book.setItemMeta(meta);
//        book.setItemMeta(this.getBookMeta(player));
//        NBTItem nbti = new NBTItem(book);
//        nbti.setString(bookIdKey, this.getId());
//        return nbti.getItem();
        return book;
    }

    public boolean hasPermission(CommandSender sender) {
        return true;
    }
}
