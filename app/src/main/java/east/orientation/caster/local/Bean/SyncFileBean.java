package east.orientation.caster.local.Bean;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by ljq on 2018/5/4.
 */

public class SyncFileBean {
    private String name;
    private long length;
    private String time;
    private String path;
    private boolean isDownLoad;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLength() {
        return FormatFileSize(length);
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getTime() {

        DateFormat simpleDateFormat = SimpleDateFormat.getDateTimeInstance();//new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long lt = Long.valueOf(time);

        Date date = new Date(lt * 1000);
        return simpleDateFormat.format(date);

    }

    public void setTime(String time) {
        this.time = time;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     */
    public String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDownLoad() {
        return isDownLoad;
    }

    public void setDownLoad(boolean downLoad) {
        isDownLoad = downLoad;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
