package mine.block.universalchat.mixin;

import me.bush.translator.Translation;
import me.shedaniel.autoconfig.AutoConfig;
import mine.block.universalchat.config.UniversalChatConfig;
import mine.block.universalchat.detection.DetectionManager;
import net.minecraft.client.gui.screen.ChatPreviewBackground;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatPreviewBackground.RenderData.class)
public class RenderDataMixin {

    private static final HashMap<String, Translation> stringStringHashMap = new HashMap<>();

    @Inject(method = "preview", at = @At("RETURN"), cancellable = true)
    public void setPreview(CallbackInfoReturnable<Text> cir) {
        Text old = cir.getReturnValue();
        if(old == null || !AutoConfig.getConfigHolder(UniversalChatConfig.class).get().enablePreviewTranslation) return;
        String nonTranslated = old.getString();

        if(nonTranslated.contains("[Enter]")) return;

        if(stringStringHashMap.containsKey(nonTranslated)) {
            Translation translation = stringStringHashMap.get(nonTranslated);
            cir.setReturnValue(Text.literal(translation.getTranslatedText()));
        } else {
            if(!DetectionManager.netIsAvailable()) return;
            var t = DetectionManager.Post(nonTranslated);
            stringStringHashMap.put(nonTranslated, t);
            cir.setReturnValue(Text.literal(t.getTranslatedText()));
        }
    }
}
