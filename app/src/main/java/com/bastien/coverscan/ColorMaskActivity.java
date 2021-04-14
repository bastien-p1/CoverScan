package com.bastien.coverscan;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.bastien.scan.R;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.tabs.TabLayout;

import java.util.Collections;
import java.util.List;

public class ColorMaskActivity extends AppCompatActivity {
    SharedPreferences sharedPref = ColorMaskActivity.this.getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    float[] def = new float[]{70, 170};
    float th_hight = sharedPref.getFloat("threshold_hight", def[0]);
    float th_low = sharedPref.getFloat("threshold_low", def[1]);
    float[] th = new float[]{th_low, th_hight};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_mask);

    }

    public void implantCheckboxes() {
        float initialSaturation = (float) 0.3;
        float initialValue = (float) 0.3;
        int nb_rows = 16;
        int nb_columns = 4;
        TableLayout nuancier = (TableLayout) findViewById(R.id.palette);
        TableRow[] rows = new TableRow[nb_rows];
        CheckBox[][] boxes = new CheckBox[nb_rows][nb_columns];

        TableRow.LayoutParams commonRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams commonCheckboxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)


        for (int i = 0; i < nb_rows; i++) {
            TableRow newRow = new TableRow(this);
            newRow.setLayoutParams(commonRowParams);
            //TableRow[i] =
            for (int j = 0; j < nb_columns; j++) {
                CheckBox colorbox = new CheckBox(this);
                colorbox.setText("");
                int colorScale = (i * nb_columns + j) / (nb_rows * nb_columns);
                float[] HSV_color = new float[]{colorScale, initialSaturation, initialValue};
                int color = Color.HSVToColor(HSV_color);
                colorbox.setBackgroundColor(color);
                colorbox.setLayoutParams(commonCheckboxParams);
                //TODO: setEventListeners
                newRow.addView(colorbox);
            }
            nuancier.addView(newRow);
        }
        editor.putInt("nb_color_boxes",(nb_rows * nb_columns));
        editor.apply();
    }

    public void returnToMain() {
        Intent switchActivityIntent = new Intent(ColorMaskActivity.this, MainActivity.class);
        startActivity(switchActivityIntent);
    }

    public void setRangeValues() {


        //keep this line for later reference
        //editor.putFloat("threshold_low",lower);

        editor.apply();
    }
}