package com.neon.clock;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ClockView extends View {

    private final Paint bgPaint      = new Paint();
    private final Paint gridPaint    = new Paint();
    private final Paint glowPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint secPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint datePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcBgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Handler  handler  = new Handler();
    private final Runnable ticker   = new Runnable() {
        @Override public void run() {
            invalidate();
            handler.postDelayed(this, 500);
        }
    };

    // Colors
    private static final int CYAN    = 0xFF00FFFF;
    private static final int MAGENTA = 0xFFFF00FF;
    private static final int GREEN   = 0xFF39FF14;
    private static final int BG      = 0xFF05050F;

    private boolean colonVisible = true;
    private long lastSec = -1;

    public ClockView(Context ctx) {
        super(ctx);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // needed for BlurMaskFilter

        Typeface mono = Typeface.MONOSPACE;

        // background
        bgPaint.setColor(BG);

        // grid
        gridPaint.setColor(0xFF0A0A1E);
        gridPaint.setStrokeWidth(1f);

        // glow behind time digits
        glowPaint.setColor(CYAN);
        glowPaint.setTextAlign(Paint.Align.CENTER);
        glowPaint.setTypeface(mono);
        glowPaint.setFakeBoldText(true);
        glowPaint.setMaskFilter(new BlurMaskFilter(40, BlurMaskFilter.Blur.NORMAL));
        glowPaint.setAlpha(130);

        // sharp time digits
        timePaint.setColor(CYAN);
        timePaint.setTextAlign(Paint.Align.CENTER);
        timePaint.setTypeface(mono);
        timePaint.setFakeBoldText(true);

        // seconds
        secPaint.setColor(GREEN);
        secPaint.setTextAlign(Paint.Align.CENTER);
        secPaint.setTypeface(mono);
        secPaint.setFakeBoldText(true);
        secPaint.setMaskFilter(new BlurMaskFilter(12, BlurMaskFilter.Blur.NORMAL));

        // date
        datePaint.setColor(MAGENTA);
        datePaint.setTextAlign(Paint.Align.CENTER);
        datePaint.setTypeface(mono);
        datePaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

        // horizontal accent line
        linePaint.setStrokeWidth(2f);

        // arc ring background
        arcBgPaint.setStyle(Paint.Style.STROKE);
        arcBgPaint.setStrokeCap(Paint.Cap.ROUND);
        arcBgPaint.setColor(0xFF1A1A2E);

        // arc ring fill
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setMaskFilter(new BlurMaskFilter(6, BlurMaskFilter.Blur.NORMAL));

        // ring labels (H / M / S)
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTypeface(mono);
        labelPaint.setFakeBoldText(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int W = getWidth();
        final int H = getHeight();
        final float cx = W / 2f;
        final float cy = H / 2f;

        Calendar cal = Calendar.getInstance();
        int hour   = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        // Blink colon every second
        long nowSec = second;
        if (nowSec != lastSec) { colonVisible = !colonVisible; lastSec = nowSec; }

        // ── Background ──────────────────────────────────────────────────────
        canvas.drawRect(0, 0, W, H, bgPaint);

        // Grid
        for (int x = 0; x < W; x += 50) canvas.drawLine(x, 0, x, H, gridPaint);
        for (int y = 0; y < H; y += 50) canvas.drawLine(0, y, W, y, gridPaint);

        // ── Scale text to screen ─────────────────────────────────────────────
        float timeSize = W * 0.22f;
        float secSize  = W * 0.10f;
        float dateSize = W * 0.045f;

        glowPaint.setTextSize(timeSize);
        timePaint.setTextSize(timeSize);
        secPaint.setTextSize(secSize);
        datePaint.setTextSize(dateSize);

        // ── Horizontal divider lines ─────────────────────────────────────────
        float lineW = W * 0.7f;
        linePaint.setShader(new LinearGradient(
            cx - lineW / 2, cy, cx + lineW / 2, cy,
            new int[]{0x0000FFFF, CYAN, 0x0000FFFF}, null, Shader.TileMode.CLAMP));
        linePaint.setStrokeWidth(2);
        canvas.drawLine(cx - lineW / 2, cy + timeSize * 0.18f,
                        cx + lineW / 2, cy + timeSize * 0.18f, linePaint);

        linePaint.setShader(new LinearGradient(
            cx - lineW / 2, cy, cx + lineW / 2, cy,
            new int[]{0x00FF00FF, MAGENTA, 0x00FF00FF}, null, Shader.TileMode.CLAMP));
        canvas.drawLine(cx - lineW / 2, cy - timeSize * 0.62f,
                        cx + lineW / 2, cy - timeSize * 0.62f, linePaint);

        // ── Time ─────────────────────────────────────────────────────────────
        String colon = colonVisible ? ":" : " ";
        String timeStr = String.format(Locale.getDefault(), "%02d" + colon + "%02d", hour, minute);

        canvas.drawText(timeStr, cx, cy, glowPaint);
        canvas.drawText(timeStr, cx, cy, timePaint);

        // ── Seconds ───────────────────────────────────────────────────────────
        String secStr = String.format(Locale.getDefault(), "%02d", second);
        canvas.drawText(secStr, cx, cy + secSize * 1.1f, secPaint);

        // ── Date ──────────────────────────────────────────────────────────────
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("ru"));
        String dateStr = sdf.format(new Date()).toUpperCase(new Locale("ru"));
        canvas.drawText(dateStr, cx, cy - timeSize * 0.72f, datePaint);

        // ── Three arc rings (H, M, S) ─────────────────────────────────────────
        drawRings(canvas, cx, H, W, hour % 12, minute, second);
    }

    private void drawRings(Canvas canvas, float cx, int H, int W,
                           int hour12, int minute, int second) {

        float ringStroke = W * 0.015f;
        float baseRadius = W * 0.42f;
        float gap        = W * 0.055f;

        arcBgPaint.setStrokeWidth(ringStroke);
        arcPaint.setStrokeWidth(ringStroke);
        labelPaint.setTextSize(W * 0.035f);

        float topY  = H - W * 0.55f;
        float botY  = H - W * 0.05f;
        float ringCY = (topY + botY) / 2f;

        int[]   colors  = {CYAN, MAGENTA, GREEN};
        float[] sweeps  = {
            (hour12 / 12f)  * 360f,
            (minute / 60f)  * 360f,
            (second / 60f)  * 360f
        };
        String[] labels = {"H", "M", "S"};

        for (int i = 0; i < 3; i++) {
            float r = baseRadius - i * gap;
            RectF oval = new RectF(cx - r, ringCY - r, cx + r, ringCY + r);

            // background arc
            canvas.drawArc(oval, -90, 360, false, arcBgPaint);

            // fill arc
            arcPaint.setColor(colors[i]);
            arcPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));
            canvas.drawArc(oval, -90, sweeps[i], false, arcPaint);

            // label at bottom of each ring
            labelPaint.setColor(colors[i]);
            canvas.drawText(labels[i], cx, ringCY + r + W * 0.04f, labelPaint);
        }
    }
}
