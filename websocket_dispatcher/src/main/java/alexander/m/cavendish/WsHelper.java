package alexander.m.cavendish;

import android.content.Context;
import android.text.TextUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import alexander.m.cavendish.ws_core.ws_conn.WsConnManager;
import alexander.m.cavendish.ws_dispatcher.OnMsgListener;
import alexander.m.cavendish.ws_dispatcher.OnSignalEventListener;
import alexander.m.cavendish.ws_dispatcher.OnWsStateListener;
import alexander.m.cavendish.ws_dispatcher.WsDispatcher;

/**
 * @Author:mashijie
 * @Date:2019/4/15
 * @Description 对外暴露ws调用方法
 * @Email:alexander.m.cavendish@gmail.com
 */
public final class WsHelper {
    private WsConnManager wsConnManager;
    private OnMsgListener mOnMsgListener;
    private OnSignalEventListener mOnSignalEventListener;
    private OnWsStateListener mOnWsStateListener;
    private static boolean sEnableLog = true;
    private static boolean sAutoReconnect = true;
    private static long sAutoReconnectTime = 2000;
    private static int sReadTimeOut = 10;
    private static int sWriteTimeOut = 10;
    private static int sConnectTimeOut = 10;
    //发ping间隔
    private static long sPingSpace = 2000;
    private static String sUrl;
    private static Context sAppliationContext;
    private static WsDispatcher mWsDispatcher;
    private static SSLSocketFactory sslSocketFactory;
    private static X509TrustManager sX509TrustManager;
    private static HostnameVerifier sHostnameVerifier;

    private WsHelper() {
        WsConnManager.Builder wb = new WsConnManager.Builder()
                .url(sUrl)
                .connectTimeOut(sConnectTimeOut)
                .readTimeOut(sReadTimeOut)
                .wirteTimeOut(sWriteTimeOut)
                .netStateService(sAppliationContext)
                .enanbleLog(sEnableLog)
                .pingSpace(sPingSpace)
                .wsListener(mWsDispatcher)
                .autoReconnect(sAutoReconnect)
                .autoReconnTime(sAutoReconnectTime);
        if (null != sX509TrustManager) {
            wb.sslSocketFactory(sslSocketFactory, sX509TrustManager);
        }
        if (null != sHostnameVerifier) {
            wb.hostnameVerifier(sHostnameVerifier);
        }
        wsConnManager = wb.build();

    }

    public void sendMessage(String message) {
        wsConnManager.sendMessage(message);
    }

    public void startWsConnect() {
        wsConnManager.startConnWs();
    }

    public void disWsConnect() {
        if (null != wsConnManager) {
            wsConnManager.disConnWs();
            wsConnManager = null;
        }
        if (null != mWsDispatcher) {
            mWsDispatcher.cancel();
            mWsDispatcher = null;
        }
        if (null != mOnMsgListener) {
            mOnMsgListener = null;
        }
        if (null != mOnSignalEventListener) {
            mOnSignalEventListener = null;
        }
        if (null != mOnWsStateListener) {
            mOnWsStateListener = null;
        }
        if (null != sAppliationContext) {
            sAppliationContext = null;
        }
        if (null != sX509TrustManager) {
            sX509TrustManager = null;
        }
        if (null != sslSocketFactory) {
            sslSocketFactory = null;
        }
        if (null != sHostnameVerifier) {
            sHostnameVerifier = null;
        }
    }

    public static class Builder {
        private WsDispatcher.Builder wb;

        public Builder() {
            wb = new WsDispatcher.Builder();
        }

        public Builder autoReconnect(boolean autoReconnect) {
            sAutoReconnect = autoReconnect;
            return this;
        }

        public Builder autoReconnectTime(long time) {
            sAutoReconnectTime = time;
            return this;
        }

        public Builder enanbleLog(boolean enableLog) {
            sEnableLog = enableLog;
            return this;
        }

        public Builder url(String url) {
            sUrl = url;
            return this;
        }

        public Builder readTimeOut(int readTimeOut) {
            sReadTimeOut = readTimeOut;
            return this;
        }

        public Builder wirteTimeOut(int wirteTimeOut) {
            sWriteTimeOut = wirteTimeOut;
            return this;
        }

        public Builder connectTimeOut(int connTimeOut) {
            sConnectTimeOut = connTimeOut;
            return this;
        }

        public Builder netStateService(Context applicationContext) {
            sAppliationContext = applicationContext.getApplicationContext();
            return this;
        }

        public Builder pingSpace(long pingSpace) {
            sPingSpace = pingSpace;
            return this;
        }

        public Builder msgListener(OnMsgListener listener) {
            wb = wb.msgListener(listener);
            return this;
        }

        public Builder signalEventListener(OnSignalEventListener listener) {
            wb = wb.signalEventListener(listener);
            return this;
        }

        public Builder wsStateListener(OnWsStateListener listener) {
            wb = wb.wsStateListener(listener);
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager manager) {
            WsHelper.sslSocketFactory = sslSocketFactory;
            WsHelper.sX509TrustManager = manager;
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            WsHelper.sHostnameVerifier = hostnameVerifier;
            return this;
        }

        public WsHelper build() {
            if (TextUtils.isEmpty(sUrl)) {
                throw new NullPointerException("url is null");
            }
            if (null == sAppliationContext) {
                throw new NullPointerException("context is null,netStateService() need a context");
            }
            WsHelper.mWsDispatcher = wb.build();
            return new WsHelper();
        }
    }

}
