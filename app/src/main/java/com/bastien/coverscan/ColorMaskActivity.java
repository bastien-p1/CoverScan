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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bastien.scan.R;
import com.google.android.material.slider.RangeSlider;

import java.util.Collections;
import java.util.List;

public class ColorMaskActivity extends AppCompatActivity {
    SharedPreferences sharedPref = ColorMaskActivity.this.getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    float[] def = new float[]{70,170};
    float th_hight = sharedPref.getFloat("threshold_hight",def[0]);
    float th_low = sharedPref.getFloat("threshold_low",def[1]);
    float[] th = new float[]{th_low,th_hight};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_mask);

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

                maskDisplay(wheel, colorClear, colorMasked);

                returnToMain();
            }
        });

        Button bt3 = findViewById(R.id.Abort);
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

        float[] th = new float[]{lower,upper};

        editor.putFloat("threshold_hight",upper);
        editor.putFloat("threshold_low",lower);

        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void maskDisplay(Bitmap arr, int clear, int masked){
        //perform a color mask to :
        //1: get the area occupied by green color on the image
        //2: get a black and white image of the original image masked
        int arrHeight = arr.getHeight();
        int arrWidth = arr.getWidth();
        int ckPx = 0;//checked pixels

        for(int he = 0; he < arrHeight; he++){
            for(int wi = 0; wi < arrWidth; wi++){
                int rgb = arr.getPixel(wi,he);
                float[] hsv = new float[3];
                int red = Color.red(rgb);
                int green = Color.green(rgb);
                int blue = Color.blue(rgb);
                Color.RGBToHSV(red,green,blue,hsv);
                float hue = hsv[0];
                boolean isInThreshold = (hue > th[0] && hue < th[1]);

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