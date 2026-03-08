package com.minhui.networkcapture.RadarView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import com.minhui.vpn.Handlers.HandlerItem.Harvestable;
import com.minhui.vpn.Handlers.HandlerItem.HarvestableType;
import com.minhui.vpn.Handlers.MainHandler;
import java.util.ArrayList;

public class HarvestingDraw {
    View view;
    float[] tempPos = new float[2];
    Paint paintCore = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint paintRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint paintPill = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void init(View view) {
        this.view = view;
        paintRing.setStyle(Paint.Style.STROKE);
        paintRing.setStrokeWidth(5f);
        paintPill.setColor(Color.parseColor("#CC0D1117"));
        paintText.setColor(Color.WHITE);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setFakeBoldText(true);
    }

    // This is the method that was missing and caused the build failure
    public void setTextSize(int size) {
        paintText.setTextSize(size);
    }

    public void draw(Canvas canvas, float lpX, float lpY, Matrix transformationMatrix, BitmapCache bitmapCache) {
        ArrayList<Harvestable> harvestables = MainHandler.getInstance().harvestablesHandler.getHarvestableList();
        
        for (Harvestable h : harvestables) {
            if (h.getCharges() <= 0) continue;
            if (!RadarSettings.getInstance().harvestingTiers[h.getTier() - 1]) continue;
            if (!RadarSettings.getInstance().harvestingEnchants[h.getEnchant()]) continue;

            tempPos[0] = h.getPosX() * -1 + lpX;
            tempPos[1] = h.getPosY() - lpY;
            transformationMatrix.mapPoints(tempPos);

            float drawX = tempPos[0];
            float drawY = tempPos[1];

            float radius = 8f + (h.getTier() * 1.5f);

            if (h.getEnchant() > 0) {
                int ringColor = Color.TRANSPARENT;
                if (h.getEnchant() == 1) ringColor = Color.parseColor("#3FB950");
                else if (h.getEnchant() == 2) ringColor = Color.parseColor("#1F6FEB");
                else if (h.getEnchant() == 3) ringColor = Color.parseColor("#BB8AFF");
                else if (h.getEnchant() == 4) ringColor = Color.parseColor("#E3B341");
                paintRing.setColor(ringColor);
                canvas.drawCircle(drawX, drawY, radius + 6f, paintRing);
            }

            paintCore.setColor(Color.parseColor("#58a6ff"));
            canvas.drawCircle(drawX, drawY, radius, paintCore);

            String typeName = "Res";
            try { typeName = HarvestableType.values()[h.getType()].name(); } catch (Exception e) {}
            String label = "T" + h.getTier() + " " + typeName + (h.getEnchant() > 0 ? "." + h.getEnchant() : "");
            
            float textWidth = paintText.measureText(label);
            canvas.drawRoundRect(new RectF(drawX - textWidth/2 - 8, drawY + 20, drawX + textWidth/2 + 8, drawY + 50), 10f, 10f, paintPill);
            canvas.drawText(label, drawX, drawY + 42, paintText);
        }
    }
}
