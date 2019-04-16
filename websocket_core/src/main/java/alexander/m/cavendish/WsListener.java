package alexander.m.cavendish;


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

    /**
     * ws收到的消息
     *
     * @param msg
     */
    void onWsMessage(String msg);
}
