# (개인프로젝트)Onion 게시판

### 1일차 
- User 회원 가입 기능
- User 회원 탈퇴 기능
- User 전체 조회 기능

### 2일차
- Swagger-ui 적용
- JWT 로그인
- JWT 로그인 쿠키에 토큰 넣기 기능
- 로그아웃

### 3일차
- JwtBlackList 기능
- 예외처리 기능
- 게시글 작성 기능
  - 보안 건텍스트 활용 본인 작성한 글만 작성
  - 5분에 1번만 작성
- 게시글 수정 기능
  - 보안 건텍스트 활용 본인 작성한 글만 수정
  - 수정헀던 글은 계속 수정 다른 게시글은 5분에 1번만 수정

### 4일차
- 게시글 삭제 기능
  - 보안 건텍스트 활용 본인 작성한 글만 삭제
  - 논리삭제-DB에서 실제로 삭제하지 않고 유저에게만 안보이도록 하는 방식 (반대는 물리삭제)
- 특정 유저 조회
- 댓글 작성 기능
  - 게시판, 게시글이 존재하고 삭제되지 않았는지를 확인하고 댓글이 작성(삭제가 은근 자주 일어나서 트랙잭션 적용 동시성 확보)
    - 트랙잭션 적용
  - 댓글 1분에 1번만 작성
- 댓글 조회 기능
  - 댓글만 보는 경우는 없기 때문에 게시글 조회시 댓글도 같이 제공
  - connection pool활용
    - board, article까진 select / join으로 조회
    - 0.1초라도 빨리 보여주기 위해 comment 테이블은 별도로 조회

### 5일차
- 댓글 수정 기능
  - 보안 컨텍스트 활용
  - 1분에 1번만 수정 기능
- 댓글 삭제 기능
  - 보안 컨텍스트 활용
  - 1분에 1번만 삭제 기능

### 6일차
- 게시글 조회수 기능
- article Elasticsearch 검색 기능
- Kibana 모니터링 적용

### 7일차
- 광고 노출 기능
  - 광고 생성 기능
    - redis 저장 및 mysql 저장
  - 특정 광고 조회 기능
    - redis에서 확인 후 조회 없으면 mysql 조회후 redis 저장
- 집계 기능
  - logstash

### 8일차
- 알람 기능(클라이언트가 있다는 가정하에)
  - 토큰 및 기기 생성등록 및 조회 기능 
  - 글 작성시 알림 기능(printf로만 오는지 확인)

### 9일차
- 알람 기능
  - 댓글 작성시 알림 기능
  - 알림 확인 기능

