package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.class_1803;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.text.TranslatableText;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.Supplier;

public class SpeedRunOptionScreen extends Screen {

    private final Screen parent;
    private final HashMap<String, ArrayList<ButtonWidget>> categorySubButtons = new HashMap<>();
    private final LinkedHashMap<String, ButtonWidget> categorySelectButtons = new LinkedHashMap<>();
    private final HashMap<ButtonWidget, Supplier<String>> tooltips = new HashMap<>();
    private ButtonScrollListWidget buttonListWidget;
    private final ArrayList<ButtonWidget> widgetButtons = new ArrayList<>();
    private String currentSelectCategory = "";

    public SpeedRunOptionScreen(Screen parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();
        categorySubButtons.clear();
        categorySelectButtons.clear();
        tooltips.clear();

        List<OptionButtonFactory> optionButtonFactoryList = SpeedRunOption.getOptionButtonFactories();

        int categoryCount = 0;

        for (OptionButtonFactory factory : optionButtonFactoryList) {
            OptionButtonFactory.Storage builder = factory.create(this).build();
            ButtonWidget button = builder.getButtonWidget();
            if (builder.getTooltip() != null) tooltips.put(button, builder.getTooltip());

            String category = builder.getCategory();
            ArrayList<ButtonWidget> categoryList = categorySubButtons.getOrDefault(category, new ArrayList<>());
            categoryList.add(button);
            categorySubButtons.put(category, categoryList);

            if (!categorySelectButtons.containsKey(category)) {
                ButtonWidget buttonWidget = new ConsumerButtonWidget(width - 110, 30 + (categoryCount++ * 22), 80, 20, new TranslatableText(category).asFormattedString(), (screen, buttonWidget1) -> selectCategory(category));
                categorySelectButtons.put(category, buttonWidget);
                buttons.add(buttonWidget);
            }
        }

        buttons.add(new ConsumerButtonWidget(width - 85, height - 35, 70, 20, ScreenTexts.CANCEL, (screen, button) -> onClose()));

        buttons.add(new ConsumerButtonWidget(width - 160, height - 35, 70, 20, new TranslatableText("speedrunigt.menu.donate").asFormattedString(), (screen, button) -> OperatingUtils.setUrl("https://ko-fi.com/redlimerl")));

        buttonListWidget = new ButtonScrollListWidget();

        categorySelectButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
    }

    public void onClose() {
        if (this.client != null) this.client.openScreen(parent);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick(this);
        }
        super.buttonClicked(button);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        ArrayList<ButtonWidget> widgets = new ArrayList<>(widgetButtons);
        buttons.addAll(widgets);
        super.mouseClicked(mouseX, mouseY, button);
        buttons.removeAll(widgets);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.buttonListWidget.render(mouseX, mouseY, delta);
        super.render(mouseX, mouseY, delta);
        drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.title.options").asFormattedString(), this.width / 2, 10, 16777215);
        drawWithShadow(this.textRenderer, "v"+ SpeedRunIGT.MOD_VERSION, 4, 4, 16777215);

        ArrayList<String> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty()) this.renderTooltip(tooltip, 0, height);
    }

    public ArrayList<String> getToolTip(int mouseX, int mouseY) {
        ArrayList<String> tooltipList = new ArrayList<>();


        int e = buttonListWidget.getEntryAt(mouseX, mouseY);
        if (e > -1) {
            ButtonWidget element = buttonListWidget.method_6697(e).getButtonWidget();
            if (tooltips.containsKey(element)) {
                String text = tooltips.get(element).get();
                tooltipList.addAll(Arrays.asList(text.split("\n")));
                return tooltipList;
            }
        }


        if (SpeedRunIGTInfoScreen.UPDATE_STATUS == SpeedRunIGTInfoScreen.UpdateStatus.OUTDATED) {
            tooltipList.add(new TranslatableText("speedrunigt.message.update_found").asFormattedString());
        }
        return tooltipList;
    }


    public void selectCategory(String key) {
        if (categorySelectButtons.containsKey(key) && categorySubButtons.containsKey(key)) {
            if (categorySelectButtons.containsKey(currentSelectCategory)) categorySelectButtons.get(currentSelectCategory).active = true;
            currentSelectCategory = key;

            categorySelectButtons.get(key).active = false;
            buttonListWidget.replaceButtons(categorySubButtons.get(key));
            buttonListWidget.scroll(0);
        }
    }

    class ButtonScrollListWidget extends EntryListWidget {

        public ButtonScrollListWidget() {
            super(SpeedRunOptionScreen.this.client, SpeedRunOptionScreen.this.width - 140, SpeedRunOptionScreen.this.height, 28, SpeedRunOptionScreen.this.height - 54, 24);
        }

        public void replaceButtons(Collection<ButtonWidget> buttonWidgets) {
            widgetButtons.clear();
            ArrayList<ButtonScrollListEntry> list = new ArrayList<>();
            for (ButtonWidget buttonWidget : buttonWidgets) {
                widgetButtons.add(buttonWidget);
                list.add(new ButtonScrollListEntry(buttonWidget));
            }
            entries.clear();
            entries.addAll(list);
        }

        @Override
        public int getRowWidth() {
            return 150;
        }

        private final ArrayList<ButtonScrollListEntry> entries = new ArrayList<>();

        @Override
        public ButtonScrollListEntry method_6697(int i) {
            return entries.get(i);
        }

        @Override
        protected int getEntryCount() {
            return entries.size();
        }

        @Override
        public void render(int mouseX, int mouseY, float delta) {
            super.render(mouseX, mouseY, delta);

            //Render bg on empty space
            if (SpeedRunOptionScreen.this.client == null) return;
            int emptyWidth = this.width;
            GL11.glDisable(2896);
            GL11.glDisable(2912);
            Tessellator var2 = Tessellator.INSTANCE;
            SpeedRunOptionScreen.this.client.getTextureManager().bindTexture(OPTIONS_BACKGROUND_TEXTURE);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            float var3 = 32.0F;
            var2.method_1405();
            var2.method_1413(4210752);
            var2.method_1399(emptyWidth, SpeedRunOptionScreen.this.height, 0.0D, emptyWidth / var3, ((float)SpeedRunOptionScreen.this.height / var3));
            var2.method_1399(SpeedRunOptionScreen.this.width, SpeedRunOptionScreen.this.height, 0.0D, ((float)SpeedRunOptionScreen.this.width / var3), (float)SpeedRunOptionScreen.this.height / var3);
            var2.method_1399(SpeedRunOptionScreen.this.width, 0.0D, 0.0D, ((float)SpeedRunOptionScreen.this.width / var3), 0);
            var2.method_1399(emptyWidth, 0.0D, 0.0D, emptyWidth / var3, 0);
            var2.method_1396();
        }

        class ButtonScrollListEntry implements class_1803 {
            private final ButtonWidget buttonWidget;

            public ButtonScrollListEntry(ButtonWidget buttonWidget) {
                this.buttonWidget = buttonWidget;
                this.buttonWidget.x = (ButtonScrollListWidget.this.width - this.buttonWidget.getWidth()) / 2;
            }

            public ButtonWidget getButtonWidget() {
                return buttonWidget;
            }

            @Override
            public void method_6700(int index, int x, int y, int rowWidth, int rowHeight, Tessellator tessellator, int mouseX, int mouseY, boolean hovered) {
                buttonWidget.y = y;
                buttonWidget.render(SpeedRunOptionScreen.this.client, mouseX, mouseY);
            }

            @Override
            public boolean method_6699(int i, int j, int k, int l, int m, int n) {
                return false;
            }

            @Override
            public void method_6701(int i, int j, int k, int l, int m, int n) {

            }
        }
    }

}
