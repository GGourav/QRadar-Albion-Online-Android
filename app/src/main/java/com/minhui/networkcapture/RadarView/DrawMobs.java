package com.minhui.networkcapture.RadarView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.minhui.networkcapture.R;
import com.minhui.vpn.Handlers.HandlerItem.Mob;
import com.minhui.vpn.Handlers.MainHandler;
import com.minhui.vpn.PhotonPackageParser.enumerations.MobCodes;
import java.util.ArrayList;

public class DrawMobs {
    ArrayList<Mob> mobList;
    Paint pCore = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint pPill = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
    float[] tempPos = new float[2];
    View view;

    public void init(View view) {
        this.view = view;
        pPill.setColor(Color.parseColor("#CC0D1117"));
        pText.setColor(Color.WHITE);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setFakeBoldText(true);
    }

    public void setTextSize(int size) {
        pText.setTextSize(size);
    }

    public void draw(Canvas canvas, float lpX, float lpY, Matrix transformationMatrix, BitmapCache bitmapCache) {
        mobList = (MainHandler.getInstance().mobsHandler.getMobList());
        int desiredWidth = RadarSettings.getInstance().mobIconWidthHeightBar;
        int desiredHeight = RadarSettings.getInstance().mobIconWidthHeightBar;

        for (Mob m : mobList) {
            MobCodes type = m.getType();
            
            // Basic Position Calculation
            tempPos[0] = m.getPosX() * -1 + lpX;
            tempPos[1] = m.getPosY() - lpY;
            transformationMatrix.mapPoints(tempPos);
            float x = tempPos[0]; float y = tempPos[1];

            Bitmap bitmap = null;
            String name = m.getName();

            // 1. TRY TO LOAD ICON
            if (name != null) {
                bitmap = bitmapCache.getBitmapFromMemCache(name);
                if (bitmap == null) {
                    try {
                        int resId = view.getResources().getIdentifier(name, "drawable", view.getContext().getPackageName());
                        if (resId != 0) {
                            Drawable d = ContextCompat.getDrawable(view.getContext(), resId);
                            if (d instanceof BitmapDrawable) {
                                bitmap = ((BitmapDrawable) d).getBitmap();
                                bitmapCache.addBitmapToMemoryCache(name, bitmap);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            // 2. DRAW LOGIC
            if (bitmap != null) {
                // ICON EXISTS: Draw original style
                bitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, false);
                canvas.drawBitmap(bitmap, x - (desiredWidth / 2), y - (desiredHeight / 2), null);
            } else {
                // ICON MISSING (New Mobs): Draw Green Dot + Text Fallback
                pCore.setColor(Color.parseColor("#3fb950")); // Default Green
                if (type == MobCodes.Boss) pCore.setColor(Color.parseColor("#ff6b35")); // Orange for Boss
                
                canvas.drawCircle(x, y, 15f, pCore);
                
                String label = (name != null) ? name : "Unknown Mob";
                pText.setTextSize(20f);
                float tw = pText.measureText(label);
                canvas.drawRoundRect(new RectF(x - tw/2 - 6, y + 18, x + tw/2 + 6, y + 44), 8f, 8f, pPill);
                canvas.drawText(label, x, y + 36, pText);
            }
        }
    }
}
