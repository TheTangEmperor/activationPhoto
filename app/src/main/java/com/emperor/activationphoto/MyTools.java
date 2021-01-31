package com.emperor.activationphoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.util.Base64;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.core.app.ActivityCompat;

public class MyTools {

    /**
     验证权限
     */
    public static boolean verifyPermissions(Activity activity, String permission) {
        // Check if we have write permission
        int result = ActivityCompat.checkSelfPermission(activity, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**提示框
     *
     * @param tipText
     * @param mContext
     */
    public static void showTipDialog(String tipText, Activity mContext, DialogInterface.OnClickListener confirm, DialogInterface.OnClickListener cancel){
        if (mContext == null){
            System.out.println("showTipDialog:  mContext is null");
            return ;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("提示");
        TextView mMsg = new TextView(mContext);
        mMsg.setText(tipText);
        mMsg.setPadding(30,0,30,0);
//        mMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        mMsg.setTextSize(18);
        builder.setView(mMsg);
        builder.setCancelable(false);
        if (confirm != null){
            builder.setPositiveButton("确定", confirm);
        }
        if (cancel != null){
            builder.setNegativeButton("取消", cancel);
        }
        Dialog tipDialog = builder.create();
        tipDialog.setCanceledOnTouchOutside(false);
        tipDialog.show();
    }


    /**
     * 打开当前应用得系统页面
     * @param context
     */
    public static void openAppInfoPage(Context context){
        if (context == null) return;
        try{
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }catch (Exception e){
            System.out.println("打开本应用设置界面失败： " + e.getMessage());
            try{
                context.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
            }catch (Exception e2){
                e2.printStackTrace();
            }
        }
    }



    /**
     * bitmap转base64
     * @param bitmap 图像
     * @param qua 压缩级别，100是不压缩
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap, int qua) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, qua, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }





}
