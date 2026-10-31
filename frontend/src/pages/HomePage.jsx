import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import '../App.css'

function HomePage() {
    // 화면에 표시할 게시글 목록
    const [articles, setArticles] = useState([])

    // 처음 화면에 들어오면 바로 조회하므로 true로 시작
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    // 홈 화면이 나타나면 게시글 자동 조회
    useEffect(() => {
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

        // 화면을 벗어난 뒤에는 조회 결과를 반영하지 않음
        return () => {
            ignore = true
        }
    }, [])

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
                            <Link to="/board">더 보기</Link>
                        </div>

                        {loading && <p>게시글을 불러오는 중입니다.</p>}

                        {error && <p role="alert">{error}</p>}

                        {!loading && !error && articles.length === 0 && (
                            <p>등록된 게시글이 없습니다.</p>
                        )}

                        {!loading && !error && articles.length > 0 && (
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
                        )}
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

export default HomePage