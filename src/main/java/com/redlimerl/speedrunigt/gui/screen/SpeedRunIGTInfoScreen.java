package com.redlimerl.speedrunigt.gui.screen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SpeedRunIGTInfoScreen extends Screen {


    public enum UpdateStatus { NONE, UNKNOWN, UPDATED, OUTDATED }
    public static UpdateStatus UPDATE_STATUS = UpdateStatus.NONE;
    static String UPDATE_URL = "";
    static String UPDATE_VERSION = "0.0";

    private final Screen parent;

    private ButtonWidget update;

    public SpeedRunIGTInfoScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        checkUpdate();

        update = addButton(new ButtonWidget(width / 2 - 155, height - 104, 150, 20, new TranslatableText("speedrunigt.menu.download_update").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open(UPDATE_URL)));
        update.active = false;
        addButton(new ButtonWidget(width / 2 + 5, height - 104, 150, 20, new TranslatableText("speedrunigt.menu.latest_change_log").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open("https://github.com/RedLime/SpeedRunIGT/releases/latest")));

        addButton(new ButtonWidget(width / 2 - 155, height - 80, 150, 20, new TranslatableText("speedrunigt.menu.open_github_repo").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open("https://github.com/RedLime/SpeedRunIGT/")));
        addButton(new ButtonWidget(width / 2 + 5, height - 80, 150, 20, new TranslatableText("speedrunigt.menu.open_support_page").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open("https://ko-fi.com/redlimerl")));
        addButton(new ButtonWidget(width / 2 - 100, height - 40, 200, 20, ScreenTexts.BACK, (ButtonWidget button) -> onClose()));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.5f, 1.5f, 1.5f);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 3, 15, 16777215);
        RenderSystem.popMatrix();
        this.drawCenteredString(this.font, new LiteralText("Made by RedLime").asFormattedString(), this.width / 2, 50, 16777215);
        this.drawCenteredString(this.font, new LiteralText("Discord : RedLime#0817").asFormattedString(), this.width / 2, 62, 16777215);
        this.drawCenteredString(this.font,
                new LiteralText("Version : "+ SpeedRunIGT.MOD_VERSION.split("\\+")[0]).asFormattedString(), this.width / 2, 78, 16777215);
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                update.active = true;
                this.drawCenteredString(this.font, new LiteralText("Updated Version : "+ UPDATE_VERSION).formatted(Formatting.YELLOW).asFormattedString(), this.width / 2, 88, 16777215);
            }
            this.drawCenteredString(this.font,
                    new TranslatableText("speedrunigt.message.update."+UPDATE_STATUS.name().toLowerCase(Locale.ROOT)).asFormattedString(),
                    this.width / 2, 116, 16777215);
        }

        super.render(mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.openScreen(parent);
        }
    }



    public static void checkUpdate() {
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            return;
        }
        new Thread(() -> {
            try {
                URL u = new URL("https://api.github.com/repos/RedLime/SpeedRunIGT/releases");
                HttpURLConnection c = (HttpURLConnection) u.openConnection();

                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);

                InputStreamReader r = new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8);
                JsonElement jsonElement = new JsonParser().parse(r);
                if (jsonElement.getAsJsonArray().size() == 0) {
                    UPDATE_STATUS = UpdateStatus.UNKNOWN;
                } else {
                    for (JsonElement element : jsonElement.getAsJsonArray()) {
                        JsonObject versionData = element.getAsJsonObject();
                        if (!versionData.get("prerelease").getAsBoolean()) {
                            for (JsonElement asset : versionData.get("assets").getAsJsonArray()) {
                                JsonObject assetData = asset.getAsJsonObject();
                                String versionName = assetData.get("name").getAsString();
                                String targetVersionName = versionName.split("\\+")[0].split("-")[1];
                                String currentVersionName = SpeedRunIGT.MOD_VERSION.split("\\+")[0];
                                String currentMCVersionName = SpeedRunIGT.MOD_VERSION.split("\\+")[1];
                                if (versionName.endsWith(currentMCVersionName + ".jar") &&
                                        compareVersion(targetVersionName, currentVersionName) > 0 && compareVersion(targetVersionName, UPDATE_VERSION) > 0) {
                                    UPDATE_STATUS = UpdateStatus.OUTDATED;
                                    UPDATE_URL = assetData.get("browser_download_url").getAsString();
                                    UPDATE_VERSION = assetData.get("name").getAsString().split("\\+")[0].split("-")[1];
                                }
                            }
                        }
                    }

                    if (UPDATE_STATUS == UpdateStatus.NONE) {
                        UPDATE_STATUS = UpdateStatus.UPDATED;
                    }
                }
            } catch (IOException e) {
                UPDATE_STATUS = UpdateStatus.UNKNOWN;
            }
        }).start();
    }

    public static int compareVersion(String left, String right) {
        if (left.equals(right)) {
            return 0;
        }
        int leftStart = 0, rightStart = 0, result;
        do {
            int leftEnd = left.indexOf('.', leftStart);
            int rightEnd = right.indexOf('.', rightStart);
            Integer leftValue = Integer.parseInt(leftEnd < 0
                    ? left.substring(leftStart)
                    : left.substring(leftStart, leftEnd));
            Integer rightValue = Integer.parseInt(rightEnd < 0
                    ? right.substring(rightStart)
                    : right.substring(rightStart, rightEnd));
            result = leftValue.compareTo(rightValue);
            leftStart = leftEnd + 1;
            rightStart = rightEnd + 1;
        } while (result == 0 && leftStart > 0 && rightStart > 0);
        if (result == 0) {
            if (leftStart > rightStart) {
                return containsNonZeroValue(left, leftStart) ? 1 : 0;
            }
            if (leftStart < rightStart) {
                return containsNonZeroValue(right, rightStart) ? -1 : 0;
            }
        }
        return result;
    }
    private static boolean containsNonZeroValue(String str, int beginIndex) {
        for (int i = beginIndex; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c != '0' && c != '.') {
                return true;
            }
        }
        return false;
    }
}
