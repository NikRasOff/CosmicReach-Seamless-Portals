package com.nikrasoff.seamlessportals.ui.widgets;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.items.*;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.ui.widgets.ItemSlotWidget;

public class FakeItemSlotWidget extends ItemSlotWidget {
    public Item fakeItem;
    public boolean fakeItemVisible = false;
    public static Vector2 tmpVec = new Vector2();

    public FakeItemSlotWidget(ISlotContainerParent parent, ISlotContainer container, int itemSlotId) {
        super(parent, container, itemSlotId);
    }

    public FakeItemSlotWidget(ISlotContainerParent parent, ISlotContainer container, int itemSlotId, boolean isOutput) {
        super(parent, container, itemSlotId, isOutput);
    }

    public FakeItemSlotWidget(ISlotContainerParent parent, ISlotContainer container, int itemSlotId, Drawable imageDrawable, Drawable imageHoveredDrawable, Drawable imageSelectedDrawable) {
        super(parent, container, itemSlotId, imageDrawable, imageHoveredDrawable, imageSelectedDrawable);
    }

    public void setFakeItem(Item fakeItem){
        this.fakeItem = fakeItem;
    }

    @Override
    public void drawItem(Viewport itemViewport) {
        ItemStack itemStack = this.getItemSlot().itemStack;
        Item drawnItem = null;
        if (this.fakeItemVisible){
            drawnItem = this.fakeItem;
        }
        if (itemStack != null){
            drawnItem = itemStack.getItem();
        }
        if (drawnItem != null) {
            Viewport viewport = this.getStage().getViewport();
            this.slotImage.localToAscendantCoordinates(null, tmpVec.set(0.0F, 0.0F));
            viewport.project(tmpVec);
            float sx = tmpVec.x;
            float sy = tmpVec.y;
            this.slotImage.localToAscendantCoordinates(null, tmpVec.set(this.slotImage.getWidth(), this.slotImage.getHeight()));
            viewport.project(tmpVec);
            float sw = tmpVec.x - sx;
            float sh = tmpVec.y - sy;
            itemViewport.setScreenBounds((int)sx + 1, (int)sy + 1, (int)sw, (int)sh);
            itemViewport.apply();
            Camera itemCam = ItemRenderer.getItemSlotCamera(drawnItem);
            itemViewport.setCamera(itemCam);
            itemViewport.apply();
            ItemRenderer.drawItem(itemCam, drawnItem);
        }
    }
}
