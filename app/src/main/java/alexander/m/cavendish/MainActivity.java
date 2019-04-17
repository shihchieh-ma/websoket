package alexander.m.cavendish;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

import alexander.m.cavendish.ws_dispatcher.OnMsgListener;
import alexander.m.cavendish.ws_dispatcher.OnSignalEventListener;
import alexander.m.cavendish.ws_dispatcher.OnWsStateListener;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnWsStateListener, OnSignalEventListener, OnMsgListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private WsHelper wsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method

        wsHelper = new WsHelper.Builder()
                .url("ws://22.86.119.73:8080/mgw4sdk")//ws地址,*必填
                .netStateService(getApplicationContext())//启动网络状态变化监听的服务,*必填
                .autoReconnect(true)//自动重连,默认true
                .autoReconnectTime(2000)//重连时间,默认失败2s后重连
                .pingSpace(2000)//发ping间隔,默认2s
                .enanbleLog(true)//打印日志,默认为true
                .connectTimeOut(5)//连接超时时长,默认10s
                .readTimeOut(5)//读取超时时长,默认10s
                .wirteTimeOut(5)//写超时时长,默认10s
                .msgListener(this)//消息回调
                .signalEventListener(this)//信令回调
                .wsStateListener(this)//ws状态变更回调
//                .sslSocketFactory()//数字签名认证,默认无
//                .hostnameVerifier()//IP地址和hostnanme验证,默认OkHostnameVerifier
                .build();
        //开始连接
        wsHelper.startWsConnect();
    }

    public void disConn(View view) {
        //断开连接
        wsHelper.disWsConnect();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    @Override
    public void onWsMessage(String msg) {
    }

    @Override
    public void onChatStart() {
    }

    @Override
    public void onChatEnd() {
    }

    @Override
    public void onWsOpen() {
    }

    @Override
    public void onWsReconnecting() {
    }

    @Override
    public void onWsFail(IOException e, Response response) {

    }

    @Override
    public void onWsClosed(int code, String reason) {
    }
}
