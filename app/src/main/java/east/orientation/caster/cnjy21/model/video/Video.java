package east.orientation.caster.cnjy21.model.video;

/**
 * 视频资源信息
 * 
 * @author zhoushubing
 *
 */
public class Video {
	/**
	 * 视频ID
	 */
	private Long id;
	/**
	 * 学段ID.
	 */
	private Integer stage;
	/**
	 * 学科ID.
	 */
	private Integer subjectId;
	/**
	 * 视频章节ID.
	 */
	private Integer chapterId;
	/**
	 * 视频知识点ID.
	 */
	private Integer knowledgeId;
	/**
	 * 视频标题.
	 */
	private String title;
	/**
	 * 视频文件大小(单位 byte)
	 */
	private Long filesize;
	/**
	 * 作者
	 */
	private String author;
	/**
	 * 视频分类（other、其他视频；kt5u、课堂无忧微视频；free、名师免费视频）
	 */
	private String videoType;
	/**
	 * 创建时间
	 */
	private Long createdtime;
	/**
	 * 最后更新时间
	 */
	private Long updatedtime;
	/**
	 * 视频封面图片
	 */
	private String img;
	/**
	 * 作者ID
	 */
	private Integer authorid;

	/**
	 * 视频下载地址(异步加载)
	 */
	private String mp4_url;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Integer getChapterId() {
		return chapterId;
	}

	public void setChapterId(Integer chapterId) {
		this.chapterId = chapterId;
	}

	public Integer getKnowledgeId() {
		return knowledgeId;
	}

	public void setKnowledgeId(Integer knowledgeId) {
		this.knowledgeId = knowledgeId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getFilesize() {
		return filesize;
	}

	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getVideoType() {
		return videoType;
	}

	public void setVideoType(String videoType) {
		this.videoType = videoType;
	}

	public Long getCreatedtime() {
		return createdtime;
	}

	public void setCreatedtime(Long createdtime) {
		this.createdtime = createdtime;
	}

	public Long getUpdatedtime() {
		return updatedtime;
	}

	public void setUpdatedtime(Long updatedtime) {
		this.updatedtime = updatedtime;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public Integer getAuthorid() {
		return authorid;
	}

	public void setAuthorid(Integer authorid) {
		this.authorid = authorid;
	}

	public String getMp4_url() {
		return mp4_url;
	}

	public void setMp4_url(String mp4_url) {
		this.mp4_url = mp4_url;
	}

}
