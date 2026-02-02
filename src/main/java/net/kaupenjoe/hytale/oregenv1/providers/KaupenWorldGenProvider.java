package net.kaupenjoe.hytale.oregenv1.providers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.worldgen.HytaleWorldGenProvider;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.prefab.PrefabStoreRoot;
import net.kaupenjoe.hytale.oregenv1.loader.generator.ModChunkGeneratorJsonLoader;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;

public class KaupenWorldGenProvider extends HytaleWorldGenProvider {

    public static final String ID = "Hytale";

    public static final BuilderCodec<KaupenWorldGenProvider> CODEC = BuilderCodec.builder(KaupenWorldGenProvider.class, KaupenWorldGenProvider::new)
        .documentation("The standard generator for Hytale.")
        .append(new KeyedCodec<>("Name", Codec.STRING), (config, s) -> config.name = s, config -> config.name)
        .documentation("The name of the generator to use. \"*Default*\" if not provided.")
        .add()
        .append(new KeyedCodec<>("Path", Codec.STRING), (config, s) -> config.path = s, config -> config.path)
        .documentation("The path to the world generation configuration. \n\nDefaults to the server provided world generation folder if not set.")
        .add()
        .build();

    private String name = "Default";

    private String path;

    @Nonnull
    @Override
    public IWorldGen getGenerator() throws WorldGenLoadException {

        Path worldGenPath;
        if (this.path != null) {
            worldGenPath = PathUtil.get(this.path);
        } else {
            worldGenPath = Universe.getWorldGenPath();
        }

        if (!"Default".equals(this.name) || !Files.exists(worldGenPath.resolve("World.json"))) {
            worldGenPath = worldGenPath.resolve(this.name);
        }

        try {
            return new ModChunkGeneratorJsonLoader(new SeedString<>("ChunkGenerator", new SeedStringResource(PrefabStoreRoot.DEFAULT, worldGenPath)), worldGenPath)
                .load();
        } catch (Error var3) {
            throw new WorldGenLoadException("Failed to load world gen!", var3);
        }
    }

    @Nonnull
    @Override
    public String toString() {

        return "HytaleWorldGenProvider{name='" + this.name + "', path='" + this.path + "'}";
    }
}
