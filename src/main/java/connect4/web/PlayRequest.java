package connect4.web;

public class PlayRequest extends RecommendRequest {

	private static final long serialVersionUID = 1L;

	private Integer column;

	public Integer getColumn() {
		return column;
	}

	public void setColumn(final Integer column) {
		this.column = column;
	}
}
