package mvc.vo;

public class SearchCriteria extends Criteria {
	
	private String searchType; // 검색 선택
	private String keyword;    // 검색어
	
	public String getSearchType() {
		return searchType;
	}
	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
}
