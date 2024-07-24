package util;

import android.os.AsyncTask;
import com.example.myapplication.ScanActivity;

public class ConnectToReaderTask extends AsyncTask<Void, Void, ConnectionResult> {
    private ScanActivity scanActivity;

    private RFIDReaderHelper rfidReaderHelper;

    public ConnectToReaderTask(ScanActivity activity, RFIDReaderHelper rfidReaderHelper) {
        this.scanActivity = activity;
        this.rfidReaderHelper = rfidReaderHelper;
    }

    @Override
    protected void onPreExecute() {
        // 在执行前显示进度条
        scanActivity.showConnectingProgress();
    }

    @Override
    protected ConnectionResult doInBackground(Void... params) {
        // 在后台执行连接读写器的操作
        ConnectionResult ConnectionResult = rfidReaderHelper.connectToReader();
        return ConnectionResult; // 连接读写器的代码
    }

    @Override
    protected void onPostExecute(ConnectionResult Result) {
        // 在执行完后隐藏进度条
        scanActivity.hideConnectingProgress();
        scanActivity.showToast(Result.getConnectionMessage());
        if (Result.isConnected()) {
            // 连接成功，继续初始化界面
            scanActivity.initUI();
        } else {
            // 连接失败，显示警告对话框或其他操作
            scanActivity.showConnectionErrorDialog();
        }
    }
}
