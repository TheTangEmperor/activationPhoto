package com.emperor.activationphoto;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivPhoto;
    private String [] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private Uri imageUri;
    private final int PHOTO_REQUEST_SELECT = 2;// 选照片之后

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnGo).setOnClickListener(this);
        ivPhoto = findViewById(R.id.ivPhoto);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 110);
        }

    }

    @Override
    public void onClick(View v) {
        if (MyTools.verifyPermissions(this, Manifest.permission.CAMERA) && MyTools.verifyPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            goPhotoAlbum();
        }else {
            MyTools.showTipDialog("所需权限未授予，是否前去授权？", this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MyTools.openAppInfoPage(MainActivity.this);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
    }

    //打开相册
    public void goPhotoAlbum() {
        ContentValues contentValues = new ContentValues(2);
        //生成缓存照片名称
        String fileName = "IMG_" + DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
        //生成缓存照片路径
        String filePath = getExternalCacheDir() + fileName;
        contentValues.put(MediaStore.Images.Media.DATA, filePath);
        // 通过插入contentValue获得生成的插入位置，Android10系统时不为Null
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (imageUri == null ){
            // 大于Android7 使用Provider获取uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", new File(filePath));
            }else {
                imageUri = Uri.fromFile(new File(filePath));
            }
        }
//      创建相机的action
        Intent capture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//            告诉系统相机要保存的位置
        capture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//            创建打开相册的action
        Intent photo = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//          最终要执行打开操作的action
        Intent chooserIntent = Intent.createChooser(photo, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{capture});
        startActivityForResult(chooserIntent, PHOTO_REQUEST_SELECT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            if (requestCode == PHOTO_REQUEST_SELECT){
                if (data != null) {
                    // 这里是针对从相册中选图片的处理
                    imageUri = data.getData();
                }else {
                    // 拍照后返回的处理
                    updatePhotos();
                }
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            // 通过uri转换成bitmap， 注意该操作是异步
                            final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            // 将图像转换成base64字符串后上传，设置50的压缩率
                            upload(MyTools.bitmapToBase64(bitmap, 50));
                            // 显示到界面上
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ivPhoto.setImageBitmap(bitmap);
                                }
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
    }


    /**
     * 告诉系统相册有新的相片
     */
    private void updatePhotos() {
        // 该广播即使多发（即选取照片成功时也发送）也没有关系，只是唤醒系统刷新媒体文件
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(imageUri);
        sendBroadcast(intent);
    }

    /**
     * 上传照片
     * @param base64 图像转换后的码
     */
    private void upload(String base64){
        System.out.println(base64);
    }

}