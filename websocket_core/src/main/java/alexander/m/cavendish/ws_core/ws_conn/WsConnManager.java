package alexander.m.cavendish.ws_core.ws_conn;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import alexander.m.cavendish.WsListener;
import alexander.m.cavendish.ws_core.OnNetChangeListener;
import alexander.m.cavendish.ws_core.OnNetStateChangeService;
import alexander.m.cavendish.ws_core.ThreadTask;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;

/**
 * @Author:mashijie
 * @Date:2019/4/15
 * @Description ws连接类
 * @Email:alexander.m.cavendish@gmail.com
 */
public final class WsConnManager implements Handler.Callback, OnNetChangeListener {

    //ws 连接地址
    private String mUrl = "";
    private int mReadTimeOut;
    private int mWriteTimeOut;
    private int mConnectTimeOut;
    private boolean mEnableLog;
    private boolean mAutoReconnect;
    private long mAutoReconnTime;
    private SSLSocketFactory mSslScoketFactory;
    private X509TrustManager mX509TrustManager;
    private HostnameVerifier mHostnameVerifier;
    private volatile int mWsState = alexander.m.cavendish.ws_core.ws_conn.WsState.NORMAL;
    //发ping间隔
    private long mPingSpace;
    private final ExecutorService EXECUTOR_SERVICE = ThreadTask.getSinglePool();
    //回调给dispatcher
    private WsListener mWsListener;
    //ws回调
    private WebSocketListener mWebSocketListener;
    //ws对象
    private WebSocket mWebSocket;
    //定时发送ping的handler
    private static Handler sPingHandler, sReconnectHandler;
    private Context mContext;
    private ServiceConnection mConnection;
    private volatile boolean mIsNetEnable = false;
    private HandlerThread mPingHandlerThread, mReconnectHandlerThred;

    private Handler getsPingHandler() {
        if (null != sPingHandler) {
            sPingHandler.removeCallbacksAndMessages(null);
            sPingHandler = null;
        }
        sPingHandler = new Handler(mPingHandlerThread.getLooper(), WsConnManager.this);
        return sPingHandler;
    }

    private Handler getsReconnectHandler() {
        if (null == sReconnectHandler) {
            sReconnectHandler = new Handler(mReconnectHandlerThred.getLooper(), WsConnManager.this);
        }
        return sReconnectHandler;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (mIsNetEnable) {
            if (msg.what == 0) {
                try {
                    //发送ping
                    Buffer buffer = new Buffer();
                    buffer.write("ping".getBytes(), 0, "ping".length());
                    mWebSocket.sendPing(buffer);
                    if (mEnableLog) {
                        Log.d("WsConnManager", "OnMessage-Send:ping");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (msg.what == 1) {
                //重连
                startConnWs();
            }
        } else {
            mWsState = WsState.FAILED;
        }
        return false;
    }

    private WsConnManager() {

    }

    private WebSocketListener getWebSocketListener() {
        if (null != mWebSocketListener) {
            mWebSocketListener = null;
        }
        mWebSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                mWebSocket = webSocket;
                mWsState = WsState.CONNECTED;
                if (null != sReconnectHandler) {
                    sReconnectHandler.removeCallbacksAndMessages(null);
                    sReconnectHandler = null;
                }
                if (null != mReconnectHandlerThred) {
                    mReconnectHandlerThred.quitSafely();
                    mReconnectHandlerThred = null;
                }
                if (mEnableLog) {
                    Log.d("WsConnManager", "onOpen");
                }
                if (null != mWsListener) {
                    mWsListener.onWsOpen();
                }
                mIsNetEnable = true;
                if (null == mPingHandlerThread) {
                    mPingHandlerThread = new HandlerThread("WS-PING");
                    mPingHandlerThread.start();
                }
                try {
                    Buffer buffer = new Buffer();
                    buffer.write("ping".getBytes(), 0, "ping".length());
                    mWebSocket.sendPing(buffer);
                    if (mEnableLog) {
                        Log.d("WsConnManager", "OnMessage-Send:ping");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(ResponseBody message) {
                try {
                    WeakReference<String> wr = null;
                    wr = new WeakReference<>(message.string());
                    if (mEnableLog) {
                        Log.d("WsConnManager", "onMessage:" + wr.get());
                    }
                    if (null != mWsListener && !TextUtils.isEmpty(wr.get())) {
                        mWsListener.onWsMessage(wr.get());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int code, String reason) {
                mWsState = WsState.CLOSED;
                if (mEnableLog) {
                    Log.d("WsConnManager", "onClosed---code:" + code + "---reason:" + reason);
                }
                if (null != mWsListener) {
                    mWsListener.onWsClosed(code, reason);
                }
            }

            @Override
            public void onPong(Buffer payload) {
                if (mEnableLog) {
                    Log.d("WsConnManager", "OnMessage-Accept:onPng");
                }
                getsPingHandler().sendEmptyMessageDelayed(0, mPingSpace);
            }

            @Override
            public void onFailure(IOException e, Response response) {
                mWsState = WsState.FAILED;
                if (mEnableLog) {
                    Log.d("WsConnManager", "onFailure:" + e.getMessage());
                }
                if (null != mWsListener) {
                    mWsListener.onWsFail(e, response);
                    if (mAutoReconnect) {
                        mWsListener.onWsReconnecting();
                    }
                }
                if (null != sPingHandler) {
                    sPingHandler.removeCallbacksAndMessages(null);
                    sPingHandler = null;
                }
                if (null != mPingHandlerThread) {
                    mPingHandlerThread.quitSafely();
                    mPingHandlerThread = null;
                }
                if (mAutoReconnect && mIsNetEnable) {
                    mWsState = WsState.RECONN_IN_QUEUE;
                    mReconnectHandlerThred = new HandlerThread("WS-RECONN");
                    mReconnectHandlerThred.start();
                    getsReconnectHandler().sendEmptyMessageDelayed(1, mAutoReconnTime);
                }
            }

        };
        return mWebSocketListener;
    }


    public void sendMessage(final String message) {
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                if (mEnableLog) {
                    Log.d("WsConnManager", "OnMessage-Send:" + message);
                }
                try {
                    WeakReference<RequestBody> weakReference =
                            new WeakReference<>(RequestBody.create(WebSocket.TEXT, message));
                    mWebSocket.sendMessage(weakReference.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void startConnWs() {
        if (mWsState == WsState.CONNECTING || mWsState == WsState.CONNECTED) {
            return;
        }
        if (mEnableLog) {
            Log.d("WsConnManager", "cnnecting");
        }
        mWsState = WsState.CONNECTING;
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mContext && null == mConnection) {
                    Intent mIntent = new Intent(mContext, OnNetStateChangeService.class);
                    mConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            OnNetStateChangeService.MyBinder mBinder = (OnNetStateChangeService.MyBinder) service;
                            mBinder.regist();
                            mBinder.enableLog(mEnableLog);
                            mBinder.setOnNetChangeListener(WsConnManager.this);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {

                        }
                    };
                    mContext.bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
                }
                Request request = new Request
                        .Builder()
                        .url(mUrl)
                        .build();
                OkHttpClient.Builder ob = new OkHttpClient.Builder()
                        .readTimeout(mReadTimeOut, TimeUnit.SECONDS)
                        .writeTimeout(mWriteTimeOut, TimeUnit.SECONDS)
                        .connectTimeout(mConnectTimeOut, TimeUnit.SECONDS);
                if (null != mX509TrustManager) {
                    ob.sslSocketFactory(mSslScoketFactory, mX509TrustManager);
                }
                if (null != mHostnameVerifier) {
                    ob.hostnameVerifier(mHostnameVerifier);
                }
                OkHttpClient client = ob.build();
                WebSocketCall webSocketCall = WebSocketCall.create(client, request);
                webSocketCall.enqueue(getWebSocketListener());
            }
        });
    }

    public void disConnWs() {
        if (null != sPingHandler) {
            sPingHandler.removeCallbacksAndMessages(null);
            sPingHandler = null;
        }
        if (null != mPingHandlerThread) {
            mPingHandlerThread.quitSafely();
            mPingHandlerThread = null;
        }
        if (null != sReconnectHandler) {
            sReconnectHandler.removeCallbacksAndMessages(null);
        }
        if (null != mReconnectHandlerThred) {
            mReconnectHandlerThred.quitSafely();
            mReconnectHandlerThred = null;
        }
        if (null != mWebSocket) {
            try {
                mWebSocket.close(3000, "normal close");
                mWebSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != mContext && null != mConnection) {
            mContext.unbindService(mConnection);
            mContext = null;
        }
        if (null != mX509TrustManager) {
            mX509TrustManager = null;
        }
        if (null != mSslScoketFactory) {
            mSslScoketFactory = null;
        }
        if (null != mHostnameVerifier) {
            mHostnameVerifier = null;
        }
    }

    @Override
    public void onNetChangeCallback(boolean hasNet) {
        if (hasNet && mWsState == WsState.FAILED) {
            startConnWs();
        }
        mIsNetEnable = hasNet;
    }


    public static class Builder {
        private WsConnManager wsConnManager;

        public Builder() {
            wsConnManager = new WsConnManager();
        }

        public Builder enanbleLog(boolean enableLog) {
            wsConnManager.mEnableLog = enableLog;
            return this;
        }

        public Builder url(String url) {
            wsConnManager.mUrl = url;
            return this;
        }

        public Builder readTimeOut(int readTimeOut) {
            wsConnManager.mReadTimeOut = readTimeOut;
            return this;
        }

        public Builder wirteTimeOut(int wirteTimeOut) {
            wsConnManager.mWriteTimeOut = wirteTimeOut;
            return this;
        }

        public Builder connectTimeOut(int connTimeOut) {
            wsConnManager.mConnectTimeOut = connTimeOut;
            return this;
        }

        public Builder wsListener(WsListener wsListener) {
            wsConnManager.mWsListener = wsListener;
            return this;
        }

        public Builder autoReconnect(boolean autoReconn) {
            wsConnManager.mAutoReconnect = autoReconn;
            return this;
        }

        public Builder autoReconnTime(long autoReconnTime) {
            wsConnManager.mAutoReconnTime = autoReconnTime;
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager manager) {
            wsConnManager.mSslScoketFactory = sslSocketFactory;
            wsConnManager.mX509TrustManager = manager;
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            wsConnManager.mHostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder netStateService(Context applicationContext) {
            wsConnManager.mContext = applicationContext;
            return this;
        }

        public Builder pingSpace(long pingSpace) {
            wsConnManager.mPingSpace = pingSpace;
            return this;
        }

        public WsConnManager build() {
            return wsConnManager;
        }
    }
}
