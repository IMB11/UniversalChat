package mine.block.universalchat.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuEntry implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return parent -> AutoConfig.getConfigScreen(UniversalChatConfig.class, parent).get(); }
}
