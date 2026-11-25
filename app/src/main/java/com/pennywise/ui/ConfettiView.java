package com.pennywise.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// A full-screen overlay that draws falling confetti, then removes itself
public class ConfettiView extends View {

    private static class Piece {
        float x, y, vx, vy, rotation, spin, size;
        int color;
    }

    private static final int[] COLORS = {
            Color.parseColor("#FFC8D3"), Color.parseColor("#82A07F"),
            Color.parseColor("#FFE6C0"), Color.parseColor("#E0627A"),
            Color.parseColor("#B8CFB3"), Color.parseColor("#C9A15A")
    };

    private final List<Piece> pieces = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float progress = 0f;

    public ConfettiView(Context context) {
        super(context);
    }

    public void burst(ViewGroup parent) {
        parent.addView(this, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        post(() -> {
            Random r = new Random();
            float d = getResources().getDisplayMetrics().density;
            for (int i = 0; i < 90; i++) {
                Piece p = new Piece();
                p.x = getWidth() / 2f;
                p.y = getHeight() / 3f;
                p.vx = (r.nextFloat() - 0.5f) * 900 * d / 3;   // spread sideways
                p.vy = -(r.nextFloat() * 700 + 300) * d / 3;   // shoot upward
                p.rotation = r.nextFloat() * 360;
                p.spin = (r.nextFloat() - 0.5f) * 720;
                p.size = (r.nextFloat() * 6 + 5) * d;
                p.color = COLORS[r.nextInt(COLORS.length)];
                pieces.add(p);
            }

            ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
            a.setDuration(1600);
            a.addUpdateListener(anim -> {
                progress = (float) anim.getAnimatedValue();
                invalidate(); // redraw
            });
            a.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    parent.removeView(ConfettiView.this);
                }
            });
            a.start();
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float t = progress * 1.6f; // seconds
        float gravity = 1400 * getResources().getDisplayMetrics().density / 3;
        paint.setAlpha((int) (255 * (1 - progress)));

        for (Piece p : pieces) {
            float x = p.x + p.vx * t;
            float y = p.y + p.vy * t + 0.5f * gravity * t * t;
            canvas.save();
            canvas.rotate(p.rotation + p.spin * t, x, y);
            paint.setColor(p.color);
            paint.setAlpha((int) (255 * (1 - progress)));
            canvas.drawRect(x - p.size / 2, y - p.size / 4, x + p.size / 2, y + p.size / 4, paint);
            canvas.restore();
        }
    }
}