import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import defaultThumbnail from '../assets/news-default.svg'
import './GolfNews.css'

function GolfNews() {
    const [news, setNews] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    // 홈에 표시할 최신 뉴스 10개 조회
    useEffect(() => {
        const controller = new AbortController()

        async function loadNews() {
            try {
                const response = await fetch(
                    '/api/news?page=0&size=10',
                    { signal: controller.signal }
                )

                if (!response.ok) {
                    throw new Error('뉴스를 불러오지 못했습니다.')
                }

                const data = await response.json()

                if (!controller.signal.aborted) {
                    setNews(data.news)
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

        // 화면을 벗어나면 진행 중인 요청 취소
        return () => controller.abort()
    }, [])

    return (
        <section className="panel news-panel">
            <div className="panel-heading">
                <h2>골프 뉴스</h2>
                <Link to="/news">더 보기</Link>
            </div>

            <p className="news-source">네이버 뉴스 검색</p>

            {loading && <p role="status">뉴스를 불러오는 중입니다.</p>}
            {error && <p role="alert">{error}</p>}

            {!loading && !error && news.length === 0 && (
                <p>등록된 뉴스가 없습니다.</p>
            )}

            {!loading && !error && (
                <ul className="news-list">
                    {news.map((item) => (
                        <li key={item.id}>
                            {/* 이미지와 제목 모두 같은 원문 링크로 연결 */}
                            <a
                                className="news-card"
                                href={item.originalUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                {/* 모든 기사에 기본 썸네일 사용 */}
                                <img
                                    src={defaultThumbnail}
                                    alt=""
                                    loading="lazy"
                                />

                                <div className="news-card-content">
                                    <h3>{item.title}</h3>

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
            )}
        </section>
    )
}

export default GolfNews