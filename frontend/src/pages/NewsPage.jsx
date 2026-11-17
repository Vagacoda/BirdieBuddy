import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import defaultThumbnail from '../assets/news-default.svg'
import '../App.css'
import '../components/GolfNews.css'

function NewsPage() {
    const [page, setPage] = useState(0)

    // 입력 중인 검색어와 실제 조회에 사용한 검색어를 구분
    const [searchInput, setSearchInput] = useState('')
    const [keyword, setKeyword] = useState('')

    const [data, setData] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    // 페이지 또는 적용된 검색어가 변경되면 조회
    useEffect(() => {
        const controller = new AbortController()

        async function loadNews() {
            try {
                const params = new URLSearchParams({
                    page: String(page),
                    size: '20',
                    keyword,
                })

                const response = await fetch(
                    `/api/news?${params}`,
                    { signal: controller.signal }
                )

                if (!response.ok) {
                    throw new Error('뉴스를 불러오지 못했습니다.')
                }

                const result = await response.json()

                if (!controller.signal.aborted) {
                    setData(result)
                }
            } catch (error) {
                if (!controller.signal.aborted) {
                    setError(error.message)
                }
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false)
                }
            }
        }

        loadNews()

        // 이전 조회가 늦게 도착해서 새 결과를 덮어쓰지 않도록 취소
        return () => controller.abort()
    }, [page, keyword])

    function handleSearch(event) {
        event.preventDefault()

        const nextKeyword = searchInput.trim()

        // 조회 조건이 같으면 다시 실행하지 않음
        if (page === 0 && keyword === nextKeyword) {
            return
        }

        setLoading(true)
        setError('')
        setKeyword(nextKeyword)
        setPage(0)
    }

    function showAllNews() {
        setSearchInput('')

        if (page === 0 && keyword === '') {
            return
        }

        setLoading(true)
        setError('')
        setKeyword('')
        setPage(0)
    }

    function changePage(nextPage) {
        setLoading(true)
        setError('')
        setPage(nextPage)
    }

    return (
        <main className="board-page">
            <Link to="/">홈으로</Link>

            <div className="panel-heading">
                <h1>골프 뉴스</h1>
                <span className="news-source">
                    네이버 뉴스 검색 · 최신순
                </span>
            </div>

            {/* 제목 검색: Enter 또는 검색 버튼으로 실행 */}
            <form className="news-search" onSubmit={handleSearch}>
                <label htmlFor="news-keyword">제목 검색</label>

                <input
                    id="news-keyword"
                    type="search"
                    maxLength={100}
                    value={searchInput}
                    onChange={(event) => setSearchInput(event.target.value)}
                    placeholder="검색어를 입력하세요"
                />

                <button type="submit" disabled={loading}>
                    검색
                </button>

                <button
                    type="button"
                    disabled={loading}
                    onClick={showAllNews}
                >
                    전체 보기
                </button>
            </form>

            {loading && <p role="status">뉴스를 불러오는 중입니다.</p>}
            {error && <p role="alert">{error}</p>}

            {!loading && !error && data && (
                <>
                    <p>
                        {keyword ? `‘${keyword}’ 검색 결과` : '전체 뉴스'}
                        {' '}총 {data.totalElements}개
                    </p>

                    {data.news.length === 0 && (
                        <p>표시할 뉴스가 없습니다.</p>
                    )}

                    <ul className="news-list">
                        {data.news.map((item) => (
                            <li key={item.id}>
                                {/* 기본 이미지와 제목 클릭 시 원문 이동 */}
                                <a
                                    className="news-card"
                                    href={item.originalUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                >
                                    <img
                                        src={defaultThumbnail}
                                        alt=""
                                        loading="lazy"
                                    />

                                    <div className="news-card-content">
                                        <h3>{item.title}</h3>

                                        <p className="news-summary">
                                            {item.summary}
                                        </p>

                                        <time dateTime={item.publishedAt}>
                                            {item.publishedAt
                                                .replace('T', ' ')
                                                .slice(0, 16)}
                                        </time>
                                    </div>
                                </a>
                            </li>
                        ))}
                    </ul>
                </>
            )}

            {!loading && !error && data && data.totalPages > 0 && (
                <nav
                    className="news-pagination"
                    aria-label="뉴스 페이지 이동"
                >
                    <button
                        type="button"
                        disabled={page === 0}
                        onClick={() => changePage(page - 1)}
                    >
                        이전
                    </button>

                    <span>
                        {page + 1} / {data.totalPages}
                    </span>

                    <button
                        type="button"
                        disabled={page >= data.totalPages - 1}
                        onClick={() => changePage(page + 1)}
                    >
                        다음
                    </button>
                </nav>
            )}
        </main>
    )
}

export default NewsPage