package east.orientation.caster.cnjy21.constant;

/**
 * API 常量配置
 * 
 * @author zhoushubing
 *
 */
public interface APIConstant {
	/**
	 * 接口域名
	 */
	public String DOMAIN = "http://dev.21cnjy.com/";
	/**
	 * 认证签名KEY
	 */
	public String ACCESS_KEY = "21cnjy9EgnOBIqdfsjwLJZ";
	/**
	 * 认证签名密钥
	 */
	public String ACCESS_SECRET = "y7dVOsdKU05sBAgawPtQHTy3PzJsJgsi";

	/**
	 * 获取省份地区信息
	 */
	public String URL_PROVINCES = DOMAIN + "api/v3/common/provinces";

	/**
	 * 获取学段下科目列表地址
	 */
	public String URL_SUBJECTS = DOMAIN + "api/v3/common/subjects";

	/**
	 * 获取科目下版本列表地址
	 */
	public String URL_VERSIONS = DOMAIN + "api/v3/common/versions";

	/**
	 * 获取版本下册别列表地址
	 */
	public String URL_BOOKS = DOMAIN + "api/v3/common/books";

	/**
	 * 获取册别下章节列表地址
	 */
	public String URL_CHAPTERS = DOMAIN + "api/v3/common/chapters";

	/**
	 * 获取章节下知识点列表地址
	 */
	public String URL_KNOWLEDGEPOINTS = DOMAIN + "api/v3/common/knowledge";

	/**
	 * 获取章节下的资源列表地址
	 */
	public String URL_DOCUMENTS = DOMAIN + "api/v3/document/items";

	/**
	 * 获取资源下载地址
	 */
	public String URL_DOCUMENT_DETAIL = DOMAIN + "api/v3/document/item-details";

	/**
	 * 获取文档预览信息
	 */
	public String URL_DOCUMENT_PREVIEW = DOMAIN + "api/v3/document/preview";

	/**
	 * 获取教材章节下试题列表
	 */
	public String URL_QUESTIONS = DOMAIN + "api/v3/questions/index";

	/**
	 * 获取试题类型列表
	 */
	public String URL_QUESTION_TYPES = DOMAIN + "api/v3/questions/types";

	/**
	 * 获取试题答案和解析
	 */
	public String URL_QUESTION_DETAIL = DOMAIN + "api/v3/questions/details";

	/**
	 * 客户端请求信息中签名的参数名称
	 */
	public String PARAM_SIGN = "sign";

	/**
	 * 客户端请求信息中 AccessKey 的参数名称
	 */
	public String PARAM_ACCESS_KEY = "access-key";

	/**
	 * 客户端请求信息中 时间戳 的参数名称
	 */
	public String PARAM_TIMESTAMP = "timestamp";

	/**
	 * 客户端请求中 参与签名的随机字符 对应的参数名称
	 */
	public String PARAM_SALT = "salt";

	/**
	 * 学段-小学
	 */
	public int STAGE_XIAOXUE = 1;
	/**
	 * 学段-初中
	 */
	public int STAGE_CHUZHONG = 2;
	/**
	 * 学段-高中
	 */
	public int STAGE_GAOZHONG = 3;

	/**
	 * 文档资源类型-课件
	 */
	public int TYPE_DOCUMENT_KEJIAN = 3;

	/**
	 * 文档资源类型-学案
	 */
	public int TYPE_DOCUMENT_XUEAN = 4;

	/**
	 * 文档资源类型-教案
	 */
	public int TYPE_DOCUMENT_JIAOAN = 8;

	/**
	 * 文档资源类型-试卷
	 */
	public int TYPE_DOCUMENT_SHIJUAN = 7;

	/**
	 * 文档资源类型-素材
	 */
	public int TYPE_DOCUMENT_SUCAI = 6;

	/**
	 * 文档资源类型-视频
	 */
	public int TYPE_DOCUMENT_SHIPING = 12;

	/**
	 * 容易
	 */
	public int DIFFICLUT_EASY = 1;

	/**
	 * 普通
	 */
	public int DIFFICULT_NORMAL = 3;

	/**
	 * 困难
	 */
	public int DIFFICULT_MOST = 5;

}
