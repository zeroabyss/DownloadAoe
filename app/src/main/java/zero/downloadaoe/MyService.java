package zero.downloadaoe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MyService extends Service {
    private static final String TAG = "MyService";
    private Down down;
    private String downUrl;
    private DownloadListener downloadListener=new DownloadListener() {
        @Override
        public void Success() {
            down=null;
            stopForeground(true);
            Toast.makeText(MyService.this,"下载完成",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void Failed() {
            down=null;
            stopForeground(true);
            Toast.makeText(MyService.this,"下载失败",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void Pause() {
            down=null;
            stopForeground(true);
            Toast.makeText(MyService.this,"暂停下载",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void Progress(int progress) {
            getManager().notify(1,getNotification("下载中",progress));
        }

        @Override
        public void Cancel() {
            down=null;
            stopForeground(true);
            Toast.makeText(MyService.this,"取消",Toast.LENGTH_SHORT).show();
        }
    };
    private MyBinder binder=new MyBinder();
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class MyBinder extends Binder{

        public void start(String url){
            if (down==null){
                down=new Down(downloadListener);
                downUrl=url;
                down.execute(url);
                //防止休眠的时候被迫停止服务，所以设置成前台服务。
                startForeground(1,getNotification("开始下载",0));
                Log.e(TAG, "start: ");

            }
        }

        public void pause(){
            if (down!=null){
                down.setPause();
            }
        }

        public void cancel(){
            if (down!=null){
                down.setCancel();
            }else{
                if (downUrl!=null){
                    String filename=downUrl.substring(downUrl.lastIndexOf("/"));
                    String filedirectory= Environment.getExternalStoragePublicDirectory
                            (Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file=new File(filedirectory+filename);
                    if (file.exists())
                        file.delete();
                    stopForeground(true);
                    Log.e("Myservice", "cancel: ");
                }
            }
        }
    }


    private NotificationManager getManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    private Notification getNotification(String title,int progress){
        Intent i=new Intent(this,MainActivity.class);
        PendingIntent intent=PendingIntent.getActivity(this,0,i,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setContentTitle(title);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(intent);
        if (progress>0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();

    }
}
