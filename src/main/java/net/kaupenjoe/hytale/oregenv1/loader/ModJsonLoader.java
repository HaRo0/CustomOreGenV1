package net.kaupenjoe.hytale.oregenv1.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.procedurallib.json.Loader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.AssetModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class ModJsonLoader<K extends SeedResource, T> extends Loader<K, T> {

    @Nullable
    protected final JsonElement json;

    public ModJsonLoader(SeedString<K> seed, Path dataFolder, @Nullable JsonElement json) {

        super(seed, dataFolder);
        if (json != null && json.isJsonObject() && json.getAsJsonObject().has("File")) {
            this.json = this.loadFileConstructor(json.getAsJsonObject().get("File").getAsString());
        } else {
            this.json = json;
        }
    }

    protected static <V> V mustGet(
        @Nonnull String key,
        @Nullable JsonElement element,
        @Nullable V defaultValue,
        @Nonnull Class<V> type,
        @Nonnull Predicate<JsonElement> predicate,
        @Nonnull Function<JsonElement, V> mapper
    ) {

        if (element == null) {
            if (defaultValue != null) {
                return defaultValue;
            } else {
                throw error("Missing property '%s'", key);
            }
        } else if (!predicate.test(element)) {
            throw error("Property '%s' must be of type '%s'", key, type.getSimpleName());
        } else {
            return mapper.apply(element);
        }
    }

    protected static Error error(String format, Object... args) {

        return new Error(String.format(format, args));
    }

    protected static Error error(Throwable parent, String format, Object... args) {

        return new Error(String.format(format, args), parent);
    }

    private static boolean isString(JsonElement element) {

        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }

    protected static boolean isNumber(JsonElement element) {

        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }

    protected static boolean isBoolean(JsonElement element) {

        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
    }

    public boolean has(String name) {

        return this.json != null && this.json.isJsonObject() && this.json.getAsJsonObject().has(name);
    }

    @Nonnull
    public JsonElement getOrLoad(@Nonnull JsonElement element) {

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            JsonElement path = obj.get("File");
            if (path != null && path.isJsonPrimitive() && path.getAsJsonPrimitive().isString()) {

                JsonElement loaded = this.loadFileElem(path.getAsString());
                element = Objects.requireNonNullElse(loaded, element);
            }
        }

        return element;
    }

    @Nullable
    public JsonElement get(String name) {

        if (this.json != null && this.json.isJsonObject()) {
            JsonElement element = this.json.getAsJsonObject().get(name);
            if (element != null && element.isJsonObject()) {
                element = this.getOrLoad(element);
            }

            return element;
        } else {
            return null;
        }
    }

    @Nullable
    public JsonElement getRaw(String name) {

        return this.json != null && this.json.isJsonObject() ? this.json.getAsJsonObject().get(name) : null;
    }

    protected JsonElement loadFile(@Nonnull String filePath) {

        Path file = this.dataFolder.resolve(filePath.replace('.', File.separatorChar) + ".json");

        try {
            JsonElement var4;
            if (!Files.exists(file)) {
                // MODIFIED (ie. Change Location!)
                Path foundPath = null;
                AssetPack[] assetPacks = AssetModule.get().getAssetPacks().stream()
                    .filter(assetPack -> !assetPack.getName().equals("Hytale:Hytale")).toArray(AssetPack[]::new);

                // Path to every AssetPack (Path pathToCaveMods : pathsToCaveMods)
                for (AssetPack pack : assetPacks) {
                    if (pack.isImmutable() && pack.getPackLocation().getFileName().toString().toLowerCase().endsWith(".zip")) {
                        // We are in a ZIP FILE
                        try (FileSystem fs = FileSystems.newFileSystem(pack.getPackLocation(), (ClassLoader) null)) {
                            Path manifestPath = fs.getPath("Server\\World\\CustomOres\\" + filePath.replace('.', File.separatorChar) + ".json");
                            if (Files.exists(manifestPath)) {
                                try (BufferedReader reader = Files.newBufferedReader(manifestPath, StandardCharsets.UTF_8)) {
                                    char[] buffer = RawJsonReader.READ_BUFFER.get();
                                    StringBuilder contentBuilder = new StringBuilder();
                                    int numCharsRead;
                                    while ((numCharsRead = reader.read(buffer)) != -1) {
                                        contentBuilder.append(buffer, 0, numCharsRead);
                                    }

                                    try (JsonReader jsonReader = new JsonReader(new StringReader(contentBuilder.toString()))) {
                                        return JsonParser.parseReader(jsonReader);
                                    }
                                }
                            }
                        }
                    } else {
                        // Normal Way (NON ZIP)
                        if (Files.exists(pack.getPackLocation().resolve("Server\\World\\CustomOres\\" + filePath.replace('.', File.separatorChar) + ".json"))) {
                            foundPath = pack.getPackLocation().resolve("Server\\World\\CustomOres\\" + filePath.replace('.', File.separatorChar) + ".json");
                        }
                    }
                }

                if (foundPath != null) {
                    try (JsonReader reader = new JsonReader(Files.newBufferedReader(foundPath))) {
                        var4 = JsonParser.parseReader(reader);
                    }
                } else {
                    throw new Error("File not found in any asset pack: " + filePath);
                }
            } else {
                // VANILLA JUST READ
                try (JsonReader reader = new JsonReader(Files.newBufferedReader(file))) {
                    var4 = JsonParser.parseReader(reader);
                }
            }

            return var4;
        } catch (Throwable var8) {
            throw new Error("Error while loading file reference." + file, var8); // ERROR THAT'S BEING THROWN
        }
    }

    protected JsonElement loadFileElem(@Nonnull String filePath) {

        return this.loadFile(filePath);
    }

    protected JsonElement loadFileConstructor(@Nonnull String filePath) {

        return this.loadFile(filePath);
    }

    @Nonnull
    protected JsonObject mustGetObject(@Nonnull String key, @Nullable JsonObject defaultValue) {

        return this.mustGet(key, defaultValue, JsonObject.class, JsonElement::isJsonObject, JsonElement::getAsJsonObject);
    }

    @Nonnull
    protected JsonArray mustGetArray(@Nonnull String key, @Nullable JsonArray defaultValue) {

        return this.mustGet(key, defaultValue, JsonArray.class, JsonElement::isJsonArray, JsonElement::getAsJsonArray);
    }

    @Nonnull
    protected String mustGetString(@Nonnull String key, @Nullable String defaultValue) {

        return this.mustGet(key, defaultValue, String.class, ModJsonLoader::isString, JsonElement::getAsString);
    }

    @Nonnull
    protected Boolean mustGetBool(@Nonnull String key, @Nullable Boolean defaultValue) {

        return this.mustGet(key, defaultValue, Boolean.class, ModJsonLoader::isBoolean, JsonElement::getAsBoolean);
    }

    @Nonnull
    protected Number mustGetNumber(@Nonnull String key, @Nullable Number defaultValue) {

        return this.mustGet(key, defaultValue, Number.class, ModJsonLoader::isNumber, JsonElement::getAsNumber);
    }

    protected <V> V mustGet(
        @Nonnull String key,
        @Nullable V defaultValue,
        @Nonnull Class<V> type,
        @Nonnull Predicate<JsonElement> predicate,
        @Nonnull Function<JsonElement, V> mapper
    ) {

        return mustGet(key, this.get(key), defaultValue, type, predicate, mapper);
    }
}
