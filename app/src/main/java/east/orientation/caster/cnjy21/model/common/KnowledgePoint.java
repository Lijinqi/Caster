package east.orientation.caster.cnjy21.model.common;

import java.util.List;

/**
 * 教材知识点信息
 * 
 * @author zhoushubing
 *
 */
public class KnowledgePoint {
	/**
	 * 知识点ID.
	 */
	private Integer id;
	/**
	 * 父级知识点ID.
	 */
	private Integer upid;
	/**
	 * 知识点名称.
	 */
	private String name;

	/**
	 * 所属科目ID.
	 */
	private Integer subjectId;

	/**
	 * 所属学段.
	 */
	private Integer stage;

	/**
	 * 子节点数据集
	 */
	private List<KnowledgePoint> childs;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUpid() {
		return upid;
	}

	public void setUpid(Integer upid) {
		this.upid = upid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}

	public Integer getStage() {
		return stage;
	}

	public void setStage(Integer stage) {
		this.stage = stage;
	}

	public List<KnowledgePoint> getChilds() {
		return childs;
	}

	public void setChilds(List<KnowledgePoint> childs) {
		this.childs = childs;
	}

}
