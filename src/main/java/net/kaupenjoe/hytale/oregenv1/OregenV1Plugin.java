package net.kaupenjoe.hytale.oregenv1;

import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import net.kaupenjoe.hytale.oregenv1.providers.KaupenWorldGenProvider;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class OregenV1Plugin extends JavaPlugin {

    public OregenV1Plugin(@NonNullDecl JavaPluginInit init) {

        super(init);
    }

    @Override
    public void setup() {

        var defaultProvider = IWorldGenProvider.CODEC.getClassFor("Hytale");
        IWorldGenProvider.CODEC.remove(defaultProvider);
        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(1), "Hytale", KaupenWorldGenProvider.class, KaupenWorldGenProvider.CODEC);
    }
}
