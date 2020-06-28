package net.socialhangover.interactivebooks;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.socialhangover.interactivebooks.handler.CommandHandler;
import net.socialhangover.interactivebooks.handler.PlayerListener;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class InteractiveBooksPlugin extends ExtendedJavaPlugin {

    private final Map<String, IBook> books = new HashMap<>();

    @Getter
    private YamlConfiguration config;

    @Override
    protected void enable() {
        reload();
        bindModule(new CommandHandler(this));
        bindModule(new PlayerListener(this));
    }

    public void reload() {
        books.clear();
        loadAll();
    }

    public List<IBook> getBooks() {
        return ImmutableList.copyOf(books.values());
    }

    @Nullable
    public IBook getBook(String id) {
        return books.get(id);
    }

    @Nullable
    public IBook getByCommand(String command) {
        for (IBook book : books.values()) {
            if (book.getCommands().contains(command)) {
                return book;
            }
        }
        return null;
    }

    public void registerBook(IBook book) {
        books.put(book.getId(), book);
    }

    public void unregisterBook(String id) {
        books.remove(id);
    }

    private void loadAll() {
        config = loadConfig("config.yml");
//        File folder = getRelativeFile("books");
        File example = new File(getDataFolder(), "examplebook.yml");
        if (!example.exists() && !config.getBoolean("save-example", false)) {
            saveResource("examplebook.yml", false);
        }
        registerBook(new IBook("example", YamlConfiguration.loadConfiguration(example)));
//        loadBookConfigs(folder);
    }

    private void loadBookConfigs(File folder) {
        folder.mkdirs();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().endsWith(".yml")) {
                registerBook(new IBook(file.getName().substring(0, file.getName().length() - 4), YamlConfiguration.loadConfiguration(file)));
            }
        }
    }
}
