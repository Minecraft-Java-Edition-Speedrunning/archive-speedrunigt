package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDisplayAlign;
import com.redlimerl.speedrunigt.timer.TimerDrawer.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class TimerElement {
    private static final Minecraft client = Minecraft.getMinecraft();

    private final TextRenderer textRenderer = client.textRenderer;
    private final Position position = new Position(0, 0);
    private final Position scaledPosition = new Position(0, 0);
    private float scale = 1;
    private int textWidth = 0;
    private String text;
    private Integer color;
    private TimerDecoration decoration;
    private float fontHeight = 8;
    private final Window window = new Window(client.options, client.width, client.height);

    public void init(float xPos, float yPos, float scale, String text, Integer color, TimerDecoration decoration, TimerDisplayAlign displayAlign, float fontHeight) {
        this.scale = scale;
        this.text = text;
        this.color = color;
        this.decoration = decoration;
        this.fontHeight = fontHeight;
        double scaledWindowWidth = window.getScaledWidth();
        double scaledWindowHeight = window.getScaledHeight();

        int translateX = (int) (xPos * scaledWindowWidth);
        int translateY = (int) (yPos * scaledWindowHeight);

        this.position.setX(translateX);
        this.position.setY(translateY);
        this.scaledPosition.setX(Math.round(translateX / this.scale));
        this.scaledPosition.setY(Math.round(translateY / this.scale));

        this.textWidth = this.textRenderer.getStringWidth(text);

        if (displayAlign != TimerDisplayAlign.LEFT) {
            if (displayAlign == TimerDisplayAlign.RIGHT || (displayAlign == TimerDisplayAlign.AUTO && this.getScaledTextWidth() + this.position.getX() > scaledWindowWidth)) {
                this.scaledPosition.setX(this.scaledPosition.getX() - Math.round((this.getScaledTextWidth() - 1) / scale));
                this.position.setX(this.position.getX() - this.getScaledTextWidth());
            }
            if (displayAlign == TimerDisplayAlign.CENTER) {
                this.scaledPosition.setX(this.scaledPosition.getX() - Math.round((this.getScaledTextWidth() - 1) / scale / 2));
                this.position.setX(this.position.getX() - (this.getScaledTextWidth() / 2));
            }
        }

        // Fix vertical height
        if (this.getScaledTextHeight() + this.position.getY() > scaledWindowHeight) {
            this.scaledPosition.setY(this.scaledPosition.getY() - MathHelper.floor(this.getScaledTextHeight() / scale));
            this.position.setY(this.position.getY() - this.getScaledTextWidth());
        }
    }

    public void draw(boolean doTranslate) {
        GL11.glPushMatrix();
        if (doTranslate) GL11.glTranslatef(0, 0, 1);
        GL11.glScalef(scale, scale, 1.0F);
        drawOutLine(this.textRenderer, scaledPosition.getX(), scaledPosition.getY(), text, color, decoration);
        GL11.glPopMatrix();
    }

    private static void drawOutLine(TextRenderer textRenderer, int x, int y, String text, Integer color, TimerDecoration decoration) {
        if (decoration == TimerDecoration.OUTLINE) {
            textRenderer.method_964(text, x + 1, y + 1, 0);
            textRenderer.method_964(text, x + 1, y, 0);
            textRenderer.method_964(text, x + 1, y - 1, 0);
            textRenderer.method_964(text, x, y - 1, 0);
            textRenderer.method_964(text, x, y + 1, 0);
            textRenderer.method_964(text, x - 1, y + 1, 0);
            textRenderer.method_964(text, x - 1, y, 0);
            textRenderer.method_964(text, x - 1, y - 1, 0);
        } else if (decoration == TimerDecoration.SHADOW) {
            textRenderer.method_964(text, x + 1, y + 1, -12566464);
        }
        textRenderer.method_964(text, x, y, color);
    }

    public Position getPosition() {
        return position;
    }

    public int getScaledTextWidth() {
        return Math.round(textWidth * scale);
    }

    public int getScaledTextHeight() {
        return Math.round(fontHeight * scale);
    }
}