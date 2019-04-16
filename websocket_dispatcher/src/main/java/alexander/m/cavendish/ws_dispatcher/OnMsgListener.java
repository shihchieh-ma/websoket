package alexander.m.cavendish.ws_dispatcher;

/**
 * @Author:mashijie
 * @Date:2019/4/16
 * @Description
 * @Email:alexander.m.cavendish@gmail.com
 */
public interface OnMsgListener {
    /**
     * ws收到的消息
     *
     * @param msg
     */
    void onWsMessage(String msg);
}
