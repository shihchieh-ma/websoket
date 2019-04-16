package alexander.m.cavendish.ws_dispatcher;

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
     *
     * @param errorInfo
     */
    void onWsFail(String errorInfo);


    /**
     * ws关闭中
     *
     * @param code
     * @param reason
     */
    void onWsClosing(int code, String reason);

    /**
     * ws连接关闭
     *
     * @param code
     * @param reason
     */
    void onWsClosed(int code, String reason);

}
