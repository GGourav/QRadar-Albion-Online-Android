package com.minhui.networkcapture.RadarView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.minhui.networkcapture.R;
import com.minhui.vpn.Handlers.MainHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class RadarView extends View {
    Paint paintCenterObject;
    Matrix transformationMatrix;
    public DrawPlayers drawPlayers = new DrawPlayers();
    public DrawMobs drawMobs = new DrawMobs();
    public HarvestingDraw harvestingDraw = new HarvestingDraw();
    public DrawChests drawChests = new DrawChests();
    public FishingZoneDraw fishingZoneDraw = new FishingZoneDraw();

    Paint borderPaint;
    BitmapCache bitmapCache = new BitmapCache();

    @Subscribe
    public void onMessage(String event) {
        invalidate();
    }

    public void init() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        
        paintCenterObject = new Paint(Paint.ANTI_ALIAS_FLAG);
        drawPlayers.init(this);
        drawMobs.init(this);
        harvestingDraw.init(this);
        fishingZoneDraw.init(this);
        drawChests.init(this);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(5f);
        borderPaint.setColor(Color.BLUE);

        // FIX: Set center dot to RED for high visibility
        paintCenterObject.setColor(Color.RED);
        paintCenterObject.setStyle(Paint.Style.FILL);

        this.post(this::initMatrix);
    }

    public void setBorderSize(int size) {
        borderPaint.setStrokeWidth(size);
    }

    public void initMatrix() {
        transformationMatrix = new Matrix();
        float radarCenterX = getWidth() / 2f;
        float radarCenterY = getHeight() / 2f;

        transformationMatrix.postTranslate(radarCenterX, radarCenterY);
        // Rotate 225 degrees to align with Albion's isometric map view
        transformationMatrix.postRotate(225, radarCenterX, radarCenterY);
        
        float scale = RadarSettings.getInstance().radarScaleBar;
        if (scale <= 0) scale = 2.3f; // Default scale safeguard
        transformationMatrix.postScale(scale, scale, radarCenterX, radarCenterY);
    }

    public RadarView(Context context) { super(context); init(); }
    public RadarView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (transformationMatrix == null) {
            return;
        }

        // Get local player position from the Network Handlers
        float lpX = MainHandler.getInstance().playersHandler.localPlayerPosX();
        float lpY = MainHandler.getInstance().playersHandler.localPlayerPosY();

        // 1. Draw your character (The Center Red Dot)
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, 15f, paintCenterObject);

        // 2. Draw Resources and Items
        harvestingDraw.draw(canvas, lpX, lpY, transformationMatrix, bitmapCache);
        fishingZoneDraw.draw(canvas, lpX, lpY, transformationMatrix, bitmapCache);
        
        // 3. Draw Mobs and Chests
        drawMobs.draw(canvas, lpX, lpY, transformationMatrix, bitmapCache);
        drawChests.draw(canvas, lpX, lpY, transformationMatrix, bitmapCache);
        
        // 4. Draw Players
        drawPlayers.draw(canvas, lpX, lpY, transformationMatrix);

        // FIX: Force a 30FPS refresh limit to prevent screen freezing/lag on iQOO
        postInvalidateDelayed(33);
    }
}
