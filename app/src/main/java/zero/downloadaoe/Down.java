package zero.downloadaoe;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import static android.content.ContentValues.TAG;

/**
 * Created by Aiy on 2017/4/3.
 */

public class Down extends AsyncTask<String,Integer,Integer> {
    private int status;
    private static final int CANCEL=1;
    private static final int PAUSE=2;
    private static final int FAILED=3;
    private static final int SUCCESS=4;
    private int lastprogress;
    private DownloadListener listener;

    public Down(DownloadListener listener) {
        this.listener=listener;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case CANCEL:
                listener.Cancel();
                break;
            case SUCCESS:
                listener.Success();
                break;
            case FAILED:
                listener.Failed();
                break;
            case PAUSE:
                listener.Pause();
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress=values[0];
        if (progress>lastprogress){
            //发送通知
            listener.Progress(progress);
            lastprogress=progress;
        }

    }

    @Override
    protected Integer doInBackground(String... params) {
        /*
        * 获得地址，文件地址
        * 如果文件存在就获取已下载的长度
        * 优化：文件长度=下载文件长度 说明完成不需要继续。 如果下载文件长度为0，说明文件有问题可以不需要。
        * okhttp获取下载资源（添加报头，从已知文件长度下载）
        * 如果返回值response不为空，然后就继续
        * 获得inputstream  然后用random读写进去文件。
        * 之后刷新UI
        * 结束返回SUCCESS 出现意外返回failed
        * */
        InputStream input=null;
        File file=null;
        RandomAccessFile save=null;
        try{
            long loaded=0;
            String downloadURL=params[0];
            String filename=downloadURL.substring(downloadURL.lastIndexOf("/"));
            String directory= Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS).getPath();
            file=new File(directory+filename);
            if (file.exists()){
                loaded=file.length();
            }
            long contentlength=getUrlContentLength(downloadURL);
            if (contentlength==0){
                return FAILED;
            }
            if (loaded==contentlength){
                return SUCCESS;
            }

            OkHttpClient okHttpClient=new OkHttpClient();
            Request request=new Request.Builder()
                    .url(downloadURL)
                    .addHeader("RANGE","bytes="+loaded+"-")
                    .build();
            Response response=okHttpClient.newCall(request).execute();
            if (response!=null){
                input=response.body().byteStream();
                save=new RandomAccessFile(file,"rw");
                save.seek(loaded);
                byte a[]=new byte[1024];
                int len;
                int total=0;
                while ((len=input.read(a))!=-1){
                    if (status==CANCEL){
                        return CANCEL;
                    }
                    if (status==PAUSE){
                        return PAUSE;
                    }
                    save.write(a,0,len);
                    total+=len;
                    int now=(int)((total+loaded)*100/contentlength);
                    publishProgress(now);
                }
                response.body().close();
                return SUCCESS;
            }


        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try{
                if (input!=null){
                    input.close();
                }
                if (save!=null){
                    save.close();
                }
                if (status==CANCEL&&file!=null){
                    file.delete();
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        return FAILED;
    }

    private long getUrlContentLength(String url){
        try {
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder()
                    .url(url)
                    .build();
            Response r=client.newCall(request).execute();
            if (r!=null&&r.isSuccessful()){
                long content=r.body().contentLength();
                r.body().close();
                return content;
            }else {
                Log.e(TAG, "getUrlContentLength: ");
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return 0;
    }

    public void setPause(){
        status=PAUSE;
    }
    public void setCancel(){
        status=CANCEL;
    }
}
