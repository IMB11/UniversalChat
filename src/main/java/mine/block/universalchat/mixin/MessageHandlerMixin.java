package mine.block.universalchat.mixin;

import me.bush.translator.Language;
import me.bush.translator.Translation;
import me.shedaniel.autoconfig.AutoConfig;
import mine.block.universalchat.config.UniversalChatConfig;
import mine.block.universalchat.detection.DetectionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.*;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow
    protected abstract void process(MessageHandler.ProcessableMessage processableMessage);
    @Shadow public abstract boolean processProfilelessMessage(MessageType.Parameters params, SignedMessage message, Text decorated);

    @Shadow public abstract boolean processHeader(MessageHeader header, MessageSignatureData signature, byte[] bodyDigest);

    @Shadow
    public abstract boolean processChatMessage(MessageType.Parameters params, SignedMessage message, Text text, PlayerListEntry playerListEntry, boolean bl, Instant instant);

    @Shadow @Nullable protected abstract PlayerListEntry getPlayerListEntry(UUID sender);


    private void manualAdd(final SignedMessage message, final MessageType.Parameters params, Text translatedContent) {
        final boolean bl = this.client.options.getOnlyShowSecureChat().getValue();
        final SignedMessage signedMessage = bl ? message.withoutUnsigned() : message;
        final Text text = params.applyChatDecoration(translatedContent);
        MessageMetadata messageMetadata = message.createMetadata();
        if (!messageMetadata.lacksSender()) {
            final PlayerListEntry playerListEntry = this.getPlayerListEntry(messageMetadata.sender());
            final Instant instant = Instant.now();
            this.process(new MessageHandler.ProcessableMessage() {
                private boolean processed;

                public boolean accept() {
                    if (this.processed) {
                        byte[] bs = message.signedBody().digest().asBytes();
                        processHeader(message.signedHeader(), message.headerSignature(), bs);
                        return false;
                    } else {
                        return processChatMessage(params, message, text, playerListEntry, bl, instant);
                    }
                }

                public boolean removeMatching(MessageSignatureData signature) {
                    if (message.headerSignature().equals(signature)) {
                        this.processed = true;
                        return true;
                    } else {
                        return false;
                    }
                }

                public void markProcessed() {
                    this.processed = true;
                }

                public boolean isUnprocessed() {
                    return !this.processed;
                }
            });
        } else {
            this.process(new MessageHandler.ProcessableMessage() {
                public boolean accept() {
                    return processProfilelessMessage(params, signedMessage, text);
                }

                public boolean isUnprocessed() {
                    return true;
                }
            });
        }
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void modifyOnChatMessage(SignedMessage message, MessageType.Parameters params, CallbackInfo ci) throws ExecutionException, InterruptedException {
        Language target = AutoConfig.getConfigHolder(UniversalChatConfig.class).get().targetLanguage;

        CompletableFuture<Text> future = new CompletableFuture<Text>().completeAsync(() -> {
            String messageContent = message.getContent().getString();

            if(!DetectionManager.netIsAvailable()) manualAdd(message, params, message.getContent());

            Translation result = DetectionManager.Post(messageContent);

            Style style = Text.literal(result.getTranslatedText()).getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Translated from " + new Locale(result.getSourceLanguage().getCode()).getDisplayLanguage() + ": ").append(Text.literal(messageContent).formatted(Formatting.GRAY))));
            manualAdd(message, params, Text.literal(result.getTranslatedText()).setStyle(style));
            return null;
        });

        ci.cancel();
    }
}
