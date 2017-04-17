package sergeylysyi.scrollpalette;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ScrollPalette extends Activity {

    public static final int REQUEST_PALETTE = 0;
    public static final float HUE_OFFSET_DIVIDER = 2f;
    public static final float SATURATION_OFFSET_DIVIDER = 500f;
    public static final int LENGTH_OF_VIBRATION_ON_BOUND = 50;

    private LinearLayout linLay;
    private SwitchingScrollView sv;
    private CurrentColor currentColor = null;
    private CurrentColor dynamicColor = null;
    private List<ColorButton> buttons = new ArrayList<>();
    private boolean colorEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pallete);

        findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra("color", currentColor.getColor());
                setResult(RESULT_OK, result);
                finish();
            }
        });

        currentColor = new CurrentColor(0);
        dynamicColor = new CurrentColor(0);

        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        sv = (SwitchingScrollView) findViewById(R.id.horizontalScrollView);
        final ViewTreeObserver vto = sv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setButtonsLayout();
                setButtonsColor();
                setButtonsBounds();
            }
        });

        linLay = (LinearLayout) findViewById(R.id.linearLayout);

        ImageView imageOfCurrentColor = (ImageView) findViewById(R.id.currentColor);
        ImageView imageOfDynamicColor = (ImageView) findViewById(R.id.dynamicColor);
        TextView textRGB = (TextView) findViewById(R.id.textViewRGB);
        TextView textHSV = (TextView) findViewById(R.id.textViewHSV);

        currentColor.addViewForBackgroundChange(imageOfCurrentColor);
        dynamicColor.addViewForBackgroundChange(imageOfDynamicColor);

        currentColor.addTextViewForTextChange(
                textRGB,
                "Red:%2h\nGreen:%2h\nBlue:%2h",
                CurrentColor.Palette.RGB);

        currentColor.addTextViewForTextChange(
                textHSV,
                "Hue:%3.2f\nSaturation:%3.2f\nValue:%3.2f",
                CurrentColor.Palette.HSV);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        for (int i = 0; i < linLay.getChildCount(); i++) {
            final ColorButton b = (ColorButton) linLay.getChildAt(i);
            b.setLayoutParams(params);

            final GestureDetector gestureDetector = new GestureDetector(
                    this,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e) {
                            currentColor.change(b.getColor());
                            return true;
                        }

                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            b.resetColor();
                            return true;
                        }

                        @Override
                        public void onLongPress(MotionEvent e) {
                            colorEditMode = true;
                            sv.disallowScroll();
                            vibrator.vibrate(LENGTH_OF_VIBRATION_ON_BOUND);
                        }
                    }
            );
            b.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent e) {
                    if (colorEditMode) {
                        if (e.getAction() == MotionEvent.ACTION_MOVE) {
                            MotionEvent.PointerCoords pointerOffset = new MotionEvent.PointerCoords();
                            e.getPointerCoords(0, pointerOffset);
                            float[] hsvOffset = new float[3];
                            hsvOffset[0] = pointerOffset.x / HUE_OFFSET_DIVIDER;
                            hsvOffset[2] = -pointerOffset.y / SATURATION_OFFSET_DIVIDER;
                            b.offsetHSVColor(hsvOffset);
                            dynamicColor.change(b.getDynamicColor());
                            return true;
                        } else if (e.getAction() == MotionEvent.ACTION_UP) {
                            b.fixCurrentColor();
                            dynamicColor.change(0);
                            colorEditMode = false;
                            sv.allowScroll();
                            return true;
                        }
                        return true;
                    }
                    return gestureDetector.onTouchEvent(e);
                }
            });

            ColorButton.ColorBoundListener colorBoundListener = new ColorButton.ColorBoundListener() {
                @Override
                public void onColorBoundReached() {
                    vibrator.vibrate(2);
                }
            };
            b.addBoundListener(colorBoundListener, 0);
            b.addBoundListener(colorBoundListener, 2);
            buttons.add(b);
        }
    }

    protected void setButtonsColor() {
        Bitmap bm = Bitmap.createBitmap(
                linLay.getWidth(),
                linLay.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);

        linLay.draw(c);

        for (ColorButton b : buttons) {
            Rect offsetViewBounds = new Rect();
            b.getHitRect(offsetViewBounds);

            b.setCoreColor(bm.getPixel(offsetViewBounds.centerX(), 0));
        }
    }

    protected void setButtonsBounds() {
        float[] hsv = new float[3];
        Iterator<ColorButton> iter = buttons.iterator();
        ColorButton prevButton = iter.next();
        while (iter.hasNext()) {
            ColorButton currentButton = iter.next();
            Color.colorToHSV(currentButton.getColor(), hsv);
            prevButton.setHueMaxValue(hsv[0]);
            Color.colorToHSV(prevButton.getColor(), hsv);
            currentButton.setHueMinValue(hsv[0]);
            prevButton = currentButton;
        }
    }

    private void setButtonsLayout() {
        Button anyButton = buttons.get(0);

        int sideSize = Math.max(anyButton.getWidth(), anyButton.getHeight());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sideSize / 2, sideSize / 2);

        int margin_width = sideSize / 8;
        int margin_height = sideSize / 4;

        params.setMargins(margin_width, margin_height, margin_width, margin_height);

        for (Button b : buttons) {
            b.setLayoutParams(params);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("current_color", currentColor.getColor());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int color = savedInstanceState.getInt("current_color");
        currentColor.change(color);
    }

}
