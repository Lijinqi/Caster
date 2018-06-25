package east.orientation.caster.sync;

/**
 * Created by ljq on 2018/6/5.
 *
 * 同步 - 消息传递
 */

public class SyncSender {

    /**
     * 登录命令
     *
     * @param account 账号
     *
     * @param password 密码
     *
     * @return 发送字符串
     */
    public static String login(String account,String password){
        String action = String.format("Orntcmd=login,data=%s,%s", account, password);
        return action;
    }

    /**
     * 上传命令
     *
     * @param fileName 文件名
     *
     * @return 发送字符串
     */
    public static String fileup(String fileName){
        String action = String.format("Orntcmd=fileup,data=A_%s,",fileName);
        return action;
    }

    /**
     * 查询命令 已下载文件无 A_/P_ 前缀
     *
     * @return
     */
    public static String filequery(){
        String action = "Orntcmd=filequery";
        return action;
    }

    /**
     * 下载命令
     *
     * @param fileName 文件名
     *
     * @return
     */
    public static String filedown(String fileName){
        String action = String.format("Orntcmd=filedown,data=%s",fileName);
        return action;
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     *
     * @return
     */
    public static String filedel(String fileName){
        String action = String.format("Orntcmd=filedel,data=A_%s",fileName);
        return action;
    }

    /**
     * 查询命令 查询新的文件
     *
     * @return
     */
    public static String filequery_syn(){
        String action = "Orntcmd=filequery_syn,data=A";
        return action;
    }

    /**
     * 删除消息
     *
     * @param fileName 文件名
     *
     * @return
     */
    public static String filedel_syn(String fileName){
        String action = String.format("Orntcmd=filedel_syn,data=P_Del_%s",fileName);
        return action;
    }

    /**
     * 下载成功回复
     *
     * @param fileName 文件名
     *
     * @return
     */
    public static String fileupdated_syn(String fileName){
        String action = String.format("Orntcmd=fileupdated_syn,data=P_%s",fileName);
        return action;
    }
}
