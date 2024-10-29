package mvc.vo;

//페이지 하단에 페이징 네비게이션에 필요한 변수들을 담아놓은 클래스
public class PageMaker {

	private int displayPageNum = 10; // 페이지 목록 번호 리스트  ex) 1 2 3 4 5 6 7 8 9 10
	private int startPage;           // 목록의 시작 번호를 담는 변수 
	private int endPage;             // 목록의 끝 번호를 담는 변수
	private int totalCount;          // 총 게시물 수를 담는 변수
	
	private boolean prev;           // 이전 버튼
	private boolean next;           // 다음 버튼
	
	private SearchCriteria scri;

	public SearchCriteria getScri() {
		return scri;
	}

	public void setScri(SearchCriteria scri) {
		this.scri = scri;
	}

	public int getDisplayPageNum() {
		return displayPageNum;
	}

	public void setDisplayPageNum(int displayPageNum) {
		this.displayPageNum = displayPageNum;
	}

	public int getStartPage() {
		return startPage;
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	public int getEndPage() {
		return endPage;
	}

	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) { // 총 게시물이 몇 개인지 받는 메소드
		this.totalCount = totalCount;
		calcDate(); // 페이지 목록 리스트 번호를 나타내주기 위한 계산식
	}

	public boolean isPrev() {
		return prev;
	}

	public void setPrev(boolean prev) {
		this.prev = prev;
	}

	public boolean isNext() {
		return next;
	}

	public void setNext(boolean next) {
		this.next = next;
	}
	
	private void calcDate() {
		// 1. 기본적으로 1에서부터 10까지 나타나게 설정한다 (페이지 네비게이션에서)
		endPage = (int)(Math.ceil(scri.getPage() / (double)displayPageNum) * displayPageNum); // 모두 올림 처리하는 메소드 ceil()
		
		// 2. endPage가 설정 되었으면 시작 페이지도 설정
		startPage = (endPage - displayPageNum) + 1;
		
		// 3. 실제 게시물 수에 따라서 endPage를 구하겠다
		int tempEndPage	= (int)(Math.ceil(totalCount / (double)scri.getPerPageNum()));
		
		// 4. 설정한 endPage와 실제 endPage를 비교 해서 최종 endPage를 구한다 
		if (endPage > tempEndPage) {
			endPage = tempEndPage;
		}
		
		// 5. 이전, 다음 버튼 만들기
		prev = (startPage == 1 ? false : true); // 삼항연산자 사용
		next = (endPage * scri.getPerPageNum() > totalCount ? false : true);
		
	}
}
