package com.nikrasoff.seamlessportals.ui.widgets;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.items.*;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.ui.widgets.ContainerSlotWidget;
import finalforeach.cosmicreach.ui.widgets.ItemStackWidget;

import java.util.function.Supplier;

public class FakeItemSlotWidget extends ContainerSlotWidget {
    public Item fakeItem;
    public boolean fakeItemVisible = false;
    public static Vector2 tmpVec = new Vector2();

    public FakeItemSlotWidget(int windowId, ISlotContainerParent containerParent, Supplier<ISlotContainer> getSlotContainer, int slotId) {
        super(windowId, containerParent, getSlotContainer, slotId);
        this.itemStackWidget = new FakeItemStackWidget(imageDrawable);
        this.itemStackWidget.addAction(new Action() {
            public boolean act(float delta) {
                ItemSlot itemSlot = FakeItemSlotWidget.this.getItemSlot();
                FakeItemSlotWidget.this.itemStackWidget.itemStack = itemSlot == null ? null : itemSlot.getItemStack();
                return false;
            }
        });
    }

    public FakeItemSlotWidget(int windowId, ISlotContainerParent containerParent, Supplier<ISlotContainer> getSlotContainer, int slotId, boolean isOutput) {
        super(windowId, containerParent, getSlotContainer, slotId, isOutput);
        this.itemStackWidget = new FakeItemStackWidget(imageDrawable);
        this.itemStackWidget.addAction(new Action() {
            public boolean act(float delta) {
                ItemSlot itemSlot = FakeItemSlotWidget.this.getItemSlot();
                FakeItemSlotWidget.this.itemStackWidget.itemStack = itemSlot == null ? null : itemSlot.getItemStack();
                return false;
            }
        });
    }

    public FakeItemSlotWidget(int windowId, ISlotContainerParent containerParent, Supplier<ISlotContainer> getSlotContainer, int slotId, Drawable imageDrawable, Drawable imageHoveredDrawable, Drawable imageSelectedDrawable) {
        super(windowId, containerParent, getSlotContainer, slotId, imageDrawable, imageHoveredDrawable, imageSelectedDrawable);
        this.itemStackWidget = new FakeItemStackWidget(imageDrawable);
        this.itemStackWidget.addAction(new Action() {
            public boolean act(float delta) {
                ItemSlot itemSlot = FakeItemSlotWidget.this.getItemSlot();
                FakeItemSlotWidget.this.itemStackWidget.itemStack = itemSlot == null ? null : itemSlot.getItemStack();
                return false;
            }
        });
    }

    public void setFakeItem(Item fakeItem){
        this.fakeItem = fakeItem;
    }

    @Override
    public void drawItem(Viewport itemViewport) {
        ((FakeItemStackWidget) this.itemStackWidget).drawItem(itemViewport, fakeItem, fakeItemVisible);
    }
}
