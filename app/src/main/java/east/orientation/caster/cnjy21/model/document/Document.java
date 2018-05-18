package east.orientation.caster.cnjy21.model.document;

import java.util.List;

import east.orientation.caster.cnjy21.model.common.Chapter;


/**
 * 文档资源信息
 * 
 * @author zhoushubing
 *
 */
public class Document {
	/**
	 * 资源ID
	 */
	private Long itemId;

	/**
	 * 
	 * 学段ID, 1:小学，2:初中，3:高中.
	 */
	private Integer stage;

	/**
	 * 
	 * 所属学科ID.
	 */
	private Integer subjectId;

	private String subjectName;

	/**
	 * 资源类型 说明：[3 = '课件', 8 = '教案', 7 = '试卷', 4 = '学案',11= '资源包',6 = '素材',12 =
	 * '视频']
	 */
	private Integer type;

	/**
	 * 资源类型名称
	 */
	private String typeName;

	/**
	 * 所属地区ID
	 */
	private Integer cityId;

	/**
	 * 所属地区名称
	 */
	private String cityName;

	/**
	 * 章节路径，更当前资源关联的所有章节
	 */
	private List<Chapter> chapterPath;

	/**
	 * 资源标题
	 */
	private String title;

	/**
	 * 资源简介
	 */
	private String intro;

	/**
	 * 资源主题.
	 */
	private String subject;

	/**
	 * 所属章节ID.
	 * 
	 */
	private Integer chapterId;
	/**
	 * 所属章节ID.
	 */
	private Integer catidroot;

	/**
	 * 上传时间.
	 */
	private Long dateline;
	/**
	 * 文件大小.
	 */
	private Long fileSize;
	/**
	 * 资料来源.
	 */
	private String softintro;
	/**
	 * 
	 * 资料类型(3:课件,4:学案,6:素材,7:试卷,8:教案).
	 */
	private Integer documentType;

	/**
	 * 下载地址
	 */
	private String downloadUrl;

	private String parentpath;

	private Integer versionid;

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}

	public Integer getChapterId() {
		return chapterId;
	}

	public void setChapterId(Integer chapterId) {
		this.chapterId = chapterId;
	}

	public Integer getCatidroot() {
		return catidroot;
	}

	public void setCatidroot(Integer catidroot) {
		this.catidroot = catidroot;
	}

	public Long getDateline() {
		return dateline;
	}

	public void setDateline(Long dateline) {
		this.dateline = dateline;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getSoftintro() {
		return softintro;
	}

	public void setSoftintro(String softintro) {
		this.softintro = softintro;
	}

	public Integer getDocumentType() {
		return documentType;
	}

	public void setDocumentType(Integer documentType) {
		this.documentType = documentType;
	}

	public Integer getStage() {
		return stage;
	}

	public void setStage(Integer stage) {
		this.stage = stage;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getParentpath() {
		return parentpath;
	}

	public void setParentpath(String parentpath) {
		this.parentpath = parentpath;
	}

	public Integer getVersionid() {
		return versionid;
	}

	public void setVersionid(Integer versionid) {
		this.versionid = versionid;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Integer getCityId() {
		return cityId;
	}

	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public List<Chapter> getChapterPath() {
		return chapterPath;
	}

	public void setChapterPath(List<Chapter> chapterPath) {
		this.chapterPath = chapterPath;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

}
