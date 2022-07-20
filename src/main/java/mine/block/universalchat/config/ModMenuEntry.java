package mine.block.universalchat.config;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuEntry implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return parent -> AutoConfig.getConfigScreen(UniversalChatConfig.class, parent).get(); }
}
