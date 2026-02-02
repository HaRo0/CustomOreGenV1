package net.kaupenjoe.hytale.oregenv1.loader.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveGenerator;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import net.kaupenjoe.hytale.oregenv1.loader.type.ModCaveTypesJsonLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModCaveGeneratorJsonLoader extends JsonLoader<SeedStringResource, CaveGenerator> {

    protected final Path caveFolder;

    protected final ZoneFileContext zoneContext;

    public ModCaveGeneratorJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, Path caveFolder, ZoneFileContext zoneContext) {

        super(seed.append(".CaveGenerator"), dataFolder, json);
        this.caveFolder = caveFolder;
        this.zoneContext = zoneContext;
    }

    private static String getZoneName(String zoneName) {

        int start = zoneName.indexOf("Zones/") + "Zones/".length();
        int end = zoneName.lastIndexOf("/");
        String zone = zoneName.substring(start, end);
        return zone;
    }

    // CAVEMODIFICATION.JSON
    @Nullable
    public CaveGenerator load() {

        CaveGenerator caveGenerator = null;
        if (this.caveFolder != null && Files.exists(this.caveFolder)) {
            Path file = this.caveFolder.resolve("Caves.json");

            try {
                JsonObject cavesJson;
                try (JsonReader reader = new JsonReader(Files.newBufferedReader(file))) {
                    cavesJson = JsonParser.parseReader(reader).getAsJsonObject();
                }
                String zoneName = getZoneName(this.caveFolder.toString());

                // Modifying the JSONObject itself and adding All Packs custom things
                // Path pathToCaveMods = AssetModule.get().getAssetPack("Lexih:TestPlugin");

                AssetPack[] assetPacks = AssetModule.get().getAssetPacks().stream()
                    .filter(assetPack -> !assetPack.getName().equals("Hytale:Hytale")).toArray(AssetPack[]::new);

                JsonObject modifiedCaves;
                JsonArray oreModificationFileList = null;

                // Path to every AssetPack (Path pathToCaveMods : pathsToCaveMods)
                for (AssetPack pack : assetPacks) {
                    if (pack.isImmutable() && pack.getPackLocation().getFileName().toString().toLowerCase().endsWith(".zip")) {
                        try (FileSystem fs = FileSystems.newFileSystem(pack.getPackLocation(), (ClassLoader) null)) {
                            Path manifestPath = fs.getPath("Server\\World\\KaupenOres\\CaveModifications\\CaveModifications.json");
                            if (Files.exists(manifestPath)) {
                                try (BufferedReader reader = Files.newBufferedReader(manifestPath, StandardCharsets.UTF_8)) {
                                    char[] buffer = RawJsonReader.READ_BUFFER.get();
                                    int numCharsRead;
                                    String content = "";
                                    while ((numCharsRead = reader.read(buffer)) != -1) {
                                        // Create the string using ONLY the characters that were just read
                                        content = new String(buffer, 0, numCharsRead);
                                        System.out.println("[CONTENT]: " + content);
                                    }

                                    try (JsonReader jsonReader = new JsonReader(new StringReader(content))) {
                                        modifiedCaves = JsonParser.parseReader(jsonReader).getAsJsonObject();
                                    }

                                    if (modifiedCaves.get(zoneName) != null) {
                                        oreModificationFileList = modifiedCaves.get(zoneName).getAsJsonArray();

                                        for (JsonElement element : oreModificationFileList) {
                                            cavesJson.get("Types").getAsJsonArray().add(element);
                                        }
                                    }

                                }
                            }
                        }
                    } else {
                        // Normal Way (NON ZIP)
                        try (JsonReader reader = new JsonReader(Files.newBufferedReader(pack.getPackLocation()
                            .resolve("Server\\World\\KaupenOres\\CaveModifications\\CaveModifications.json")))) {
                            modifiedCaves = JsonParser.parseReader(reader).getAsJsonObject();

                            if (modifiedCaves.get(zoneName) != null) {
                                oreModificationFileList = modifiedCaves.get(zoneName).getAsJsonArray();

                                for (JsonElement element : oreModificationFileList) {
                                    cavesJson.get("Types").getAsJsonArray().add(element);
                                }
                            }

                            System.out.println("[FINISHED CAVE JSON]: " + cavesJson);
                        }
                    }
                }

                caveGenerator = new CaveGenerator(this.loadCaveTypes(cavesJson));
            } catch (Throwable var9) {
                throw new Error(String.format("Error while loading caves for world generator from %s", file), var9);
            }
        }

        return caveGenerator;
    }

    @Nonnull
    protected CaveType[] loadCaveTypes(@Nonnull JsonObject jsonObject) {

        return new ModCaveTypesJsonLoader(this.seed, this.dataFolder, jsonObject.get("Types"), this.caveFolder, this.zoneContext).load();
    }

    public interface Constants {

        String FILE_CAVES_JSON = "Caves.json";

        String KEY_TYPES = "Types";

        String ERROR_LOADING_CAVES = "Error while loading caves for world generator from %s";
    }
}
