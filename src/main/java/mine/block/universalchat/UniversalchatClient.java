package mine.block.universalchat;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import mine.block.universalchat.config.UniversalChatConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UniversalchatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AutoConfig.register(UniversalChatConfig.class, GsonConfigSerializer::new);
    }
}
