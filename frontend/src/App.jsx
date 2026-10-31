import { useEffect, useState } from 'react'
import './App.css'

function App() {
  // 화면에 표시할 게시글 목록
  const [articles, setArticles] = useState([])

  // 조회 진행 상태와 오류 메시지
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // 조회를 완료했는지 구분
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    // 화면을 벗어난 뒤에는 조회 결과를 반영하지 않도록 구분
    let ignore = false

    async function loadArticles() {
      try {
        const response = await fetch('/api/articles')

        if (!response.ok) {
          throw new Error('게시글 목록을 불러오지 못했습니다.')
        }

        const data = await response.json()

        if (!ignore) {
          setArticles(data.articles)
          setLoaded(true)
        }
      } catch (error) {
        if (!ignore) {
          setError(error.message)
        }
      } finally {
        if (!ignore) {
          setLoading(false)
        }
      }
    }

    loadArticles()

    return () => {
      ignore = true
    }
  }, [])

  async function loadArticles() {
    setLoading(true)
    setError('')

    try {
      // Vite 프록시를 통해 Spring Boot에 목록 요청
      const response = await fetch('/api/articles')

      if (!response.ok) {
        throw new Error('게시글 목록을 불러오지 못했습니다.')
      }

      // 응답 JSON을 JavaScript 객체로 변환
      const data = await response.json()

      // 응답의 articles 목록을 화면 상태에 저장
      setArticles(data.articles)
      setLoaded(true)
    } catch (error) {
      setError(error.message)
    } finally {
      // 성공하거나 실패해도 로딩 종료
      setLoading(false)
    }
  }

  return (
      <div className="home">
        {/* 홈페이지 상단 */}
        <header className="site-header">
          <h1>BirdieBuddy</h1>
        </header>

        <main className="home-content">
          {/* 첫 번째 줄: 게시판 + 유저 대시보드 */}
          <div className="home-top">
            <section className="panel board-panel">
              <div className="panel-heading">
                <h2>게시판</h2>

                <button onClick={loadArticles} disabled={loading}>
                  {loading ? '불러오는 중...' : '게시글 불러오기'}
                </button>
              </div>

              {error && <p role="alert">{error}</p>}

              {!loaded && !loading && !error && (
                  <p>버튼을 눌러 게시글을 확인해 주세요.</p>
              )}

              {loaded && !loading && !error && articles.length === 0 && (
                  <p>등록된 게시글이 없습니다.</p>
              )}

              <div className="table-container">
                <table className="article-table">
                  <thead>
                  <tr>
                    <th>번호</th>
                    <th>카테고리</th>
                    <th>제목</th>
                    <th>작성자</th>
                    <th>조회수</th>
                  </tr>
                  </thead>

                  <tbody>
                  {articles.map((article) => (
                      <tr key={article.id}>
                        <td>{article.id}</td>
                        <td>
                          {article.category === 'FREE'
                              ? '자유'
                              : article.category === 'QUESTION'
                                  ? '질문'
                                  : article.category}
                        </td>
                        <td>{article.title}</td>
                        <td>{article.authorNickname}</td>
                        <td>{article.viewCount}</td>
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>
            </section>

            <aside className="panel user-panel">
              <h2>유저 대시보드</h2>
              <p>회원 정보 영역을 준비 중입니다.</p>
            </aside>
          </div>

          {/* 두 번째 줄: 지도 + 골프장 목록 */}
          <section className="panel golf-panel">
            <div className="map-area">
              <h2>지도</h2>
              <p>지도 서비스 준비 중</p>
            </div>

            <div className="course-area">
              <h2>골프장 목록</h2>
              <p>골프장 정보를 준비 중입니다.</p>
            </div>
          </section>

          {/* 세 번째 줄: 뉴스 + 유튜브 */}
          <div className="home-bottom">
            <section className="panel news-panel">
              <h2>뉴스</h2>
              <p>골프 뉴스를 준비 중입니다.</p>
            </section>

            <section className="panel youtube-panel">
              <h2>유튜브</h2>
              <p>골프 영상을 준비 중입니다.</p>
            </section>
          </div>
        </main>
      </div>
  )
}

export default App