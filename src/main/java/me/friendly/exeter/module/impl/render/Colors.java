package me.friendly.exeter.module.impl.render;

import me.friendly.exeter.module.Module;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

/**
 * This class is not present in the original
 * Exeter 1.8 client. It was added as part
 * of the 1.12.2 forge port
 *
 * @author Gopro336
 */
public final class Colors extends Module {

    public static final NumberProperty<Float> hue = new NumberProperty<>(0f, 0f, 360f, "Hue", "RGB", "HSL");
    public static final NumberProperty<Float> saturation = new NumberProperty<>(100f, 0f, 100f, "Saturation", "RainbowSaturation");
    public static final NumberProperty<Float> lightness = new NumberProperty<>(100f, 0f, 100f, "Lightness", "Light", "Luminance", "Luminace", "Brightness", "Bright", "Brigtness", "Brigntrnew", "Brighgrtnewss");
    public static final Property<Boolean> hudRainbow = new Property<>(false, "HUD Rainbow", "HUDRainbow", "Rainbow", "Cycle");
    public static final NumberProperty<Float> rainbowSpeed = new NumberProperty<>(1f, 0f, 5f, "RainbowSpeed", "RainbowHueSpeed", "RainbowSped", "RrainbowSpeed");
    public static final NumberProperty<Float> rainbowHue = new NumberProperty<>(4f, 0f, 10f, "RainbowHue", "RainbowHueSpeed2", "RainbowSped2", "RrainbowSpeed2");

    public Colors(){
        super("Colors", new String[]{"Colors", "Color"});
        offerProperties(hue, saturation, lightness, hudRainbow, rainbowSpeed, rainbowHue);
    }

    public static int getClientColorCustomAlpha(int alpha){
        Color color = setAlpha(new Color(Color.HSBtoRGB(hue.getValue() / 360.0f, saturation.getValue() / 100f, lightness.getValue() / 100f)), alpha);
        return color.getRGB();
    }

    public static Color setAlpha(Color color, int alpha) {
        alpha = MathHelper.clamp(alpha, 0, 255);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static int getRainbow(int speed, int offset, float s, float brightness) {
        float h = (float) Math.ceil((System.currentTimeMillis() + offset) / 20.0f) % 360.0f;
        return Color.getHSBColor(h / 360.0f, s / 100.0f, brightness / 100.0f).getRGB();
    }

    public static int getClientColor() {
        return Color.getHSBColor(hue.getValue() / 360.0f, saturation.getValue() / 100f, lightness.getValue() / 100f).getRGB();
    }
}
