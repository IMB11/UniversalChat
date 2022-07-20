package mine.block.universalchat.mixin;

import me.shedaniel.autoconfig.AutoConfig;
import mine.block.universalchat.config.UniversalChatConfig;
import mine.block.universalchat.detection.DetectionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Shadow protected abstract void addMessage(Text message, int messageId);

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    public void addMessage(Text text, CallbackInfo ci) {
//
//        CompletableFuture future = new CompletableFuture().completeAsync(() -> {
//            String input = text.getString();
//            String language = DetectionManager.getLanguage(input);
//
//            String translated = DetectionManager.Post(input, language, AutoConfig.getConfigHolder(UniversalChatConfig.class).getConfig().targetLanguage.getCode());
//
//            if(translated.equals(input)) {
//                this.addMessage(text, 0);
//                return null;
//            }
//
//            if(translated.equals("translation_err")) {
//                Style style = text.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("A translation error has occured. We were unable to translate this message.").formatted(Formatting.RED, Formatting.ITALIC)));
//                this.addMessage(text.copy().formatted(Formatting.UNDERLINE).setStyle(style), 0);
//            } else {
//                Style style = Text.literal(translated).getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Translated from " + new Locale(language).getDisplayLanguage() + ": ").append(Text.literal(input).formatted(Formatting.GRAY))));
//                this.addMessage(Text.literal(translated).copy().setStyle(style), 0);
//            }
//            return null;
//        });
//
//        ci.cancel();
    }

}