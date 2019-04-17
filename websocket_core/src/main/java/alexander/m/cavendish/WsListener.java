package alexander.m.cavendish;


import java.io.IOException;

import okhttp3.Response;

/**
 * @Author:mashijie
 * @Date:2019/4/15
 * @Description socket状态listener
 * @Email:alexander.m.cavendish@gmail.com
 */
public interface WsListener {
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

    /**
     * ws收到的消息
     *
     * @param msg
     */
    void onWsMessage(String msg);
}
