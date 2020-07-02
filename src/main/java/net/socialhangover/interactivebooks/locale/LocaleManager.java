package net.socialhangover.interactivebooks.locale;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor
public class LocaleManager {

    private Map<Message, String> messages = ImmutableMap.of();

    public void tryLoad(Path file) {
        if (Files.exists(file)) {
            try {
                loadFromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            EnumMap<Message, String> messages = new EnumMap<>(Message.class);

            Map<String, Object> data = new Yaml().load(reader);
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (entry.getKey() == null || entry.getKey().isEmpty() || entry.getValue() == null) {
                    continue;
                }

                if (entry.getValue() instanceof String) {
                    String key = entry.getKey().toUpperCase().replace('-', '_');
                    String value = (String) entry.getValue();

                    try {
                        messages.put(Message.valueOf(key), value);
                    } catch (IllegalArgumentException e) {
                        // ignore
                    }
                }
            }

            this.messages = ImmutableMap.copyOf(messages);
        }
    }

    public String getTranslation(Message key) {
        return this.messages.get(key);
    }

}
