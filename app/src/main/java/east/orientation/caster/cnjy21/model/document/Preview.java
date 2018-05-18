package east.orientation.caster.cnjy21.model.document;

import java.util.List;

/**
 * 资源预览对象
 * 
 * @author zhoushubing
 *
 */
public class Preview {

	/**
	 * 资料ID
	 */
	private String itemId;

	/**
	 * 预览文件数据集
	 */
	private List<PreviewFile> previewFiles;

	/**
	 * 子文件
	 */
	private List<Preview> subsets;

	/**
	 * 文件类型
	 */
	private String fileType;

	/**
	 * 标题
	 */
	private String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public List<Preview> getSubsets() {
		return subsets;
	}

	public void setSubsets(List<Preview> subsets) {
		this.subsets = subsets;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public List<PreviewFile> getPreviewFiles() {
		return previewFiles;
	}

	public void setPreviewFiles(List<PreviewFile> previewFiles) {
		this.previewFiles = previewFiles;
	}

	public static class PreviewFile {
		/**
		 * 
		 * 预览文件URL地址
		 */
		private String fileUrl;
		/**
		 * 预览文件类型(后缀) 如：.mp3, .flv, .swf, .ppt 等等.
		 */
		private String fileType;

		public String getFileUrl() {
			return fileUrl;
		}

		public void setFileUrl(String fileUrl) {
			this.fileUrl = fileUrl;
		}

		public String getFileType() {
			return fileType;
		}

		public void setFileType(String fileType) {
			this.fileType = fileType;
		}
	}
}
