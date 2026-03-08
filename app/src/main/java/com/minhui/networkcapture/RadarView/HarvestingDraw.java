package com.minhui.networkcapture.RadarView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.minhui.vpn.Handlers.HandlerItem.Harvestable;
import com.minhui.vpn.Handlers.HandlerItem.HarvestableType;
import com.minhui.vpn.Handlers.MainHandler;
import java.util.ArrayList;

public class HarvestingDraw {
    View view;
    float[] tempPos = new float[2];
    Paint paintPill = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void init(View view) {
        this.view = view;
        paintPill.setColor(Color.parseColor("#CC0D1117"));
        paintText.setColor(Color.WHITE);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setFakeBoldText(true);
    }

    // Fixed: Added back the method the app expects
    public void setTextSize(int size) {
        paintText.setTextSize(size);
    }

    public void draw(Canvas canvas, float lpX, float lpY, Matrix transformationMatrix, BitmapCache bitmapCache) {
        ArrayList<Harvestable> harvestables = MainHandler.getInstance().harvestablesHandler.getHarvestableList();
        int desiredWidth = RadarSettings.getInstance().harvestingWidthHeightBar;
        int desiredHeight = RadarSettings.getInstance().harvestingWidthHeightBar;

        for (Harvestable h : harvestables) {
            if (h.getCharges() <= 0) continue;
            
            tempPos[0] = h.getPosX() * -1 + lpX;
            tempPos[1] = h.getPosY() - lpY;
            transformationMatrix.mapPoints(tempPos);

            String typeName = "";
            try { typeName = HarvestableType.values()[h.getType()].name().toLowerCase(); } catch (Exception e) {}
            String imgName = typeName + "_" + h.getTier() + "_" + h.getEnchant();

            // Try to load the icon
            Bitmap bitmap = bitmapCache.getBitmapFromMemCache(imgName);
            if (bitmap == null) {
                try {
                    int resId = view.getResources().getIdentifier(imgName, "drawable", view.getContext().getPackageName());
                    if (resId != 0) {
                        Drawable d = ContextCompat.getDrawable(view.getContext(), resId);
                        if (d instanceof BitmapDrawable) {
                            bitmap = ((BitmapDrawable) d).getBitmap();
                            bitmapCache.addBitmapToMemoryCache(imgName, bitmap);
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (bitmap != null) {
                // ICON FOUND: Draw original QRadar style
                bitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, false);
                canvas.drawBitmap(bitmap, tempPos[0] - desiredWidth/2, tempPos[1] - desiredHeight/2, null);
            } else {
                // ICON MISSING: Draw professional Pill Label (Fallback)
                String label = "T" + h.getTier() + " " + typeName;
                float tw = paintText.measureText(label);
                canvas.drawRoundRect(new RectF(tempPos[0] - tw/2 - 8, tempPos[1] + 20, tempPos[0] + tw/2 + 8, tempPos[1] + 50), 10, 10, paintPill);
                canvas.drawText(label, tempPos[0], tempPos[1] + 42, paintText);
            }
        }
    }
}
