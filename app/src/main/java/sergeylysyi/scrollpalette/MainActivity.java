package sergeylysyi.scrollpalette;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;
import java.util.List;


public class MainActivity extends Activity {
    private List<ColorButton> savedColorButtons = new LinkedList<>();
    private CurrentColor currentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_acivity);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paletteForResult();
            }
        });

        currentColor = new CurrentColor(0);

        View colorView = findViewById(R.id.colorView);

        currentColor.addViewForBackgroundChange(colorView);

        colorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText(
                            "no data here, look instance of " + ColorButton.class.toString(), "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            v);
                    v.startDrag(data, shadowBuilder, v, 0);
                    return true;
                } else {
                    return false;
                }
            }
        });

        ColorButton b1 = (ColorButton) findViewById(R.id.color_button1);
        ColorButton b2 = (ColorButton) findViewById(R.id.color_button2);
        ColorButton b3 = (ColorButton) findViewById(R.id.color_button3);

        savedColorButtons.add(b1);
        savedColorButtons.add(b2);
        savedColorButtons.add(b3);

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        int idx = 1;
        for (ColorButton b : savedColorButtons) {
            b.setCoreColor(settings.getInt("saved_color_" + idx, Color.LTGRAY));
            idx++;
        }
        currentColor.change(settings.getInt("current_color", 0));

        for (final ColorButton b : savedColorButtons) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentColor.change(b.getColor());
                }
            });
            b.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    switch (event.getAction()) {
                        case DragEvent.ACTION_DRAG_ENTERED:
                            b.setColor(currentColor.getColor());
                            break;
                        case DragEvent.ACTION_DRAG_EXITED:
                            b.resetColor();
                            break;
                        case DragEvent.ACTION_DROP:
                            b.setCoreColor(currentColor.getColor());
                            break;
                    }
                    return true;
                }
            });
        }
    }

    void paletteForResult() {
        Intent intent = new Intent(this, ScrollPalette.class);
        startActivityForResult(intent, ScrollPalette.REQUEST_PALETTE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ScrollPalette.REQUEST_PALETTE:
                    currentColor.change(data.getIntExtra("color", 0));
                    return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int idx = 1;
        for (ColorButton b : savedColorButtons) {
            editor.putInt("saved_color_" + idx, b.getColor());
            idx++;
        }
        editor.putInt("current_color", currentColor.getColor());
        editor.apply();

        super.onStop();
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
