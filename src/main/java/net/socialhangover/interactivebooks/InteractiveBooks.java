package net.socialhangover.interactivebooks;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class InteractiveBooks extends ExtendedJavaPlugin {

    private static InteractiveBooks instance;
    private static final Map<String, IBook> books = new HashMap<>();

    @Override
    protected void enable() {
        instance = this;
        loadAll();
        registerCommand();
        bindModule(new PlayerListener());
    }

    @Override
    protected void disable() {
        instance = null;
    }

    /**
     * Gets the instance of this plugin.
     *
     * @return an instance of the plugin
     */
    @Deprecated
    public static InteractiveBooks getInstance() {
        return instance;
    }

    /**
     * Gets the registered books.
     *
     * @return a {@link Map} with book ids as keys and the registered books ({@link IBook}) as values
     */
    public static Map<String, IBook> getBooks() {
        return new HashMap<>(books);
    }

    /**
     * Gets an {@link IBook} by its id.
     *
     * @param id the id of the book to get
     * @return the book with the specified id if it's registered, or null if not found
     * @see #registerBook(IBook)
     */
    public static IBook getBook(String id) {
        return books.get(id);
    }

    /**
     * Registers a book.
     *
     * @param book the book id to register
     */
    public static void registerBook(IBook book) {
        books.put(book.getId(), book);
    }

    /**
     * Unegisters a book by its id.
     *
     * @param id the book id to unregister
     */
    public static void unregisterBook(String id) {
        books.remove(id);
    }

    private void registerCommand() {
        PluginCommand commandIBooks = getCommand("ibooks");
        Objects.requireNonNull(commandIBooks).setExecutor(new CommandIBooks(this));
        commandIBooks.setTabCompleter(new TabCompleterIBooks());
    }

    static void loadAll() {
        InteractiveBooks.getInstance().saveDefaultConfig();
        InteractiveBooks.getInstance().reloadConfig();
        File f = new File(InteractiveBooks.getInstance().getDataFolder(), "books");
        if (!f.exists()) {
            try {
                if (!f.mkdirs())
                    throw new IOException();
                Files.copy(Objects.requireNonNull(InteractiveBooks.getInstance().getResource("examplebook.yml")), new File(f, "examplebook.yml").toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadBookConfigs();
    }

    private static void loadBookConfigs() {
        InteractiveBooks.getBooks().keySet().forEach(InteractiveBooks::unregisterBook);
        File booksFolder = new File(InteractiveBooks.getInstance().getDataFolder(), "books");
        for (File f : Objects.requireNonNull(booksFolder.listFiles()))
            if (f.getName().endsWith(".yml"))
                InteractiveBooks.registerBook(new IBook(f.getName().substring(0, f.getName().length() - 4), YamlConfiguration.loadConfiguration(f)));
    }

}
