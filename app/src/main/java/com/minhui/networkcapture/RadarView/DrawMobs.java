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
import com.minhui.vpn.Handlers.HandlerItem.Mob;
import com.minhui.vpn.Handlers.MainHandler;
import com.minhui.vpn.PhotonPackageParser.enumerations.MobCodes;
import java.util.ArrayList;

public class DrawMobs {
    Paint pPill = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
    float[] tempPos = new float[2];
    View view; // Store the view to access context safely

    public void init(View view) {
        this.view = view;
        pPill.setColor(Color.parseColor("#CC0D1117"));
        pText.setColor(Color.WHITE);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setFakeBoldText(true);
        pText.setTextSize(22f);
    }

    // FIX: Added back the method that SettingsFragment requires
    public void setTextSize(int size) {
        pText.setTextSize(size);
    }

    public void draw(Canvas canvas, float lpX, float lpY, Matrix transformationMatrix, BitmapCache bitmapCache) {
        ArrayList<Mob> mobList = MainHandler.getInstance().mobsHandler.getMobList();
        int iconSize = RadarSettings.getInstance().mobIconWidthHeightBar;

        for (Mob m : mobList) {
            tempPos[0] = m.getPosX() * -1 + lpX;
            tempPos[1] = m.getPosY() - lpY;
            transformationMatrix.mapPoints(tempPos);

            Bitmap bitmap = null;
            String name = m.getName();
            
            if (name != null && view != null) {
                bitmap = bitmapCache.getBitmapFromMemCache(name);
                if (bitmap == null) {
                    try {
                        // FIX: Use view.getContext() instead of canvas.getContext()
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

            if (bitmap != null) {
                bitmap = Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, false);
                canvas.drawBitmap(bitmap, tempPos[0] - iconSize/2, tempPos[1] - iconSize/2, null);
            } else {
                // FALLBACK: Draw label for new mobs (Crystal Spider, Smuggler, etc.)
                String label = (name != null && !name.isEmpty()) ? name : "Mob " + m.getTypeId();
                float tw = pText.measureText(label);
                canvas.drawRoundRect(new RectF(tempPos[0]-tw/2-10, tempPos[1]+20, tempPos[0]+tw/2+10, tempPos[1]+55), 10, 10, pPill);
                canvas.drawText(label, tempPos[0], tempPos[1]+45, pText);
            }
        }
    }
}
