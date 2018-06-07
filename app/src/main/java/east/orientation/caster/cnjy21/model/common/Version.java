package east.orientation.caster.cnjy21.model.common;

/**
 * 教材版本
 * 
 * @author zhoushubing
 *
 */
public class Version {
	/**
	 * 版本ID
	 */
	private Integer versionId;

	/**
	 * 版本名称
	 */
	private String versionName;

	/**
	 * 学段ID
	 */
	private Integer stage;

	/**
	 * 科目ID
	 */
	private Integer subjectId;

	/**
	 * 是否选中
	 */
	private boolean isSelected;

	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Integer getStage() {
		return stage;
	}

	public void setStage(Integer stage) {
		this.stage = stage;
	}

	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}
}
