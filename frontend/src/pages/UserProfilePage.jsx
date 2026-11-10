import {useEffect, useState} from 'react'
import {Link, useParams, useSearchParams} from 'react-router-dom'
import AdminBlockControl from '../components/AdminBlockControl.jsx'
import '../App.css'

function UserProfilePage() {
    // /users/5에서 회원 ID를 가져옴
    const {userId} = useParams()
    const [searchParams, setSearchParams] = useSearchParams()

    // 페이지 번호를 주소에 저장해서 뒤로 가기에도 유지
    const requestedPage = Number(searchParams.get('page') || '1')
    const page = Number.isInteger(requestedPage) && requestedPage >= 1 && requestedPage <= 2147483647 ? requestedPage : 1

    const [result, setResult] = useState(null)
    const [retry, setRetry] = useState(0)

    const [myId, setMyId] = useState(null)
    const [myRole, setMyRole] = useState(null)

    useEffect(() => {
        let ignore = false
        const token = sessionStorage.getItem('birdiebuddy.accessToken')

        if (!token) {
            setMyId(null)
            return
        }

        fetch('/api/users/me', {
            headers: { Authorization: `Bearer ${token}` },
        })
            .then((response) => (response.ok ? response.json() : null))
            .then((me) => {
                if (!ignore) {
                    setMyId(me?.id ?? null)
                    setMyRole(me?.role ?? null)
                }
            })
            .catch(() => {
                if (!ignore) setMyId(null)
            })

        return () => {
            ignore = true
        }
    }, [userId])

    // 어떤 회원의 몇 번째 페이지 결과인지 구분
    const requestKey = `${userId}:${page}:${retry}`

    useEffect(() => {
        let ignore = false

        async function fetchJson(url) {
            const response = await fetch(url)

            if (response.status === 404) {
                throw new Error('존재하지 않거나 조회할 수 없는 회원입니다.')
            }

            if (!response.ok) {
                throw new Error('회원 정보를 불러오지 못했습니다.')
            }

            return response.json()
        }

        async function loadProfile() {
            try {
                // 잘못된 주소는 서버 요청 전에 차단
                if (!/^[1-9]\d*$/.test(userId)) {
                    throw new Error('올바르지 않은 회원 주소입니다.')
                }

                // 공개 프로필과 작성글 목록을 함께 요청
                // 공개 API이므로 로그인 토큰을 보내지 않아도 됨
                const [profile, articlePage] = await Promise.all([fetchJson(`/api/users/${userId}`), fetchJson(`/api/users/${userId}/articles?page=${page}&size=10`),])

                if (!ignore) {
                    setResult({
                        key: requestKey, profile, articlePage, error: '',
                    })
                }
            } catch (error) {
                if (!ignore) {
                    setResult({
                        key: requestKey, profile: null, articlePage: null, error: error.message,
                    })
                }
            }
        }

        loadProfile()

        return () => {
            // 다른 회원이나 페이지로 이동했다면 이전 응답은 무시
            ignore = true
        }
    }, [userId, page, requestKey])

    const loading = result?.key !== requestKey
    const profile = loading ? null : result.profile
    const articlePage = loading ? null : result.articlePage
    const error = loading ? '' : result.error
    const isAdmin = myRole === 'ADMIN'
    const isMyProfile = Number(userId) === myId

    function movePage(nextPage) {
        setSearchParams({page: String(nextPage)})
    }

    return (<main className="board-page user-profile-page">
            <nav>
                <Link to="/">홈으로</Link>
                {' / '}
                <Link to="/board">게시판으로</Link>
            </nav>

            {loading && (<p className="profile-notice" role="status">
                    회원 정보를 불러오는 중입니다.
                </p>)}

            {error && (<div className="profile-notice">
                    <p role="alert">{error}</p>
                    <button
                        type="button"
                        onClick={() => setRetry((value) => value + 1)}
                    >
                        다시 시도
                    </button>
                </div>)}

            {profile && articlePage && (<>
                <header className="profile-header">
                    <div className="profile-header-row">
                        <div>
                            <p>유저 프로필</p>
                            <h1>
                                {profile.nickname}

                                {isAdmin && !isMyProfile && (
                                    <AdminBlockControl
                                        targetUserId={Number(userId)}
                                        targetNickname={profile.nickname}
                                        onUnauthorized={() => {
                                            setMyId(null)
                                            setMyRole(null)
                                        }}
                                    />
                                )}
                            </h1>
                        </div>

                        {Number(userId) === myId && (
                            <Link className="profile-edit-link" to="/users/me/edit">
                                개인정보 수정
                            </Link>
                        )}
                    </div>
                </header>

                    <section aria-labelledby="user-articles-heading">
                        <div className="panel-heading">
                            <h2 id="user-articles-heading">작성한 게시글</h2>
                            <span>총 {articlePage.totalElements}개</span>
                        </div>

                        {articlePage.articles.length === 0 ? (<p>
                                {articlePage.totalElements === 0 ? '작성한 게시글이 없습니다.' : '이 페이지에는 게시글이 없습니다.'}
                            </p>) : (<div className="table-container">
                                <table className="article-table">
                                    <thead>
                                    <tr>
                                        <th>번호</th>
                                        <th>카테고리</th>
                                        <th>제목</th>
                                        <th>작성일</th>
                                        <th>조회수</th>
                                    </tr>
                                    </thead>

                                    <tbody>
                                    {articlePage.articles.map((article) => (<tr key={article.id}>
                                            <td>{article.id}</td>

                                            <td>
                                                {article.category === 'FREE' ? '자유' : article.category === 'QUESTION' ? '질문' : article.category}
                                            </td>

                                            <td>
                                                <Link to={`/board/${article.id}`}>
                                                    {article.title}
                                                </Link>
                                            </td>

                                            <td>
                                                {article.createdAt.slice(0, 10)}
                                            </td>

                                            <td>{article.viewCount}</td>
                                        </tr>))}
                                    </tbody>
                                </table>
                            </div>)}

                        {articlePage.totalPages > 0 && page <= articlePage.totalPages && (<nav
                                className="board-pagination"
                                aria-label="작성글 페이지 이동"
                            >
                                <button
                                    type="button"
                                    disabled={page <= 1}
                                    onClick={() => movePage(page - 1)}
                                >
                                    이전
                                </button>

                                <span>
                    {page} / {articlePage.totalPages} 페이지
                  </span>

                                <button
                                    type="button"
                                    disabled={!articlePage.hasNext}
                                    onClick={() => movePage(page + 1)}
                                >
                                    다음
                                </button>
                            </nav>)}

                        {page > 1 && page > articlePage.totalPages && (<div className="board-pagination">
                                <button type="button" onClick={() => movePage(1)}>
                                    첫 페이지로
                                </button>
                            </div>)}
                    </section>
                </>)}
        </main>)
}

export default UserProfilePage
