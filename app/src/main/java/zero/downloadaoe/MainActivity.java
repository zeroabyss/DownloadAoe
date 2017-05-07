package zero.downloadaoe;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MyService.MyBinder myBinder;
    private static final String TAG = "MainActivity";
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder= (MyService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: " );
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i=new Intent(MainActivity.this,MyService.class);
        startService(i);
        bindService(i,connection,BIND_AUTO_CREATE);
        findViewById(R.id.startDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myBinder.start("http://10.0.3.2:8080/1.jpg");
            }
        });

        findViewById(R.id.pauseDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myBinder.pause();
            }
        });

        findViewById(R.id.cancel_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myBinder.cancel();
            }
        });

        String[] permissions={
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        List<String> unPermission=new ArrayList<>();

        for(String s:permissions){
            if (ContextCompat.checkSelfPermission(MainActivity.this,s)
                    !=PackageManager.PERMISSION_GRANTED){
                unPermission.add(s);
            }
        }
        String[] requestPermission=unPermission.toArray(new String[unPermission.size()]);
        ActivityCompat.requestPermissions(MainActivity.this,requestPermission,1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "没有获得相应的权限", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
