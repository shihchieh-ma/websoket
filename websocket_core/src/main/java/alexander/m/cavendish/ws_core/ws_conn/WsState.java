package alexander.m.cavendish.ws_core.ws_conn;

/**
 * @Author:mashijie
 * @Date:2019/4/16
 * @Description
 * @Email:alexander.m.cavendish@gmail.com
 */
public class WsState {
    //初始默认状态,未连接
    public static final int NORMAL = 0x001;
    //正在连接
    public static final int CONNECTING = 0x002;
    //连接成功
    public static final int CONNECTED = 0x003;
    //关闭中
    public static final int CLOSING = 0x004;
    //已关闭
    public static final int CLOSED = 0x005;
    //连接失败
    public static final int FAILED = 0x006;
    //队列准备重连
    public static final int RECONN_IN_QUEUE = 0x007;
}
