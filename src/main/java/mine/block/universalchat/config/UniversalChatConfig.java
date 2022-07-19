package mine.block.universalchat.config;

import me.bush.translator.Language;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "universalchat")
public class UniversalChatConfig implements ConfigData {
    public Language targetLanguage = Language.ENGLISH;
}
