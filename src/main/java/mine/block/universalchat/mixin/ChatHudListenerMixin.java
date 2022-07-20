package mine.block.universalchat.mixin;

import me.bush.translator.Language;
import me.bush.translator.Translation;
import me.shedaniel.autoconfig.AutoConfig;
import mine.block.universalchat.config.UniversalChatConfig;
import mine.block.universalchat.detection.DetectionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.network.MessageType;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {
    @Shadow @Final private MinecraftClient client;

    private void manualAdd(MessageType type, Text message) {
        if (type == MessageType.CHAT) {
            this.client.inGameHud.getChatHud().addMessage(message);
        } else {
            this.client.inGameHud.getChatHud().queueMessage(message);
        }
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void modifyOnChatMessage(MessageType type, Text message, UUID sender, CallbackInfo ci) throws ExecutionException, InterruptedException {
        Language target = AutoConfig.getConfigHolder(UniversalChatConfig.class).get().targetLanguage;

        CompletableFuture<Text> future = new CompletableFuture<Text>().completeAsync(() -> {
            AtomicReference<Text> text = new AtomicReference<>(message);
            if (type == MessageType.CHAT) {
                String messageContent = message.getString();

                if(!DetectionManager.netIsAvailable()) manualAdd(type, text.get());

                Translation result = DetectionManager.Post(messageContent);

                Style style = new LiteralText(result.getTranslatedText()).getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Translated from " + new Locale(result.getSourceLanguage().getCode()).getDisplayLanguage() + ": ").append(new LiteralText(messageContent).formatted(Formatting.GRAY))));
                manualAdd(type, new LiteralText(result.getTranslatedText()).setStyle(style));
            } else {
                manualAdd(type, text.get());
            }

            return null;
        });

        ci.cancel();
    }
}
