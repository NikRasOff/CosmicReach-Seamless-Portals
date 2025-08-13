package com.nikrasoff.seamlessportals.ui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.ui.widgets.ItemStackWidget;

public class FakeItemStackWidget extends ItemStackWidget {

    public static Vector2 tmpVec = new Vector2();

    public FakeItemStackWidget(Drawable imageDrawable) {
        super(imageDrawable);
    }

    public void drawItem(Viewport itemViewport, Item fakeItem, boolean fakeItemVisible) {
        ItemStack itemStack = this.itemStack;
        Item drawnItem = null;
        if (fakeItemVisible){
            drawnItem = fakeItem;
        }
        if (itemStack != null){
            drawnItem = itemStack.getItem();
        }
        if (drawnItem != null) {
            Viewport viewport = this.getStage().getViewport();
            this.localToAscendantCoordinates((Actor)null, tmpVec.set(0.0F, 0.0F));
            viewport.project(tmpVec);
            float sx = tmpVec.x;
            float sy = tmpVec.y;
            this.localToAscendantCoordinates((Actor)null, tmpVec.set(this.getWidth(), this.getHeight()));
            viewport.project(tmpVec);
            float sw = tmpVec.x - sx;
            float sh = tmpVec.y - sy;
            if (this.expandOnHover && this.isHovered && !Gdx.input.isCursorCatched()) {
                sx -= 4.0F;
                sy -= 4.0F;
                sw += 8.0F;
                sh += 8.0F;
            }

            itemViewport.setScreenBounds((int)sx + 1, (int)sy + 1, (int)sw, (int)sh);
            itemViewport.apply();
            Camera itemCam = ItemRenderer.getItemSlotCamera(drawnItem);
            itemViewport.setCamera(itemCam);
            itemViewport.apply();
            ItemRenderer.drawItem(itemCam, drawnItem);
        }
    }
}
