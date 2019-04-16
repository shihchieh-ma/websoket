package alexander.m.cavendish.ws_dispatcher;

import alexander.m.cavendish.WsListener;

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
    public void onWsFail(String errorInfo) {
        if (null != mOnWsStateListener) {
            mOnWsStateListener.onWsFail(errorInfo);
        }
    }

    @Override
    public void onWsClosing(int code, String reason) {
        if (null != mOnWsStateListener) {
            mOnWsStateListener.onWsClosing(code, reason);
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
