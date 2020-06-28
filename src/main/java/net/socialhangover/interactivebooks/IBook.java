package net.socialhangover.interactivebooks;

import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;
import de.tr7zw.changeme.nbtapi.NBTItem;
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

    private static final String BOOK_ID_KEY = "InteractiveBooks|Book-Id";

    @Getter
    private final String id;

    private final String name;
    private final String title;
    private final String author;
    private final List<String> lore;
    private final BookMeta.Generation generation;
    private final List<String> chapters;

    @Getter
    private final List<String> commands;

    public IBook(String id, YamlConfiguration config) {
        this.id = id;
        this.name = config.getString("name", "");
        this.title = config.getString("title", "");
        this.author = config.getString("author", "");
        this.lore = ImmutableList.copyOf(config.getStringList("lore"));
        this.chapters = ImmutableList.copyOf(config.getStringList("chapters"));
        this.generation = Enums.getIfPresent(BookMeta.Generation.class, config.getString("generation", "")).or(BookMeta.Generation.ORIGINAL);

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

    public ItemStack getItem(@Nullable Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(Text.setPlaceholders(player, name));
        meta.setAuthor(Text.setPlaceholders(player, author));
        if (!lore.isEmpty()) {
            meta.setLore(Text.setPlaceholders(player, lore));
        }
        meta.setGeneration(generation);

        List<BaseComponent[]> pages = new ArrayList<>();
        for (String chapter : chapters) {
            pages.add(MiniMessageParser.parseFormat(Text.setPlaceholders(player, chapter)));
        }
        meta.spigot().setPages(pages);
        book.setItemMeta(meta);
        return book;
    }

    public ItemStack getTaggedItem(@Nullable Player player) {
        ItemStack book = getItem(player);
        NBTItem nbti = new NBTItem(book);
        nbti.setString(BOOK_ID_KEY, this.getId());
        return nbti.getItem();
    }

    public boolean hasPermission(CommandSender sender) {
        return true;
    }
}
