package mine.block.universalchat;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import mine.block.universalchat.config.UniversalChatConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

@Environment(EnvType.CLIENT)
public class UniversalchatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AutoConfig.register(UniversalChatConfig.class, GsonConfigSerializer::new);
    }
}
