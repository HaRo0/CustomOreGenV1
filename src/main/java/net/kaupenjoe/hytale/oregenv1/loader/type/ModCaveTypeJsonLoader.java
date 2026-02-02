package net.kaupenjoe.hytale.oregenv1.loader.type;

import com.google.gson.JsonElement;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.HeightThresholdCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.IHeightThresholdInterpreter;
import com.hypixel.hytale.procedurallib.json.*;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.procedurallib.supplier.IFloatRange;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveBiomeMaskFlags;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.loader.cave.CaveBiomeMaskJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.cave.CaveNodeTypeStorage;
import com.hypixel.hytale.server.worldgen.loader.cave.FluidLevelJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.prefab.BlockPlacementMaskJsonLoader;
import com.hypixel.hytale.server.worldgen.util.ConstantNoiseProperty;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import com.hypixel.hytale.server.worldgen.util.condition.flag.Int2FlagsCondition;
import net.kaupenjoe.hytale.oregenv1.loader.ModJsonLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

public class ModCaveTypeJsonLoader extends ModJsonLoader<SeedStringResource, CaveType> {

    protected final Path caveFolder;

    protected final String name;

    protected final ZoneFileContext zoneContext;

    public ModCaveTypeJsonLoader(
        @Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, Path caveFolder, String name, ZoneFileContext zoneContext
    ) {

        super(seed.append(".CaveType"), dataFolder, json);
        this.caveFolder = caveFolder;
        this.name = name;
        this.zoneContext = zoneContext;
    }

    @Nonnull
    public CaveType load() {

        IPointGenerator pointGenerator = this.loadEntryPointGenerator();
        return new CaveType(
            this.name,
            this.loadEntryNodeType(),
            this.loadYaw(),
            this.loadPitch(),
            this.loadDepth(),
            this.loadHeightFactors(),
            pointGenerator,
            this.loadBiomeMask(),
            this.loadBlockMask(),
            this.loadMapCondition(),
            this.loadHeightCondition(),
            this.loadFixedEntryHeight(),
            this.loadFixedEntryHeightNoise(),
            this.loadFluidLevel(),
            this.loadEnvironment(),
            this.loadSurfaceLimited(),
            this.loadSubmerge(),
            this.loadMaximumSize(pointGenerator)
        );
    }

    @Nonnull
    protected IFloatRange loadYaw() {

        return new FloatRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Yaw"), -180.0F, 180.0F, deg -> deg * (float) (Math.PI / 180.0)).load();
    }

    @Nonnull
    protected IFloatRange loadPitch() {

        return new FloatRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Pitch"), -15.0F, deg -> deg * (float) (Math.PI / 180.0)).load();
    }

    @Nonnull
    protected IFloatRange loadDepth() {

        return new FloatRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Depth"), 80.0F).load();
    }

    @Nullable
    protected IHeightThresholdInterpreter loadHeightFactors() {

        return new HeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, this.get("HeightRadiusFactor"), 320).load();
    }

    @Nonnull
    protected CaveNodeType loadEntryNodeType() {

        JsonElement entry = this.get("Entry");
        if (entry == null) {
            throw new IllegalArgumentException("\"Entry\" is not defined. Define an entry node type");
        } else {
            CaveNodeTypeStorage caveNodeTypeStorage = new CaveNodeTypeStorage(this.seed, this.dataFolder, this.caveFolder, this.zoneContext);
            if (entry.isJsonObject()) {
                String entryNodeTypeString = this.seed.get().getUniqueName("CaveType#");
                return caveNodeTypeStorage.loadCaveNodeType(entryNodeTypeString, entry.getAsJsonObject());
            } else if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
                String entryNodeTypeString = entry.getAsString();
                return caveNodeTypeStorage.loadCaveNodeType(entryNodeTypeString);
            } else {
                throw error("Invalid entry node type definition! Expected String or JsonObject: " + entry);
            }
        }
    }

    @Nonnull
    protected ICoordinateCondition loadHeightCondition() {

        ICoordinateCondition heightCondition = DefaultCoordinateCondition.DEFAULT_TRUE;
        if (this.has("HeightThreshold")) {
            IHeightThresholdInterpreter interpreter = new HeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, this.get("HeightThreshold"), 320)
                .load();
            heightCondition = new HeightThresholdCoordinateCondition(interpreter);
        }

        return heightCondition;
    }

    @Nullable
    protected IPointGenerator loadEntryPointGenerator() {

        if (!this.has("EntryPoints")) {
            throw new IllegalArgumentException("\"EntryPoints\" is not defined, no spawn information for caves available");
        } else {
            return new PointGeneratorJsonLoader<>(this.seed, this.dataFolder, this.get("EntryPoints")).load();
        }
    }

    @Nonnull
    protected Int2FlagsCondition loadBiomeMask() {

        Int2FlagsCondition mask = CaveBiomeMaskFlags.DEFAULT_ALLOW;
        if (this.has("BiomeMask")) {
            ZoneFileContext context = this.zoneContext.matchContext(this.json, "BiomeMask");
            mask = new CaveBiomeMaskJsonLoader(this.seed, this.dataFolder, this.get("BiomeMask"), context).load();
        }

        return mask;
    }

    @Nullable
    protected BlockMaskCondition loadBlockMask() {

        BlockMaskCondition placementConfiguration = BlockMaskCondition.DEFAULT_TRUE;
        if (this.has("BlockMask")) {
            placementConfiguration = new BlockPlacementMaskJsonLoader(this.seed, this.dataFolder, this.getRaw("BlockMask")).load();
        }

        return placementConfiguration;
    }

    @Nonnull
    protected ICoordinateCondition loadMapCondition() {

        return new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
    }

    @Nullable
    protected IDoubleRange loadFixedEntryHeight() {

        IDoubleRange fixedEntryHeight = null;
        if (this.has("FixedEntryHeight")) {
            fixedEntryHeight = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("FixedEntryHeight"), 0.0).load();
        }

        return fixedEntryHeight;
    }

    @Nullable
    protected NoiseProperty loadFixedEntryHeightNoise() {

        NoiseProperty maxNoise = ConstantNoiseProperty.DEFAULT_ZERO;
        if (this.has("FixedEntryHeightNoise")) {
            maxNoise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("FixedEntryHeightNoise")).load();
        }

        return maxNoise;
    }

    @Nonnull
    protected CaveType.FluidLevel loadFluidLevel() {

        CaveType.FluidLevel fluidLevel = CaveType.FluidLevel.EMPTY;
        if (this.has("FluidLevel")) {
            fluidLevel = new FluidLevelJsonLoader(this.seed, this.dataFolder, this.get("FluidLevel")).load();
        }

        return fluidLevel;
    }

    protected int loadEnvironment() {

        int environment = Integer.MIN_VALUE;
        if (this.has("Environment")) {
            JsonElement envElement = this.get("Environment");
            if (envElement != null && envElement.isJsonPrimitive()) {
                String environmentId = envElement.getAsString();
                environment = Environment.getAssetMap().getIndex(environmentId);
                if (environment == Integer.MIN_VALUE) {
                    throw new Error(String.format("Error while looking up environment \"%s\"!", environmentId));
                }
            }
        }

        return environment;
    }

    protected boolean loadSurfaceLimited() {

        if (this.has("SurfaceLimited")) {
            JsonElement element = this.get("SurfaceLimited");
            return element != null && element.getAsBoolean();
        }
        return true;
    }

    protected boolean loadSubmerge() {

        return this.mustGetBool("Submerge", false);
    }

    protected double loadMaximumSize(@Nullable IPointGenerator pointGenerator) {

        if (this.has("MaximumSize")) {
            JsonElement element = this.get("MaximumSize");
            if (element != null && element.isJsonPrimitive()) {
                return element.getAsLong();
            }
        }
        return pointGenerator != null ? MathUtil.fastFloor(pointGenerator.getInterval()) : 0.0;
    }
}
