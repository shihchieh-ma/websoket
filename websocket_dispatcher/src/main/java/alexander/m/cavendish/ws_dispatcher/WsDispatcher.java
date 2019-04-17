package alexander.m.cavendish.ws_dispatcher;

import java.io.IOException;

import alexander.m.cavendish.WsListener;
import okhttp3.Response;

/**
 * @Author:mashijie
 * @Date:2019/4/15
 * @Description
 * @Email:alexander.m.cavendish@gmail.com
 */
public final class WsDispatcher implements WsListener {

    private OnMsgListener mOnMsgListener;
    private OnSignalEventListener mOnSignalEventListener;
    private OnWsStateListener mOnWsStateListener;

    public void cancel() {
        if (null != mOnMsgListener) {
            mOnMsgListener = null;
        }
        if (null != mOnSignalEventListener) {
            mOnSignalEventListener = null;
        }
        if (null != mOnWsStateListener) {
            mOnWsStateListener = null;
        }
    }

    private WsDispatcher() {

    }


    @Override
    public void onWsOpen() {
        if (null != mOnWsStateListener) {
            mOnWsStateListener.onWsOpen();
        }
    }

    @Override
    public void onWsReconnecting() {
        if (null != mOnWsStateListener) {
            mOnWsStateListener.onWsReconnecting();
        }
    }


    @Override
    public void onWsFail(IOException e, Response response) {
        if (null != mOnWsStateListener) {
            mOnWsStateListener.onWsFail(e, response);
        }
    }

    @Override
    public void onWsClosed(int code, String reason) {
        if (null != mOnWsStateListener) {
            mOnWsStateListener.onWsClosed(code, reason);
        }
    }

    @Override
    public void onWsMessage(String msg) {
        if (null != mOnMsgListener && Judger.newJudgerInstance().judgeMessage(msg)) {
            mOnMsgListener.onWsMessage(msg);
        }
    }

    public static class Builder {
        private WsDispatcher wsDispatcher;

        public Builder() {
            wsDispatcher = new WsDispatcher();
        }

        public Builder msgListener(OnMsgListener onMsgListener) {
            wsDispatcher.mOnMsgListener = onMsgListener;
            return this;
        }

        public Builder signalEventListener(OnSignalEventListener onSignalEventListener) {
            wsDispatcher.mOnSignalEventListener = onSignalEventListener;
            return this;
        }

        public Builder wsStateListener(OnWsStateListener onWsStateListener) {
            wsDispatcher.mOnWsStateListener = onWsStateListener;
            return this;
        }

        public WsDispatcher build() {
            return wsDispatcher;
        }
    }
}
