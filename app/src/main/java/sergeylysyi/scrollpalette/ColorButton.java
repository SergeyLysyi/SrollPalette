package sergeylysyi.scrollpalette;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.Button;

import java.util.*;

public class ColorButton extends Button {
    static private final int HSV_ARRAY_SIZE = 3;
    static private final float HUE_MAX_VALUE = 359.7f;
    static private final float EPSILON = 0.1f;
    private int coreColor;
    private List<LinkedList<ColorBoundListener>> boundListeners = new ArrayList<>(HSV_ARRAY_SIZE);
    private float[] fixedHSV = new float[HSV_ARRAY_SIZE];
    private float[] dynamicHSV = new float[HSV_ARRAY_SIZE];

    public ColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < HSV_ARRAY_SIZE; i++) {
            boundListeners.add(new LinkedList<ColorBoundListener>());
        }
    }

    public int getColor() {
        return Color.HSVToColor(fixedHSV);
    }

    public int getDynamicColor(){
        return Color.HSVToColor(dynamicHSV);
    }

    public void resetColor() {
        setCoreColor(coreColor);
    }

    public void setHSVColor(float[] hsv) {
        System.arraycopy(hsv, 0, this.fixedHSV, 0, HSV_ARRAY_SIZE);
        System.arraycopy(hsv, 0, this.dynamicHSV, 0, HSV_ARRAY_SIZE);
        applyColor(hsv);
    }

    public void setCoreColor(int color) {
        coreColor = color;
        float[] hsv = new float[HSV_ARRAY_SIZE];
        Color.colorToHSV(coreColor, hsv);
        setHSVColor(hsv);
    }

    public void offsetHSVColor(float[] hsvOffset) {
        //Hue max is 360 instead of 1;
        boolean[] maxReached = new boolean[HSV_ARRAY_SIZE];

        dynamicHSV[0] = Math.min(Math.max(fixedHSV[0] + hsvOffset[0], 0), HUE_MAX_VALUE);
        maxReached[0] = dynamicHSV[0] > HUE_MAX_VALUE - EPSILON || dynamicHSV[0] < 0 + EPSILON;

        for (int i = 1; i < HSV_ARRAY_SIZE; i++) {
            dynamicHSV[i] = Math.min(Math.max(fixedHSV[i] + hsvOffset[i], 0), 1);
            maxReached[i] = dynamicHSV[i] > 1- EPSILON || dynamicHSV[i] < 0 + EPSILON;
        }
        applyColor(dynamicHSV);
        for (int i = 0; i < maxReached.length; i++) {
            if (maxReached[i]) {
                for (ColorBoundListener cbl : boundListeners.get(i)) {
                    cbl.onColorBoundReached();
                }
            }
        }
    }

    public void fixCurrentColor() {
        System.arraycopy(dynamicHSV, 0, fixedHSV, 0, HSV_ARRAY_SIZE);
    }

    private void applyColor(float[] hsv) {
        applyColor(Color.HSVToColor(hsv));
    }

    private void applyColor(int color) {
        this.getBackground().setColorFilter(color, PorterDuff.Mode.SRC);
        this.invalidate();
    }

    public void addBoundListener(ColorBoundListener listener, int hsvIndex){
        boundListeners.get(hsvIndex).add(listener);
    }

    interface ColorBoundListener {
        void onColorBoundReached();
    }
}
