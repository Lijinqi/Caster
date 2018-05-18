package east.orientation.caster.local.Bean;

/**
 * Created by ljq on 2018/5/3.
 */

public class DirBean {
    // 0 外部存储 1 视频 2 音乐 3 图片 4 文档
    private int id;
    private String name;
    private boolean isSelected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
