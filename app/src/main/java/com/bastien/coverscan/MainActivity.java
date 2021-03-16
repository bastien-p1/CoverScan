package com.bastien.coverscan;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bastien.scan.R;

public class MainActivity extends AppCompatActivity {

    //creating a default for the threshold values
    SharedPreferences sharedPref;
    float[] def;
    float upper;
    float lower;

    //creating a response code for the activity
    static final int REQUEST_IMAGE_CAPTURE = 100;
    //creating a global variable to contain the value of the covering
    float covering;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = MainActivity.this.getSharedPreferences("Values",Context.MODE_PRIVATE);
        def = new float[]{70,170};
        lower = sharedPref.getFloat("threshold_low",def[0]);
        upper = sharedPref.getFloat("threshold_high",def[1]);

        Button bt1 = findViewById(R.id.TakePictureButton);
        bt1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                //start the entire process chain
                capturePhoto();
            }
        });

        Button bt2 = findViewById(R.id.mask);
        bt2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                //switch to color mask parameters view
                Intent switchActivityIntent = new Intent(MainActivity.this, ColorMaskActivity.class);
                startActivity(switchActivityIntent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)

    private void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
            Log.e("fail", "capturePhoto: capture failed");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            //execute code after the execution of capturePhoto():
            Bundle extras = data.getExtras();
            Bitmap byteImg = (Bitmap) extras.get("data");

            //process the picture :
            cover(byteImg);

        }
    }

    
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void cover(Bitmap arr){
        //perform a color mask to :
        //1: get the area occupied by green color on the image
        //2: get a black and white image of the original image masked
        int arrHeight = arr.getHeight();
        int arrWidth = arr.getWidth();
        int ckPx = 0;//checked pixels
        
        if(lower < 0){
            lower = 360+lower;
        }
        if(upper < 0){
            upper = 360+upper;
        }
        boolean isInverted = (lower > upper);

        Log.d("DBSHI", "setRangeValues: "+sharedPref.getFloat("threshold_low",1000));
        Log.d("DBSHI", "setRangeValues: "+sharedPref.getFloat("threshold_high",1000));

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
                    isInThreshold = (hue > lower || hue < upper);
                }else{
                    isInThreshold = (hue > lower && hue < upper);
                }

                if(isInThreshold){
                    //create color for the pixel
                    int color = Color.argb(255,0,0,0);
                    //put this color in the corresponding pixel on the masked image
                    arr.setPixel(wi,he,color);
                    //increase corresponding pixel counter
                    ckPx += 1;
                }else{
                    //create color for the pixel
                    int color = Color.argb(255,255,255,255);
                    //put this color in the corresponding pixel on the masked image
                    arr.setPixel(wi,he,color);
                }
            }
        }
        covering = (ckPx*100)/(arrHeight*arrWidth);
        //display the covering ratio we got :
        TextView coveringDisplay = findViewById(R.id.coveringDisplay);
        String display = "couverture : "+ covering + "%";
        Log.d("result", display);
        coveringDisplay.setText(display);
        //then display the masked Image :
        ImageView mask = findViewById(R.id.maskedDisplay);
        mask.setImageBitmap(arr);
    }
}

