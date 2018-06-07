package east.orientation.caster.cnjy21.model.common;

/**
 * 教材册别信息
 * 
 * @author zhoushubing
 *
 */
public class Book {
	/**
	 * 册别ID
	 */
	private Integer bookId;

	/**
	 * 测别名称
	 */
	private String bookName;

	/**
	 * 是否选中
	 */
	private boolean isSelected;

	public Integer getBookId() {
		return bookId;
	}

	public void setBookId(Integer bookId) {
		this.bookId = bookId;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}
}
