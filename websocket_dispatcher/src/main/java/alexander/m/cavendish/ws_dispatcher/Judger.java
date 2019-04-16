package alexander.m.cavendish.ws_dispatcher;

/**
 * @Author:mashijie
 * @Date:2019/4/16
 * @Description 判断消息是正常消息还是信令
 * @Email:alexander.m.cavendish@gmail.com
 */
public class Judger {
    private static Judger judger;

    private Judger() {
    }

    public void clear() {
        if (null != judger) {
            judger = null;
        }
    }

    public static Judger newJudgerInstance() {
        if (null == judger) {
            judger = new Judger();
        }
        return judger;
    }

    public boolean judgeMessage(String msg) {
        //约定信令判断,若是信令返回false,onsignaleventlistener回调接口即可
        return true;
    }
}
