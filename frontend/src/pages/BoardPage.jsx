import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import '../App.css'

function BoardPage() {
    // 서버에서 받은 게시글과 페이지 정보
    const [articles, setArticles] = useState([])
    const [totalElements, setTotalElements] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [hasNext, setHasNext] = useState(false)

    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    // 실제 조회에 사용할 조건
    // 빈 문자열은 전체 카테고리를 의미
    const [category, setCategory] = useState('')
    const [page, setPage] = useState(1)
    const [search, setSearch] = useState({
        type: 'TITLE',
        keyword: '',
    })

    // 검색창에서 편집 중인 값
    // 실제 검색 조건과 분리해서, 타이핑할 때마다 요청하지 않음
    const [inputSearchType, setInputSearchType] = useState('TITLE')
    const [inputKeyword, setInputKeyword] = useState('')

    // 한 페이지에 표시할 게시글 수
    const size = 10

    useEffect(() => {
        let ignore = false

        async function loadArticles() {
            setLoading(true)
            setError('')

            // API 주소에 붙일 조회 조건 만들기
            const params = new URLSearchParams({
                page: String(page),
                size: String(size),
            })

            // 전체 카테고리라면 category를 보내지 않음
            if (category) {
                params.set('category', category)
            }

            // 검색어가 있을 때만 검색 조건 추가
            if (search.keyword) {
                params.set('searchType', search.type)
                params.set('keyword', search.keyword)
            }

            try {
                const response = await fetch(`/api/articles?${params}`)

                if (!response.ok) {
                    throw new Error('게시글 목록을 불러오지 못했습니다.')
                }

                const data = await response.json()

                // 다른 조건으로 바뀌거나 화면을 떠났다면 결과를 반영하지 않음
                if (!ignore) {
                    setArticles(data.articles)
                    setTotalElements(data.totalElements)
                    setTotalPages(data.totalPages)
                    setHasNext(data.hasNext)
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
    }, [category, page, search])

    // 카테고리를 바꾸면 해당 카테고리의 첫 페이지부터 조회
    // 이미 적용한 검색 조건은 유지
    function handleCategoryChange(nextCategory) {
        setCategory(nextCategory)
        setPage(1)
    }

    // 검색 버튼 또는 Enter를 누르면 실행
    function handleSearch(event) {
        event.preventDefault()

        setSearch({
            type: inputSearchType,
            keyword: inputKeyword.trim(),
        })

        setPage(1)
    }

    // 검색만 해제하고 선택한 카테고리는 유지
    function handleResetSearch() {
        setInputSearchType('TITLE')
        setInputKeyword('')
        setSearch({
            type: 'TITLE',
            keyword: '',
        })
        setPage(1)
    }

    return (
        <main className="board-page">
            <Link to="/">홈으로</Link>

            <h1>게시판</h1>

            {/* 카테고리는 검색창이 아닌 목록 위에서 선택 */}
            <div className="board-categories" aria-label="게시글 카테고리">
                {[
                    { value: '', label: '전체' },
                    { value: 'FREE', label: '자유' },
                    { value: 'QUESTION', label: '질문' },
                ].map((item) => (
                    <button
                        key={item.value}
                        type="button"
                        aria-pressed={category === item.value}
                        onClick={() => handleCategoryChange(item.value)}
                    >
                        {item.label}
                    </button>
                ))}
            </div>

            {loading && <p role="status">게시글을 불러오는 중입니다.</p>}
            {error && <p role="alert">{error}</p>}

            {!loading && !error && (
                <>
                    <p className="board-count">총 {totalElements}개의 게시글</p>

                    {articles.length === 0 ? (
                        <p>조회된 게시글이 없습니다.</p>
                    ) : (
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

                                        <td>
                                            <Link to={`/board/${article.id}`}>
                                                {article.title}
                                            </Link>
                                        </td>

                                        <td>{article.authorNickname}</td>
                                        <td>{article.viewCount}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </>
            )}

            {/* 검색창은 게시글 목록 아래에 배치 */}
            <form className="board-search" onSubmit={handleSearch}>
                <select
                    aria-label="검색 기준"
                    value={inputSearchType}
                    onChange={(event) => setInputSearchType(event.target.value)}
                >
                    <option value="TITLE">제목</option>
                    <option value="AUTHOR">작성자</option>
                </select>

                <input
                    type="search"
                    aria-label="검색어"
                    placeholder="검색어를 입력하세요"
                    value={inputKeyword}
                    onChange={(event) => setInputKeyword(event.target.value)}
                />

                <button type="submit">검색</button>

                <button type="button" onClick={handleResetSearch}>
                    검색 초기화
                </button>
            </form>

            {!loading && !error && totalPages > 0 && (
                <nav className="board-pagination" aria-label="게시판 페이지 이동">
                    <button
                        type="button"
                        disabled={page <= 1}
                        onClick={() => setPage((currentPage) => currentPage - 1)}
                    >
                        이전
                    </button>

                    <span>
            {page} / {totalPages} 페이지
          </span>

                    <button
                        type="button"
                        disabled={!hasNext}
                        onClick={() => setPage((currentPage) => currentPage + 1)}
                    >
                        다음
                    </button>
                </nav>
            )}
        </main>
    )
}

export default BoardPage