package com.example.jewelryar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {

    public static class Sprite {
        public final Bitmap bmp;
        public final float cx, top, width, angle;

        public Sprite(Bitmap bmp, float cx, float top, float width) {
            this(bmp, cx, top, width, 0f);
        }

        public Sprite(Bitmap bmp, float cx, float top, float width, float angle) {
            this.bmp = bmp;
            this.cx = cx;
            this.top = top;
            this.width = width;
            this.angle = angle;
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF dst = new RectF();
    private List<Sprite> sprites = new ArrayList<>();

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSprites(List<Sprite> newSprites) {
        this.sprites = newSprites;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Sprite s : sprites) {
            float h = s.width * s.bmp.getHeight() / (float) s.bmp.getWidth();
            dst.set(s.cx - s.width / 2f, s.top, s.cx + s.width / 2f, s.top + h);
            canvas.save();
            canvas.rotate(s.angle, s.cx, s.top + h / 2f);
            canvas.drawBitmap(s.bmp, null, dst, paint);
            canvas.restore();
        }
    }
}