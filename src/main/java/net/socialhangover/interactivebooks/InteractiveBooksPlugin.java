package net.socialhangover.interactivebooks;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;

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
        registerCommand();
        bindModule(new PlayerListener(this));
    }

    public void reload() {
        books.keySet().forEach(this::unregisterBook);
        loadAll();
    }

    public List<IBook> getBooks() {
        return ImmutableList.copyOf(books.values());
    }

    public IBook getBook(String id) {
        return books.get(id);
    }

    public void registerBook(IBook book) {
        books.put(book.getId(), book);
    }

    public void unregisterBook(String id) {
        books.remove(id);
    }

    private void registerCommand() {
        PluginCommand commandIBooks = getCommand("ibooks");
        Objects.requireNonNull(commandIBooks).setExecutor(new CommandIBooks(this));
        commandIBooks.setTabCompleter(new TabCompleterIBooks(this));
    }

    private void loadAll() {
        config = loadConfig("config.yml");
        File folder = getRelativeFile("books");
        File example = new File(folder, "examplebook.yml");
        if (!example.exists() && !config.getBoolean("save-example", false)) {
            saveResource("examplebook.yml", false);
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
}
