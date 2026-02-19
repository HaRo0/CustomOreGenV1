package net.kaupenjoe.hytale.oregenv1.loader.zone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.biome.BiomePatternGenerator;
import com.hypixel.hytale.server.worldgen.biome.CustomBiome;
import com.hypixel.hytale.server.worldgen.biome.TileBiome;
import com.hypixel.hytale.server.worldgen.cave.CaveGenerator;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.biome.BiomePatternGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.cave.CaveGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.container.UniquePrefabContainerJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.zone.ZoneBiomesJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.zone.ZoneCustomBiomesJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.zone.ZoneJsonLoader;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import com.hypixel.hytale.server.worldgen.zone.ZoneDiscoveryConfig;
import net.kaupenjoe.hytale.oregenv1.loader.generator.ModCaveGeneratorJsonLoader;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

public class ModZoneJsonLoader extends ZoneJsonLoader {


    public ModZoneJsonLoader(@NonNullDecl SeedString<SeedStringResource> seed, @NonNullDecl Path dataFolder, @NonNullDecl JsonElement json, @NonNullDecl ZoneFileContext zoneContext) {
        super(seed, dataFolder, json, zoneContext);
    }

    @Override
    @Nullable
    protected CaveGenerator loadCaveGenerator() {

        try {
            return new ModCaveGeneratorJsonLoader(this.seed, this.dataFolder, this.json, this.zoneContext.getPath().resolve("Cave"), this.zoneContext).load();
        } catch (Throwable var2) {
            throw new Error("Error while loading cave generator.", var2);
        }
    }
}
