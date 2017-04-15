package sergeylysyi.scrollpalette;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class ScrollPalette extends Activity {

    private LinearLayout linLay;
    private HorizontalScrollView sv;
    private CurrentColor currentColor = null;
    private List<Button> buttons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pallete);

        currentColor = new CurrentColor(0);

        sv = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);

        final ViewTreeObserver vto = sv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setButtonsLayout();
                setButtonsColor();
            }
        });

        linLay = (LinearLayout) findViewById(R.id.linearLayout);

        ImageView imageOfCurrentColor = (ImageView) findViewById(R.id.currentColor);
        TextView textRGB = (TextView) findViewById(R.id.textViewRGB);
        TextView textHSV = (TextView) findViewById(R.id.textViewHSV);

        currentColor.addViewForBackgroundChange(imageOfCurrentColor);

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
            Button b = (Button) linLay.getChildAt(i);
            b.setLayoutParams(params);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bitmap bm = Bitmap.createBitmap(
                            v.getWidth(),
                            v.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(bm);
                    v.draw(c);

                    int color = bm.getPixel(bm.getWidth() / 2, bm.getHeight() / 2);
                    ScrollPalette.this.currentColor.change(color);
                }
            });
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

        for (Button b : buttons) {
            Rect offsetViewBounds = new Rect();
            b.getHitRect(offsetViewBounds);

            b.getBackground().setColorFilter(
                    bm.getPixel(offsetViewBounds.centerX(), 0),
                    PorterDuff.Mode.SRC);
        }
    }

    private void setButtonsLayout(){
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
        outState.putInt("currentColor", currentColor.getColor());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int color = savedInstanceState.getInt("currentColor");
        currentColor.change(color);
    }

    private static class CurrentColor {
        enum Palette {RGB, HSV}

        private int color;
        private Set<View> viewsForChange = new HashSet<>();
        private Map<TextView, StringPalettePair> textViewsForChange = new HashMap<>();

        CurrentColor(int color) {
            this.color = color;
        }

        int getColor() {
            return color;
        }

        void change(int newColor) {
            color = newColor;
            for (View v : viewsForChange) {
                applyToView(v);
            }
            for (TextView tv : textViewsForChange.keySet()) {
                applyToTextView(tv);
            }
        }

        private void applyToView(View v) {
            v.setBackgroundColor(color);
            v.invalidate();
        }

        private void applyToTextView(TextView tv) {
            if (textViewsForChange.get(tv).palette == Palette.RGB) {
                setTextRGB(tv);
            } else {
                setTextHSV(tv);
            }
        }

        private void setTextRGB(TextView tv) {
            tv.setText(String.format(
                    Locale.getDefault(),
                    textViewsForChange.get(tv).formattedString,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)));
            tv.invalidate();
        }

        private void setTextHSV(TextView tv) {
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            tv.setText(String.format(
                    Locale.getDefault(),
                    textViewsForChange.get(tv).formattedString,
                    hsv[0],
                    hsv[1],
                    hsv[2]));
            tv.invalidate();
        }

        void addViewForBackgroundChange(View view) {
            viewsForChange.add(view);
            applyToView(view);
        }

        void addTextViewForTextChange(TextView textView, String formattedString, Palette palette) {
            textViewsForChange.put(textView, new StringPalettePair(formattedString, palette));
            applyToTextView(textView);
        }

        static private class StringPalettePair {
            final String formattedString;
            final Palette palette;

            StringPalettePair(String formattedString, Palette palette) {
                this.formattedString = formattedString;
                this.palette = palette;
            }
        }
    }
}