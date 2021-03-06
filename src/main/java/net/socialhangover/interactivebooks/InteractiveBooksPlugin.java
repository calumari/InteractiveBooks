package net.socialhangover.interactivebooks;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.SneakyThrows;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.socialhangover.interactivebooks.handler.CommandHandler;
import net.socialhangover.interactivebooks.handler.PlayerListener;
import net.socialhangover.interactivebooks.locale.LocaleManager;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class InteractiveBooksPlugin extends ExtendedJavaPlugin {

    private final Map<String, IBook> books = new HashMap<>();

    @Getter
    private YamlConfiguration config;

    @Getter
    private final LocaleManager localeManager = new LocaleManager();

    @Override
    protected void enable() {
        reload();
        bindModule(new CommandHandler(this));
        bindModule(new PlayerListener(this));
    }

    public void reload() {
        this.localeManager.tryLoad(getDataDirectory().resolve("locale.yml"));
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
        books.clear();
        config = loadConfig("config.yml");

        File folder = getRelativeFile("books");
        folder.mkdirs();

        if (config.getBoolean("load-example", true)) {
            File example = new File(folder, "examplebook.yml");
            if (!example.exists()) {
                registerBook(new IBook("examplebook", YamlConfiguration.loadConfiguration(createTemplate("examplebook"))));
            }
        }
        loadBookConfigs(folder);
    }

    private void loadBookConfigs(File folder) {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().endsWith(".yml")) {
                registerBook(new IBook(file.getName().substring(0, file.getName().length() - 4), YamlConfiguration.loadConfiguration(file)));
            }
        }
    }

    @SneakyThrows
    public File createTemplate(String name) {
        File folder = new File(getRelativeFile("books"), name + ".yml");
        if (folder.exists()) {
            return null;
        }
        Files.copy(getResource("examplebook.yml"), folder.toPath());
        return folder;
    }
}
