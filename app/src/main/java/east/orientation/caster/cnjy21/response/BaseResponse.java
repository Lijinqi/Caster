package east.orientation.caster.cnjy21.response;

/**
 * Created by ljq on 2018/4/25.
 */

public class BaseResponse<T> {
    private int code;

    private String msg;

    private T data;

    private Page page;

    public static class Page {
        // 总数量
        private Integer count;
        // 每页大小
        private Integer size;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }
}
