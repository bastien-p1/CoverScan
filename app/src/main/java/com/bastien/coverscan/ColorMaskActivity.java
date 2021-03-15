package com.bastien.coverscan;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bastien.scan.R;
import com.google.android.material.slider.RangeSlider;

import java.util.Collections;
import java.util.List;

public class ColorMaskActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    float[] def;
    float th_high;
    float th_low;
    boolean inversion;
    float[] th;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_mask);
        //Todo setup rangeslider variables from backend
        //Todo reset view

        sharedPref = ColorMaskActivity.this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        def = new float[]{70,170};
        th_high = sharedPref.getFloat("threshold_high",def[0]);
        th_low = sharedPref.getFloat("threshold_low",def[1]);
        inversion = sharedPref.getBoolean("isInverted",false);
        th = new float[]{th_low, th_high};

        Button bt1 = findViewById(R.id.Abort);
        bt1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                //just return to main view
                returnToMain();
            }
        });

        Button bt2 = findViewById(R.id.Test);
        bt2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                setRangeValues();

                Bitmap wheel = BitmapFactory.decodeResource(getResources(),R.drawable.hsv_color_wheel);
                int colorClear = Color.argb(0,0,0,0);
                int colorMasked = Color.argb(180,0,0,0);

                maskDisplay(wheel, colorClear, colorMasked, inversion);
            }
        });

        Button bt3 = findViewById(R.id.Validate);
        bt3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                setRangeValues();
                returnToMain();
            }
        });
    }


    public void returnToMain(){
        Intent switchActivityIntent = new Intent(ColorMaskActivity.this, MainActivity.class);
        startActivity(switchActivityIntent);
    }

    public void setRangeValues(){
        float upper = 0;
        float lower = 0;

        RangeSlider slider = findViewById(R.id.ColorRange);
        List<Float> values = slider.getValues();
        lower = Collections.min(values);
        upper = Collections.max(values);

        if(lower < 0){
            lower = 360+lower;
        }
        if(upper < 0){
            upper = 360+upper;
        }

        boolean isInverted = (lower > upper);

        editor.putFloat("threshold_high",upper);
        editor.putFloat("threshold_low",lower);
        editor.putBoolean("isInverted",isInverted);

        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void maskDisplay(Bitmap Iarr, int clear, int masked, boolean isInverted){
        Bitmap arr = Iarr.copy(Bitmap.Config.ARGB_8888, true);
        int arrHeight = arr.getHeight();
        int arrWidth = arr.getWidth();

        for(int he = 0; he < arrHeight; he++){
            for(int wi = 0; wi < arrWidth; wi++){
                int rgb = arr.getPixel(wi,he);
                float[] hsv = new float[3];
                int red = Color.red(rgb);
                int green = Color.green(rgb);
                int blue = Color.blue(rgb);
                Color.RGBToHSV(red,green,blue,hsv);
                float hue = hsv[0];

                boolean isInThreshold;
                if(isInverted){
                    isInThreshold = (hue > th[0] || hue < th[1]);
                }else{
                    isInThreshold = (hue > th[0] && hue < th[1]);
                }

                if(isInThreshold){
                    //put the clear color in the corresponding pixel on the masked image
                    arr.setPixel(wi,he,clear);
                }else{
                    //put the mask color in the corresponding pixel on the masked image
                    arr.setPixel(wi,he,masked);
                }
            }
        }
        //then display the masked Image :
        ImageView mask = findViewById(R.id.MaskingView);
        mask.setImageBitmap(arr);
    }
}