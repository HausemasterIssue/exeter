package me.friendly.exeter.module.impl.render.clickgui.item.properties;

import me.friendly.api.minecraft.render.RenderMethods;
import me.friendly.api.minecraft.render.font.FontUtil;
import me.friendly.exeter.module.impl.render.Colors;
import me.friendly.exeter.module.impl.render.clickgui.ClickGui;
import me.friendly.exeter.module.impl.render.clickgui.Panel;
import me.friendly.exeter.module.impl.render.clickgui.item.Item;
import me.friendly.api.properties.NumberProperty;
import org.lwjgl.input.Mouse;

public class NumberSlider
extends Item {
    private NumberProperty numberProperty;
    private float difference;

    public NumberSlider(NumberProperty numberProperty) {
        super(numberProperty.getAliases()[0]);
        this.numberProperty = numberProperty;
        this.difference = numberProperty.getMaximum().floatValue() - numberProperty.getMinimum().floatValue();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        dragSetting(mouseX, mouseY);

        float percent = (((Number) numberProperty.getValue()).floatValue() - numberProperty.getMinimum().floatValue()) / difference;
        float barWidth = (width) * percent;

        RenderMethods.drawRect(x, y, ((Number) numberProperty.getValue()).floatValue() <= numberProperty.getMinimum().floatValue() ? x : x + barWidth, y + height - 0.5f, !isHovering(mouseX, mouseY) ? Colors.getClientColorCustomAlpha(77) : Colors.getClientColorCustomAlpha(55));
        FontUtil.drawString(String.format("%s\u00a77 %s", this.getLabel(), this.numberProperty.getValue()), this.x + 2.0f, this.y + 4.0f, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY) && mouseButton == 0) {
            setSettingFromX(mouseX);
            Item.playClickSound();
        }
    }

    private void setSettingFromX(int mouseX) {
        float percent = (mouseX - x) / (width);
        if(numberProperty.getValue() instanceof Double) {
            double result = (Double)numberProperty.getMinimum() + (difference * percent);
            numberProperty.setValue(Math.round(10.0 * result) / 10.0);
        } else if (numberProperty.getValue() instanceof Float) {
            float result = (Float)numberProperty.getMinimum() + (difference * percent);
            numberProperty.setValue(Math.round(10.0f * result) / 10.0f);
        } else if (numberProperty.getValue() instanceof Integer) {
            numberProperty.setValue(((Integer)numberProperty.getMinimum() + (int)(difference * percent)));
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }

    private void dragSetting(int mouseX, int mouseY) {
        if(isHovering(mouseX, mouseY) && Mouse.isButtonDown(0)) {
            setSettingFromX(mouseX);
        }
    }

    private boolean isHovering(int mouseX, int mouseY) {
        for (Panel panel : ClickGui.getClickGui().getPanels()) {
            if (!panel.drag) continue;
            return false;
        }
        return (float)mouseX >= this.getX() && (float)mouseX <= this.getX() + (float)this.getWidth() && (float)mouseY >= this.getY() && (float)mouseY <= this.getY() + (float)this.height;
    }

//    private float middle() {
//        return max.floatValue() - min.floatValue();
//    }
//
//    private float part() {
//        return ((Number) numberProperty.getValue()).floatValue() - min.floatValue();
//    }
//
//    private float partialMultiplier() {
//        return part() / middle();
//    }
}

