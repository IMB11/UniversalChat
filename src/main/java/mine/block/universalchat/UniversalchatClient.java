package mine.block.universalchat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import io.netty.util.concurrent.CompleteFuture;
import me.bush.translator.Language;
import me.bush.translator.LanguageKt;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import mine.block.universalchat.config.UniversalChatConfig;
import mine.block.universalchat.detection.DetectionManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class UniversalchatClient implements ClientModInitializer {

    enum CommandLanguage implements StringIdentifiable {
        AFRIKAANS("af"),
        ALBANIAN("sq"),
        AMHARIC("am"),
        ARABIC("ar"),
        ARMENIAN("hy"),
        AZERBAIJANI("az"),
        BASQUE("eu"),
        BELARUSIAN("be"),
        BENGALI("bn"),
        BOSNIAN("bs"),
        BULGARIAN("bg"),
        CATALAN("ca"),
        CEBUANO("ceb"),
        CHICHEWA("ny"),
        CHINESE_SIMPLIFIED("zh-cn"),
        CHINESE_TRADITIONAL("zh-tw"),
        CORSICAN("co"),
        CROATIAN("hr"),
        CZECH("cs"),
        DANISH("da"),
        DUTCH("nl"),
        ENGLISH("en"),
        ESPERANTO("eo"),
        ESTONIAN("et"),
        FILIPINO("tl"),
        FINNISH("fi"),
        FRENCH("fr"),
        FRISIAN("fy"),
        GALICIAN("gl"),
        GEORGIAN("ka"),
        GERMAN("de"),
        GREEK("el"),
        GUJARATI("gu"),
        HATIAN_CREOLE("ht"),
        HAUSA("ha"),
        HAWAIIAN("haw"),
        HEBREW_IW("iw"),
        HEBREW_HE("he"),
        HINDI("hi"),
        HMONG("hm"),
        HUNGARIAN("hu"),
        ICELANDIC("is"),
        IGBO("ig"),
        INDONESIAN("id"),
        IRISH("ga"),
        ITALIAN("it"),
        JAPANESE("ja"),
        JAVANESE("jw"),
        KANNADA("kn"),
        KAZAKH("kk"),
        KHMER("km"),
        KOREAN("ko"),
        KURDISH_KURMANJI("ku"),
        KYRGYZ("ky"),
        LAO("lo"),
        LATIN("la"),
        LATVIAN("lv"),
        LITHUANIAN("lt"),
        LUXEMBOURGISH("lb"),
        MACEDONIAN("mk"),
        MALAGASY("mg"),
        MALAY("ms"),
        MALAYALAM("ml"),
        MALTESE("mt"),
        MAORI("mi"),
        MARATHI("mr"),
        MONGOLIAN("mn"),
        MYANMAR_BURMESE("my"),
        NEPALI("ne"),
        NORWEGIAN("no"),
        ODIA("or"),
        PASHTO("ps"),
        PERSIAN("fa"),
        POLISH("pl"),
        PORTUGUESE("pt"),
        PUNJABI("pa"),
        ROMANIAN("ro"),
        RUSSIAN("ru"),
        SAMOAN("sm"),
        SCOTS_GAELIC("gd"),
        SERBIAN("sr"),
        SESOTHO("st"),
        SHONA("sn"),
        SINDHI("sd"),
        SINHALA("si"),
        SLOVAK("sk"),
        SLOVENIAN("sl"),
        SOMALI("so"),
        SPANISH("es"),
        SUDANESE("su"),
        SWAHILI("sw"),
        SWEDISH("sv"),
        TAJIK("tg"),
        TAMIL("ta"),
        TELUGU("te"),
        THAI("th"),
        TURKISH("tr"),
        UKRAINIAN("uk"),
        URDU("ur"),
        UYGHUR("ug"),
        UZBEK("uz"),
        VIETNAMESE("vi"),
        WELSH("cy"),
        XHOSA("xh"),
        YIDDISH("yi"),
        YORUBA("yo"),
        ZULU("zu");

        private final String code;

        public static final Codec<CommandLanguage> CODEC = StringIdentifiable.createCodec(CommandLanguage::values);

        CommandLanguage(String code) {
            this.code = code;
        }

        @Override
        public String asString() {
            return code;
        }
    }

    public class CEnumArgumentType<T extends Enum<T> & StringIdentifiable> extends EnumArgumentType<T> {
        public CEnumArgumentType(Codec<T> codec, Supplier<T[]> valuesSupplier) {
            super(codec, valuesSupplier);
        }
    }

    @Override
    public void onInitializeClient() {
        AutoConfig.register(UniversalChatConfig.class, GsonConfigSerializer::new);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("translate").then(
                            argument("targetLangCode", new CEnumArgumentType<>(CommandLanguage.CODEC, CommandLanguage::values)).then(
                                    argument("content", StringArgumentType.string()).executes(context -> {
                                        CommandLanguage language = context.getArgument("targetLangCode", CommandLanguage.class);
                                        String content = StringArgumentType.getString(context, "content");

                                        new CompletableFuture<String>().completeAsync(() -> {
                                            if(!DetectionManager.netIsAvailable()) {
                                                context.getSource().sendError(Text.literal("You are not connected to the internet.").formatted(Formatting.RED, Formatting.BOLD));
                                                return "";
                                            }

                                            var translated = DetectionManager.translator.translateBlocking(content, LanguageKt.languageOf(language.code), Language.AUTO);

                                            Style style = Text.literal("").getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Translated from " + new Locale(translated.getSourceLanguage().getCode()).getDisplayLanguage() + ": ").append(Text.literal(content).formatted(Formatting.GRAY))));

                                            context.getSource().sendFeedback(Text.literal(translated.getTranslatedText()).setStyle(style));

                                            return "";
                                        });

                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            );
        });
    }
}
