package alexander.m.cavendish.ws_dispatcher;

import java.io.IOException;

import okhttp3.Response;

/**
 * @Author:mashijie
 * @Date:2019/4/16
 * @Description
 * @Email:alexander.m.cavendish@gmail.com
 */
public interface OnWsStateListener {
    /**
     * ws连接成功
     */
    void onWsOpen();

    /**
     * ws重连中
     */
    void onWsReconnecting();

    /**
     * ws连接失败
     * @param e
     * @param response
     */
    void onWsFail(IOException e, Response response);

    /**
     * ws连接关闭
     *
     * @param code
     * @param reason
     */
    void onWsClosed(int code, String reason);

}
