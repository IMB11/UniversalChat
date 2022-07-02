package mine.block.universalchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

@Environment(EnvType.CLIENT)
public class UniversalchatClient implements ClientModInitializer, PreLaunchEntrypoint {
    @Override
    public void onInitializeClient() {}

    @Override
    public void onPreLaunch() {}
}
