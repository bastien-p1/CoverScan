package com.bastien.coverscan;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bastien.scan.R;

public class MainActivity extends AppCompatActivity {

    String scan_mode;
    static final int REQUEST_IMAGE_CAPTURE = 100;
    float MaskValue = 109/100;//define the green level determining the vegetal covering of the surface
    float covering;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bt1 = findViewById(R.id.TakePictureButton);
        bt1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                //start the entire process chain
                Spinner mode = findViewById(R.id.scan_modes);
                scan_mode = String.valueOf(mode.getSelectedItem());
                capturePhoto();
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
        //perform a colormask to :
        //1: get the area occupied by green color on the image
        //2: get a black and white image of the original image masked
        int arrHeight = arr.getHeight();
        int arrWidth = arr.getWidth();
        int ckPx = 0;

        for(int he = 0; he < arrHeight; he++){
            for(int wi = 0; wi < arrWidth; wi++){
                //TODO: refactor this part using HSV color comparison or functional comparison
                int c = arr.getPixel(wi,he);
                int red = Color.red(c);
                int green = Color.green(c);
                int blue = Color.blue(c);
                int Avr = (red+green+blue)/3;
                if(!(Avr == 0) && ((green/Avr) > MaskValue)){
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
        String display = "couverture : "+ Float.toString(covering) + "%";
        Log.d("result", display);
        coveringDisplay.setText(display);
        //then display the masked Image :
        ImageView mask = findViewById(R.id.maskedDisplay);
        mask.setImageBitmap(arr);
    }
}

