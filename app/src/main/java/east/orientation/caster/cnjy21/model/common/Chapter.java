package east.orientation.caster.cnjy21.model.common;

import java.util.List;

/**
 * 教材章节信息
 * 
 * @author zhoushubing
 *
 */
public class Chapter {
	/**
	 * 教材的章id 或 教材的节ID.
	 */
	private Integer id;
	/**
	 * 教材的章名称或教材的节名称.
	 */
	private String name;
	/**
	 * 学段ID.
	 */
	private String stage;
	/**
	 * 学科ID.
	 */
	private Integer subjectId;
	/**
	 * 父级ID.
	 */
	private Integer upid;
	/**
	 * 子节点数据集格式字段与父节点一致，客户端调用该节点递归.
	 */
	private List<Chapter> childs;

	/**
	 * 所有父级关系路径.
	 */
	private String parentpath;

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

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}

	public Integer getUpid() {
		return upid;
	}

	public void setUpid(Integer upid) {
		this.upid = upid;
	}

	public List<Chapter> getChilds() {
		return childs;
	}

	public void setChilds(List<Chapter> childs) {
		this.childs = childs;
	}

	public String getParentpath() {
		return parentpath;
	}

	public void setParentpath(String parentpath) {
		this.parentpath = parentpath;
	}

}
