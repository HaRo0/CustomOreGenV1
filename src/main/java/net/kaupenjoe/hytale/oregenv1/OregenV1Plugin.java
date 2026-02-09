package net.kaupenjoe.hytale.oregenv1;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.assettypehandler.JsonTypeHandler;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAssetType;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorEditorType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import net.kaupenjoe.hytale.oregenv1.providers.CustomWorldGenProvider;
import org.bson.BsonDocument;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.nio.file.Path;

public class OregenV1Plugin extends JavaPlugin {

    public static OregenV1Plugin INSTANCE;

    public OregenV1Plugin(@NonNullDecl JavaPluginInit init) {

        super(init);
        INSTANCE = this;
    }

    @Override
    public void setup() {

        var defaultProvider = IWorldGenProvider.CODEC.getClassFor("Hytale");
        IWorldGenProvider.CODEC.remove(defaultProvider);
        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(1), "Hytale", CustomWorldGenProvider.class, CustomWorldGenProvider.CODEC);

        AssetEditorPlugin.get().getAssetTypeRegistry().registerAssetType(new TypeHandler("CaveModifications", "Server/World/CustomOres/CaveModifications"));
        AssetEditorPlugin.get().getAssetTypeRegistry().registerAssetType(new TypeHandler("ZoneData", "Server/World/CustomOres/Ores"));

    }

    private static class TypeHandler extends JsonTypeHandler {

        protected TypeHandler(String id, String path) {

            super(new AssetEditorAssetType(id, null, false, path, ".json", AssetEditorEditorType.JsonSource));
        }

        @Override
        public AssetLoadResult loadAsset(AssetPath var1, Path var2, byte[] var3, AssetUpdateQuery var4, EditorClient var5) {

            return AssetLoadResult.ASSETS_CHANGED;
        }

        @Override
        public AssetLoadResult loadAssetFromDocument(AssetPath var1, Path var2, BsonDocument var3, AssetUpdateQuery var4, EditorClient var5) {

            return AssetLoadResult.ASSETS_CHANGED;
        }

        @Override
        public AssetLoadResult unloadAsset(AssetPath var1, AssetUpdateQuery var2) {

            return AssetLoadResult.ASSETS_CHANGED;
        }

        @Override
        public AssetLoadResult restoreOriginalAsset(AssetPath var1, AssetUpdateQuery var2) {

            return AssetLoadResult.ASSETS_CHANGED;
        }

        @Override
        public AssetUpdateQuery getDefaultUpdateQuery() {

            return AssetUpdateQuery.DEFAULT;
        }
    }
}
