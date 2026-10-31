import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

function BoardPage() {
    const [articles, setArticles] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        let ignore = false

        async function loadArticles() {
            try {
                const response = await fetch('/api/articles?page=1&size=10')

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

        return () => {
            ignore = true
        }
    }, [])

    return (
        <main className="board-page">
            <Link to="/">홈으로</Link>

            <h1>게시판</h1>

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
                                {/* 첫 번째 칸: 게시글 번호 */}
                                <td>{article.id}</td>

                                {/* 두 번째 칸: 카테고리 */}
                                <td>
                                    {article.category === 'FREE'
                                        ? '자유'
                                        : article.category === 'QUESTION'
                                            ? '질문'
                                            : article.category}
                                </td>

                                {/* 세 번째 칸: 제목을 클릭하면 상세 화면 이동 */}
                                <td>
                                    <Link to={`/board/${article.id}`}>
                                        {article.title}
                                    </Link>
                                </td>

                                {/* 네 번째 칸: 작성자 */}
                                <td>{article.authorNickname}</td>

                                {/* 다섯 번째 칸: 조회수 */}
                                <td>{article.viewCount}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </main>
    )
}

export default BoardPage