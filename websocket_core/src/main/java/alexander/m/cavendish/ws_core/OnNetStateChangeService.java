package alexander.m.cavendish.ws_core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * @Author:mashijie
 * @Date:2019/4/16
 * @Description
 * @Email:alexander.m.cavendish@gmail.com
 */
public final class OnNetStateChangeService extends Service {

    private OnNetChangeListener mOnNetChangeListener;
    private boolean enableLog;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
               if (enableLog){
                   Log.d("OnNetStateChangeService", "网络状态已经改变");
               }
                ConnectivityManager mConnectivityManager = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mInfo = mConnectivityManager.getActiveNetworkInfo();
                if (mInfo != null && mInfo.isAvailable()) {
                    if (null != mOnNetChangeListener) {
                        mOnNetChangeListener.onNetChangeCallback(true);
                    }
                    if (enableLog) {
                        Log.d("OnNetStateChangeService", "有网");
                    }
                } else {
                    if (null != mOnNetChangeListener) {
                        mOnNetChangeListener.onNetChangeCallback(false);
                    }
                    if (enableLog) {
                        Log.d("OnNetStateChangeService", "无网");
                    }
                }
            }
        }
    };

    public class MyBinder extends Binder {
        private MyBinder() {

        }

        public void enableLog(boolean enableLog) {
            OnNetStateChangeService.this.enableLog = enableLog;
        }

        public void setOnNetChangeListener(OnNetChangeListener listener) {
            OnNetStateChangeService.this.mOnNetChangeListener = listener;
        }

        public void regist() {
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mReceiver, mFilter);
        }

        public OnNetStateChangeService getService() {
            return OnNetStateChangeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        if (null != mOnNetChangeListener) {
            mOnNetChangeListener = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //非正常退出重启service,不一定能启动起来,很多厂商对AMS做了限制
        //START_REDELIVER_INTENT同START_STICK重启的延时不一样，START_STICK一般固定1s，而START_REDELIVER_INTENT较长;
        return Service.START_STICKY;
    }
}
