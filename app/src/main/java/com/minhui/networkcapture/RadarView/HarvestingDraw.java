package com.minhui.networkcapture.RadarView;

import android.graphics.*;
import android.view.View;
import com.minhui.vpn.Handlers.HandlerItem.Harvestable;
import com.minhui.vpn.Handlers.MainHandler;
import java.util.ArrayList;

public class HarvestingDraw {
    Paint pPill = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
    float[] tempPos = new float[2];

    public void init(View view) {
        pPill.setColor(Color.parseColor("#CC0D1117"));
        pText.setColor(Color.WHITE);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTextSize(22f);
    }

    public void setTextSize(int size) { pText.setTextSize(size); }

    public void draw(Canvas canvas, float lpX, float lpY, Matrix transformationMatrix, BitmapCache bitmapCache) {
        ArrayList<Harvestable> list = MainHandler.getInstance().harvestablesHandler.getHarvestableList();
        for (Harvestable h : list) {
            tempPos[0] = h.getPosX() * -1 + lpX;
            tempPos[1] = h.getPosY() - lpY;
            transformationMatrix.mapPoints(tempPos);

            // DRAW PILL LABEL (Guaranteed to show even without icons)
            String label = "T" + h.getTier() + " Resource";
            float tw = pText.measureText(label);
            canvas.drawRoundRect(new RectF(tempPos[0]-tw/2-10, tempPos[1]+20, tempPos[0]+tw/2+10, tempPos[1]+55), 10, 10, pPill);
            canvas.drawText(label, tempPos[0], tempPos[1]+45, pText);
            
            // Draw a small blue dot
            pPill.setColor(Color.parseColor("#58a6ff"));
            canvas.drawCircle(tempPos[0], tempPos[1], 10f, pPill);
            pPill.setColor(Color.parseColor("#CC0D1117"));
        }
    }
}
