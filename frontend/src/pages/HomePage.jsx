import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import UserDashboard from '../components/UserDashboard.jsx'
import GolfMap from '../components/GolfMap.jsx'
import '../App.css'

// 두 좌표 사이의 직선거리를 km 단위로 계산
function getDistanceKm(lat1, lng1, lat2, lng2) {
    const toRadians = (degree) => (degree * Math.PI) / 180

    const earthRadiusKm = 6371
    const latDifference = toRadians(lat2 - lat1)
    const lngDifference = toRadians(lng2 - lng1)

    const a =
        Math.sin(latDifference / 2) ** 2 +
        Math.cos(toRadians(lat1)) *
        Math.cos(toRadians(lat2)) *
        Math.sin(lngDifference / 2) ** 2

    return earthRadiusKm * 2 * Math.atan2(
        Math.sqrt(a),
        Math.sqrt(1 - a)
    )
}

function HomePage() {
    // 화면에 표시할 게시글 목록
    const [articles, setArticles] = useState([])

    // 처음 화면에 들어오면 바로 조회하므로 true로 시작
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    // 2026/11/13
    // 골프장 목록과 요청 상태
    const [courses, setCourses] = useState([])
    const [coursesLoading, setCoursesLoading] = useState(true)
    const [coursesError, setCoursesError] = useState('')
    // 사용자가 허용한 현재 위치
    const [myLocation, setMyLocation] = useState(null)
    const [locationMessage, setLocationMessage] = useState('')

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

    useEffect(() => {
        let ignore = false

        async function loadCourses() {
            try {
                const response = await fetch('/api/golf-courses')

                if (!response.ok) {
                    throw new Error('골프장 목록을 불러오지 못했습니다.')
                }

                const data = await response.json()

                if (!ignore) {
                    setCourses(data)
                }
            } catch (error) {
                if (!ignore) {
                    setCoursesError(error.message)
                }
            } finally {
                if (!ignore) {
                    setCoursesLoading(false)
                }
            }
        }

        loadCourses()

        return () => {
            ignore = true
        }
    }, [])

    function requestLocation() {
        if (!window.isSecureContext || !navigator.geolocation) {
            setLocationMessage(
                '현재 위치를 확인할 수 없습니다.'
            )
            return
        }

        setLocationMessage('현재 위치를 확인하는 중입니다.')

        navigator.geolocation.getCurrentPosition(
            (position) => {
                setMyLocation({
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude,
                })
                setLocationMessage('')
            },
            (error) => {
                if (error.code === 1) {
                    setLocationMessage('위치 권한이 거부되었습니다.')
                } else {
                    setLocationMessage('현재 위치를 확인하지 못했습니다.')
                }
            },
            {
                timeout: 10000,
                maximumAge: 60000,
            }
        )
    }

// 위치가 있으면 가까운 순으로 정렬
    const displayedCourses = myLocation
        ? courses
            .map((course) => ({
                ...course,
                distanceKm: getDistanceKm(
                    myLocation.latitude,
                    myLocation.longitude,
                    Number(course.latitude),
                    Number(course.longitude)
                ),
            }))
            .sort((a, b) => a.distanceKm - b.distanceKm)
            .slice(0, 5)
        : courses.slice(0, 5)

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
                                            <td>
                                                <Link to={`/users/${article.authorId}`}>
                                                    {article.authorNickname}
                                                </Link>
                                            </td>

                                            {/* 다섯 번째 칸: 조회수 */}
                                            <td>{article.viewCount}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </section>

                    <UserDashboard />
                </div>

                {/* 두 번째 줄: 지도 + 골프장 목록 */}
                <section className="panel golf-panel">
                    <div className="map-area">
                        <h2>지도</h2>
                        <GolfMap courses={displayedCourses} myLocation={myLocation} />
                    </div>

                    <div className="course-area">
                            <h2>골프장 목록</h2>

                            <button type="button" onClick={requestLocation}>
                                내 위치 확인
                            </button>

                            {locationMessage && <p role="status">{locationMessage}</p>}

                            {coursesLoading && <p>골프장 정보를 불러오는 중입니다.</p>}

                            {coursesError && <p role="alert">{coursesError}</p>}

                            {!coursesLoading && !coursesError && (
                                <>
                                    <div className="table-container" style={{ maxHeight: '330px', overflowY: 'auto' }}>
                                        <table className="article-table">
                                            <thead>
                                            <tr>
                                                <th>이름</th>
                                                <th>주소</th>
                                                <th>거리</th>
                                            </tr>
                                            </thead>

                                            <tbody>
                                            {displayedCourses.map((course) => (
                                                <tr key={course.id}>
                                                    <td>{course.name}</td>
                                                    <td>{course.address}</td>
                                                    <td>
                                                        {myLocation
                                                            ? `${course.distanceKm.toFixed(1)} km`
                                                            : '—'}
                                                    </td>
                                                </tr>
                                            ))}
                                            </tbody>
                                        </table>
                                    </div>
                                </>
                            )}
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
