package vazkii.quark.base.client.config.screen;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import vazkii.quark.base.client.config.screen.widgets.ScrollableWidgetList;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractScrollingWidgetScreen extends AbstractQScreen {

	private final List<AbstractWidget> scrollingWidgets = new LinkedList<>();
	private ScrollableWidgetList<?, ?> elementList;

	private Button resetButton;

	private boolean needsScrollUpdate = false;
	private double currentScroll = 0;

	public AbstractScrollingWidgetScreen(Screen parent) {
		super(parent);
	}

	@Override
	protected void init() {
		super.init();

		int pad = 3;
		int bWidth = 121;
		int left = (width - (bWidth + pad) * 3) / 2;
		int vStart = height - 30;

		addRenderableWidget(new Button(left, vStart, bWidth, 20, Component.translatable("quark.gui.config.default"), this::onClickDefault));
		addRenderableWidget(resetButton = new Button(left + bWidth + pad, vStart, bWidth, 20, Component.translatable("quark.gui.config.discard"), this::onClickDiscard));
		addRenderableWidget(new Button(left + (bWidth + pad) * 2, vStart, bWidth, 20, Component.translatable("gui.done"), this::onClickDone));

		elementList = createWidgetList();
		addWidget(elementList);
		refresh();
		needsScrollUpdate = true;
	}

	@Override
	public void tick() {
		super.tick();

		resetButton.active = isDirty();
	}

	public void refresh() {
		for (AbstractWidget widget : scrollingWidgets)
			removeWidget(widget);
		scrollingWidgets.clear();

		elementList.populate(w -> {
			scrollingWidgets.add(w);
			if(w instanceof Button)
				addRenderableWidget(w);
			else addWidget(w);
		});
	}

	@Override
	public void render(@Nonnull PoseStack mstack, int mouseX, int mouseY, float partialTicks) {
		if(needsScrollUpdate) {
			elementList.setScrollAmount(currentScroll);
			needsScrollUpdate = false;
		}

		currentScroll = elementList.getScrollAmount();

		scrollingWidgets.forEach(w -> w.visible = false);

		renderBackground(mstack);
		elementList.render(mstack, mouseX, mouseY, partialTicks);

		List<AbstractWidget> visibleWidgets = new LinkedList<>();
		scrollingWidgets.forEach(w -> {
			if(w.visible)
				visibleWidgets.add(w);
			w.visible = false;
		});

		super.render(mstack, mouseX, mouseY, partialTicks);

		Window main = minecraft.getWindow();
		int res = (int) main.getGuiScale();

		RenderSystem.enableScissor(0, 40 * res, width * res, (height - 80) * res);
		visibleWidgets.forEach(w -> {
			w.visible = true;
			w.render(mstack, mouseX, mouseY, partialTicks);
		});
		RenderSystem.disableScissor();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		return super.mouseClicked(x, y, button);
	}

	protected abstract ScrollableWidgetList<?, ?> createWidgetList();
	protected abstract void onClickDefault(Button b);
	protected abstract void onClickDiscard(Button b);
	protected abstract boolean isDirty();

	protected void onClickDone(Button b) {
		returnToParent(b);
	}

}
