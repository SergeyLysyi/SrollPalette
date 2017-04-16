package sergeylysyi.scrollpalette;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.Button;

import java.util.*;

public class ColorButton extends Button {
    static private final int HSV_ARRAY_SIZE = 3;
    private int coreColor;
    private List<ColorBoundListener> boundListeners = new LinkedList<>();
    private float[] fixedHSV = new float[HSV_ARRAY_SIZE];
    private float[] currentHSV = new float[HSV_ARRAY_SIZE];

    public ColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getColor() {
        return Color.HSVToColor(fixedHSV);
    }

    public void resetColor() {
        setCoreColor(coreColor);
    }

    public void setHSVColor(float[] hsv) {
        System.arraycopy(hsv, 0, this.fixedHSV, 0, HSV_ARRAY_SIZE);
        System.arraycopy(hsv, 0, this.currentHSV, 0, HSV_ARRAY_SIZE);
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
        currentHSV[0] = Math.min(Math.max(fixedHSV[0] + hsvOffset[0], 0), 359.9999f);
        for (int i = 1; i < HSV_ARRAY_SIZE; i++) {
            currentHSV[i] = Math.min(Math.max(fixedHSV[i] + hsvOffset[i], 0), 1);
        }
        applyColor(currentHSV);
    }

    public void fixCurrentColor() {
        System.arraycopy(currentHSV, 0, fixedHSV, 0, HSV_ARRAY_SIZE);
    }

    private void applyColor(float[] hsv) {
        applyColor(Color.HSVToColor(hsv));
    }

    private void applyColor(int color) {
        this.getBackground().setColorFilter(color, PorterDuff.Mode.SRC);
        this.invalidate();
    }

    public void addBoundListener(ColorBoundListener listener){
        boundListeners.add(listener);
    }

    interface ColorBoundListener {
        void onColorBoundReached();
    }
}
