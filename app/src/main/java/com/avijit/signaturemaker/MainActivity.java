package com.avijit.signaturemaker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.avijit.signaturemaker.databinding.ActivityMainBinding;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;

public class MainActivity extends AppCompatActivity {
    private static Context CONTEXT;
    ActivityMainBinding binding;
    public static final int PERMISSION_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater(), null, false);
        CONTEXT = this;
        setContentView(binding.getRoot());
        binding.icErase.setOnClickListener(v -> {
            binding.signatureView.clear();
        });
        binding.icColor.setOnClickListener(v -> {
            new ColorPickerDialog()
                    .withColor(getResources().getColor(R.color.black)) // the default / initial color
                    .withListener((dialog, color) -> {
                        binding.signatureView.setPenColor(color);
                    })
                    .show(getSupportFragmentManager(), "colorPicker");
        });
        binding.icSave.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    CONTEXT, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                // You can use the API that requires the permission.
                //performAction(...);
                binding.signatureView.saveToDevice();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                } else {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.signatureView.saveToDevice();
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    Toast.makeText(CONTEXT, "Allow permissions to save to sd card", Toast.LENGTH_SHORT).show();
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.alert_dialog_message)
                .setPositiveButton("Yes", (dialogInterface, i) -> finishAffinity())
                .setNegativeButton("No", null)
                .create()
                .show();
    }
}