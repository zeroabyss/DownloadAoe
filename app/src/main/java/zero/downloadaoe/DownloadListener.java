package zero.downloadaoe;

/**
 * Created by Aiy on 2017/4/3.
 */

public interface DownloadListener {
    void Success();
    void Failed();
    void Pause();
    void Progress(int progress);
    void Cancel();
}
