package in.cs654.chariot.imageblur;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.cs654.chariot.avro.BasicRequest;
import in.cs654.chariot.avro.BasicResponse;
import in.cs654.chariot.utils.CommonUtils;
import in.cs654.chariot.utils.PrashtiClient;

public class MainActivity extends AppCompatActivity {

    int TAKE_PHOTO_CODE = 0;
    public static int count = 0;
    final static String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/chariot/";
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File newdir = new File(dir);
        newdir.mkdirs();
        count++;
        String file = dir+count+".jpg";
        final File newfile = new File(file);
        try {
            newfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri outputFileUri = Uri.fromFile(newfile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);


        Button process = (Button) findViewById(R.id.btnProcess);
        process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TextView textView = (TextView) findViewById(R.id.textView);
//                textView.setText("Waiting...");
                try {
                    // TODO correct it to call blur function (after DB and device setup)
                    String imgFilePath = dir + count + ".jpg";
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFilePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bytes = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("imgBytes", encodedImage);
                    PrashtiClient client = new PrashtiClient();
                    BasicRequest imgBlurReq = BasicRequest.newBuilder()
                            .setRequestId(CommonUtils.randomString(32))
                            .setDeviceId("imgBlurApp")
                            .setFunctionName("blur")
                            .setArguments(new ArrayList<String>())
                            .setExtraData(map)
                            .build();
                    BasicResponse response = client.call(imgBlurReq);
//                    textView.setText(response.getResponse().get("answer"));
                    byte[] bytes1 = Base64.decode(response.getResponse().get("blurStr"), Base64.DEFAULT);
                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.length);
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(bitmap1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            ImageView view = (ImageView) findViewById(R.id.imageView);
            String file = dir+count+".jpg";
            final File newfile = new File(file);
            if (newfile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(newfile.getAbsolutePath());
                view.setImageBitmap(bitmap);
            }
        }
    }
}
