package mine.block.universalchat.mixin;

import me.bush.translator.Language;
import me.bush.translator.LanguageKt;
import me.bush.translator.Translation;
import me.shedaniel.autoconfig.AutoConfig;
import mine.block.universalchat.config.UniversalChatConfig;
import mine.block.universalchat.detection.DetectionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.network.message.MessageSender;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {
    @Shadow @Final private MinecraftClient client;

    private void manualAdd(MessageType.DisplayRule type, Text message, MessageSender sender) {
        Text text2 = type.apply(message, sender);
        if (sender == null) {
            this.client.inGameHud.getChatHud().addMessage(text2);
        } else {
            this.client.inGameHud.getChatHud().queueMessage(text2);
        }
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void modifyOnChatMessage(MessageType type, Text message, MessageSender sender, CallbackInfo ci) throws ExecutionException, InterruptedException {
        Language target = AutoConfig.getConfigHolder(UniversalChatConfig.class).get().targetLanguage;

        CompletableFuture<Text> future = new CompletableFuture<Text>().completeAsync(() -> {
            AtomicReference<Text> text = new AtomicReference<>(message);
            type.chat().ifPresent((ignored) -> {
                String messageContent = message.getString();

                Translation result = DetectionManager.Post(messageContent);

                Style style = Text.literal(result.getTranslatedText()).getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Translated from " + new Locale(result.getSourceLanguage().getCode()).getDisplayLanguage() + ": ").append(Text.literal(messageContent).formatted(Formatting.GRAY))));
                manualAdd(ignored, Text.literal(result.getTranslatedText()).setStyle(style), sender);
            });

            return null;
        });

        ci.cancel();
    }
}
