package com.pennywise.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import androidx.core.content.ContextCompat;
import com.pennywise.R;

import com.pennywise.model.Money;

public final class Anim {

    private Anim() {}

    // Fade in while sliding up a little. "index" staggers rows one after another.
    public static void fadeInUp(View v, int index) {
        v.setAlpha(0f);
        v.setTranslationY(dp(v, 16));
        v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * 60L)
                .setDuration(350)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    // Progress bar fills smoothly from 0 to the target
    public static void fillProgress(ProgressBar bar, int target) {
        ObjectAnimator a = ObjectAnimator.ofInt(bar, "progress", 0, target);
        a.setDuration(700);
        a.setInterpolator(new DecelerateInterpolator());
        a.start();
    }

    // A money amount counts up to its value, e.g. $0.00 -> $2,720.00
    public static void countUp(TextView text, long cents) {
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(700);
        a.setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(anim -> {
            float f = (float) anim.getAnimatedValue();
            text.setText(Money.format(Math.round(cents * f)));
        });
        a.start();
    }

    // Button shrinks a little while pressed, then springs back
    @SuppressLint("ClickableViewAccessibility")
    public static void pressEffect(View v) {
        v.setOnTouchListener((view, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(90).start();
            } else if (e.getAction() == MotionEvent.ACTION_UP
                    || e.getAction() == MotionEvent.ACTION_CANCEL) {
                view.animate().scaleX(1f).scaleY(1f).setDuration(140).start();
            }
            return false; // let the normal click still happen
        });
    }

    private static float dp(View v, int value) {
        return value * v.getResources().getDisplayMetrics().density;
    }

    // "+$5.00" floats up from a view and fades out
    public static void floatText(Activity activity, View anchor, String text) {
        ViewGroup root = activity.findViewById(android.R.id.content);
        TextView t = new TextView(activity);
        t.setText(text);
        t.setTextSize(16);
        t.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.inter_semibold));
        t.setTextColor(ContextCompat.getColor(activity, R.color.tag_green));
        t.setGravity(Gravity.CENTER);

        int[] anchorPos = new int[2];
        int[] rootPos = new int[2];
        anchor.getLocationInWindow(anchorPos);
        root.getLocationInWindow(rootPos);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root.addView(t, lp);

        t.post(() -> {
            t.setX(anchorPos[0] - rootPos[0] + anchor.getWidth() / 2f - t.getWidth() / 2f);
            t.setY(anchorPos[1] - rootPos[1] - t.getHeight() / 2f);
            t.animate()
                    .translationYBy(-dp(t, 60))
                    .alpha(0f)
                    .setDuration(900)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> root.removeView(t))
                    .start();
        });
    }

    // Bounce bigger and back
    public static void pop(View v) {
        v.animate().cancel();
        v.setScaleX(1f);
        v.setScaleY(1f);
        v.animate()
                .scaleX(1.35f).scaleY(1.35f)
                .setDuration(140)
                .withEndAction(() -> v.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(260)
                        .setInterpolator(new OvershootInterpolator(3f))
                        .start())
                .start();
    }

    // Confetti burst over the whole screen
    public static void confetti(Activity activity) {
        ViewGroup root = activity.findViewById(android.R.id.content);
        new ConfettiView(activity).burst(root);
    }

}