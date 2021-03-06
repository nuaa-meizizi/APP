package com.scy.health.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class PremissionDialog {
    private static final String TAG = "PremissionDialog";

    public static void showMissingPermissionDialog(final Context context, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("帮助");
        builder.setMessage(content);

        // 拒绝, 退出应用
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                startAppSettings(context);
            }
        });

        builder.show();
    }

    // 启动应用的设置
    private static void startAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    public static boolean lacksPermission(String permission,Context context) {
        try {
            return ContextCompat.checkSelfPermission(context.getApplicationContext(), permission) ==
                    PackageManager.PERMISSION_DENIED;
        }
        catch (Exception e){
            Log.e(TAG, "lacksPermission: ", e);
            return false;
        }
    }
}
