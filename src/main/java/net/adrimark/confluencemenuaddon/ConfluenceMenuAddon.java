package net.adrimark.confluencemenuaddon;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.bus.api.IEventBus;

import net.adrimark.confluencemenuaddon.client.ClientConfigScreen;
import net.adrimark.confluencemenuaddon.config.ClientConfig;

// Client-only mod, no server-side content. See ClientConfig for what's configurable.
@Mod(ConfluenceMenuAddon.MODID)
public class ConfluenceMenuAddon {

    public static final String MODID = "confluencemenuaddon";

    public ConfluenceMenuAddon(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            ClientConfigScreen.register(modContainer);
        }
    }
}
