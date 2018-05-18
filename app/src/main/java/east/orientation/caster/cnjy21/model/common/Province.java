package east.orientation.caster.cnjy21.model.common;

/**
 * 省份地区信息
 * 
 * @author zhoushubing
 *
 */
public class Province {

	/**
	 * 地区ID.
	 */
	private Integer id;
	/**
	 * 地区名称.
	 */
	private String name;
	/**
	 * 父级ID.
	 */
	private Integer upid;
	/**
	 * 地区级别 (1:省、2:市、3:区、4:街道/镇/乡)
	 */
	private Integer level;
	/**
	 * 地区名称拼音
	 */
	private String pinyin;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getUpid() {
		return upid;
	}

	public void setUpid(Integer upid) {
		this.upid = upid;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

}
