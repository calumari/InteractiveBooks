package net.leonardo_dgs.interactivebooks;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class InteractiveBooks extends JavaPlugin {

    private static InteractiveBooks instance;
    private static final Map<String, IBook> books = new HashMap<>();

    @Override
    public void onEnable()
    {
        instance = this;
        Config.loadAll();
        registerCommand();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable()
    {
        instance = null;
    }

    /**
     * Gets the instance of this plugin.
     *
     * @return an instance of the plugin
     */
    public static InteractiveBooks getInstance()
    {
        return instance;
    }

    /**
     * Gets the registered books.
     *
     * @return a {@link Map} with book ids as keys and the registered books ({@link IBook}) as values
     */
    public static Map<String, IBook> getBooks()
    {
        return new HashMap<>(books);
    }

    /**
     * Gets an {@link IBook} by its id.
     *
     * @param id the id of the book to get
     * @return the book with the specified id if it's registered, or null if not found
     * @see #registerBook(IBook)
     */
    public static IBook getBook(String id)
    {
        return books.get(id);
    }

    /**
     * Registers a book.
     *
     * @param book the book id to register
     */
    public static void registerBook(IBook book)
    {
        books.put(book.getId(), book);
    }

    /**
     * Unegisters a book by its id.
     *
     * @param id the book id to unregister
     */
    public static void unregisterBook(String id)
    {
        books.remove(id);
    }

    private void registerCommand()
    {
        PluginCommand commandIBooks = getCommand("ibooks");
        Objects.requireNonNull(commandIBooks).setExecutor(new CommandIBooks());
        commandIBooks.setTabCompleter(new TabCompleterIBooks());
    }

}
