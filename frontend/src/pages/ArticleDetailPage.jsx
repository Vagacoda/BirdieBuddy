import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'

function ArticleDetailPage() {
    // /board/6에서 게시글 번호 6을 가져옴
    const { articleId } = useParams()

    // 어떤 게시글의 조회 결과인지 함께 저장
    const [result, setResult] = useState(null)

    useEffect(() => {
        let ignore = false

        async function loadArticle() {
            try {
                const response = await fetch(`/api/articles/${articleId}`)

                if (response.status === 404) {
                    throw new Error('존재하지 않거나 삭제된 게시글입니다.')
                }

                if (!response.ok) {
                    throw new Error('게시글을 불러오지 못했습니다.')
                }

                const data = await response.json()

                if (!ignore) {
                    setResult({
                        articleId,
                        article: data,
                        error: '',
                    })
                }
            } catch (error) {
                if (!ignore) {
                    setResult({
                        articleId,
                        article: null,
                        error: error.message,
                    })
                }
            }
        }

        loadArticle()

        return () => {
            ignore = true
        }
    }, [articleId])

    // 현재 주소에 해당하는 조회 결과가 아직 없으면 로딩 중
    const loading = result?.articleId !== articleId
    const article = loading ? null : result.article
    const error = loading ? '' : result.error

    return (
        <main className="article-detail-page">
            <nav>
                <Link to="/">홈으로</Link>
                {' / '}
                <Link to="/board">게시판 목록으로</Link>
            </nav>

            {loading && <p>게시글을 불러오는 중입니다.</p>}

            {error && <p role="alert">{error}</p>}

            {article && (
                <article>
                    <header>
                        <p>
                            {article.category === 'FREE'
                                ? '자유'
                                : article.category === 'QUESTION'
                                    ? '질문'
                                    : article.category}
                        </p>

                        <h1>{article.title}</h1>

                        <p>
                            작성자: {article.authorNickname}
                            {' · '}
                            조회수: {article.viewCount}
                        </p>

                        <p>
                            작성일: {article.createdAt.replace('T', ' ')}
                        </p>
                    </header>

                    <hr />

                    {/* 작성할 때 입력한 줄바꿈을 화면에서도 유지 */}
                    <div style={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }}>
                        {article.content}
                    </div>
                </article>
            )}
        </main>
    )
}

export default ArticleDetailPage