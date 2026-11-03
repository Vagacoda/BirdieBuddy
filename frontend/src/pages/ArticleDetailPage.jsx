import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import CommentSection from '../components/CommentSection.jsx'
import '../App.css'

const TOKEN_KEY = 'birdiebuddy.accessToken'

// 조회수 중복 방지에 사용하는 브라우저 방문자 ID
function getVisitorId() {
    const key = 'birdiebuddy.visitorId'
    let visitorId = localStorage.getItem(key)

    if (!visitorId) {
        visitorId = crypto.randomUUID()
        localStorage.setItem(key, visitorId)
    }

    return visitorId
}

function ArticleDetailPage() {
    const { articleId } = useParams()

    // 다른 게시글로 이동하면 입력값과 조회 상태를 새로 시작
    return <ArticleDetailContent key={articleId} articleId={articleId} />
}

function ArticleDetailContent({ articleId }) {
    const navigate = useNavigate()

    const [article, setArticle] = useState(null)
    const [loading, setLoading] = useState(true)
    const [loadError, setLoadError] = useState('')

    // 서버에서 확인한 로그인 회원 ID
    const [myId, setMyId] = useState(null)
    const [authError, setAuthError] = useState('')
    const [needsLogin, setNeedsLogin] = useState(false)

    // 수정 입력창 상태
    const [editing, setEditing] = useState(false)
    const [category, setCategory] = useState('FREE')
    const [title, setTitle] = useState('')
    const [content, setContent] = useState('')

    // 수정·삭제 요청 상태
    const [busy, setBusy] = useState(false)
    const busyRef = useRef(false)
    const [actionError, setActionError] = useState('')
    const [message, setMessage] = useState('')

    useEffect(() => {
        let ignore = false

        async function loadArticle() {
            try {
                const response = await fetch(`/api/articles/${articleId}`, {
                    headers: {
                        'X-Visitor-Id': getVisitorId(),
                    },
                })

                if (response.status === 404) {
                    throw new Error('존재하지 않거나 삭제된 게시글입니다.')
                }

                if (!response.ok) {
                    throw new Error('게시글을 불러오지 못했습니다.')
                }

                const data = await response.json()

                if (!ignore) {
                    setArticle(data)
                }
            } catch (error) {
                if (!ignore) {
                    setLoadError(error.message)
                }
            } finally {
                if (!ignore) {
                    setLoading(false)
                }
            }
        }

        async function loadMyInfo() {
            const token = sessionStorage.getItem(TOKEN_KEY)

            // 비로그인이면 공개 게시글만 조회
            if (!token) {
                return
            }

            try {
                const response = await fetch('/api/users/me', {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                })

                if (ignore) {
                    return
                }

                if (response.status === 401) {
                    sessionStorage.removeItem(TOKEN_KEY)
                    setNeedsLogin(true)
                    setAuthError('로그인이 만료되었습니다.')
                    return
                }

                if (!response.ok) {
                    throw new Error('로그인 정보를 확인하지 못했습니다.')
                }

                const data = await response.json()

                if (!ignore) {
                    setMyId(data.id)
                }
            } catch (error) {
                if (!ignore) {
                    setAuthError(error.message)
                }
            }
        }

        // 게시글 조회와 내 정보 조회는 각각 실행
        // 내 정보 조회가 실패해도 공개 게시글은 볼 수 있음
        loadArticle()
        loadMyInfo()

        return () => {
            ignore = true
        }
    }, [articleId])

    // 닉네임이 아닌 회원 ID로 본인 글인지 비교
    const isOwner =
        article !== null &&
        myId !== null &&
        article.authorId === myId

    function startEditing() {
        // 현재 게시글 내용을 입력창에 복사
        setCategory(article.category)
        setTitle(article.title)
        setContent(article.content)

        setActionError('')
        setMessage('')
        setEditing(true)
    }

    function cancelEditing() {
        // 저장하지 않고 기존 상세 화면으로 복귀
        setEditing(false)
        setActionError('')
    }

    // 수정·삭제 요청에 사용할 토큰 확인
    function getAccessToken() {
        const token = sessionStorage.getItem(TOKEN_KEY)

        if (!token) {
            setNeedsLogin(true)
            throw new Error('홈에서 다시 로그인해 주세요.')
        }

        return token
    }

    async function checkActionResponse(response) {
        const data = await response.json().catch(() => ({}))

        if (response.status === 401) {
            sessionStorage.removeItem(TOKEN_KEY)
            setNeedsLogin(true)
            throw new Error(
                '로그인이 만료되었습니다. 수정 중인 내용은 복사해 둔 뒤 다시 로그인해 주세요.'
            )
        }

        if (response.status === 403) {
            throw new Error('본인이 작성한 게시글만 수정·삭제할 수 있습니다.')
        }

        if (response.status === 404) {
            throw new Error('존재하지 않거나 삭제된 게시글입니다.')
        }

        if (!response.ok) {
            throw new Error(data.message || '요청을 처리하지 못했습니다.')
        }

        return data
    }

    async function handleUpdate(event) {
        event.preventDefault()

        if (busyRef.current) {
            return
        }

        setActionError('')
        setMessage('')

        if (!title.trim() || !content.trim()) {
            setActionError('제목과 내용을 입력해 주세요.')
            return
        }

        busyRef.current = true
        setBusy(true)

        try {
            const token = getAccessToken()
            const updatedValues = {
                category,
                title: title.trim(),
                content,
            }

            const response = await fetch(`/api/articles/${articleId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(updatedValues),
            })

            await checkActionResponse(response)

            // 저장에 성공한 값으로 현재 화면 갱신
            // 상세 조회를 다시 요청하지 않으므로 조회수에도 영향을 주지 않음
            setArticle((previous) => ({
                ...previous,
                ...updatedValues,
            }))

            setEditing(false)
            setMessage('게시글이 수정되었습니다.')
        } catch (error) {
            // 실패하면 입력한 내용을 유지
            setActionError(error.message)
        } finally {
            busyRef.current = false
            setBusy(false)
        }
    }

    async function handleDelete() {
        if (busyRef.current) {
            return
        }

        const confirmed = window.confirm('이 게시글을 삭제하시겠습니까?')

        if (!confirmed) {
            return
        }

        setActionError('')
        setMessage('')
        busyRef.current = true
        setBusy(true)

        try {
            const token = getAccessToken()

            const response = await fetch(`/api/articles/${articleId}`, {
                method: 'DELETE',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })

            await checkActionResponse(response)

            // 삭제 완료 후 목록으로 이동
            navigate('/board', { replace: true })
        } catch (error) {
            setActionError(error.message)
        } finally {
            busyRef.current = false
            setBusy(false)
        }
    }

    return (
        <main className="article-detail-page">
            <nav>
                <Link to="/">홈으로</Link>
                {' / '}
                <Link to="/board">게시판 목록으로</Link>
            </nav>

            {loading && <p role="status">게시글을 불러오는 중입니다.</p>}
            {loadError && <p role="alert">{loadError}</p>}
            {authError && <p role="alert">{authError}</p>}

            {needsLogin && (
                <p>
                    <Link to="/">홈에서 다시 로그인</Link>
                </p>
            )}

            {article && (
                <>
                    {editing ? (
                        <form className="article-editor" onSubmit={handleUpdate}>
                            <h1>게시글 수정</h1>

                            <fieldset disabled={busy}>
                                <label htmlFor="edit-category">카테고리</label>
                                <select
                                    id="edit-category"
                                    value={category}
                                    onChange={(event) => setCategory(event.target.value)}
                                >
                                    <option value="FREE">자유</option>
                                    <option value="QUESTION">질문</option>

                                    {/* 기존 데이터에 다른 카테고리가 있어도 유지 가능 */}
                                    {!['FREE', 'QUESTION'].includes(category) && (
                                        <option value={category}>{category}</option>
                                    )}
                                </select>

                                <label htmlFor="edit-title">제목</label>
                                <input
                                    id="edit-title"
                                    type="text"
                                    maxLength={200}
                                    value={title}
                                    onChange={(event) => setTitle(event.target.value)}
                                    required
                                />

                                <label htmlFor="edit-content">내용</label>
                                <textarea
                                    id="edit-content"
                                    rows={12}
                                    value={content}
                                    onChange={(event) => setContent(event.target.value)}
                                    required
                                />

                                <div className="article-actions">
                                    <button type="button" onClick={cancelEditing}>
                                        취소
                                    </button>

                                    <button type="submit" disabled={needsLogin}>
                                        {busy ? '저장 중...' : '저장'}
                                    </button>
                                </div>
                            </fieldset>
                        </form>
                    ) : (
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
                                    작성자:{' '}
                                    <Link to={`/users/${article.authorId}`}>
                                        {article.authorNickname}
                                    </Link>
                                    {' · '}
                                    조회수: {article.viewCount}
                                </p>

                                <p>
                                    작성일: {article.createdAt.replace('T', ' ')}
                                </p>
                            </header>

                            <hr />

                            <div
                                style={{
                                    whiteSpace: 'pre-wrap',
                                    overflowWrap: 'anywhere',
                                }}
                            >
                                {article.content}
                            </div>

                            {/* 본인 글에서만 수정·삭제 표시 */}
                            {isOwner && !needsLogin && (
                                <div className="article-actions">
                                    <button
                                        type="button"
                                        disabled={busy}
                                        onClick={startEditing}
                                    >
                                        수정
                                    </button>

                                    <button
                                        type="button"
                                        className="danger-button"
                                        disabled={busy}
                                        onClick={handleDelete}
                                    >
                                        {busy ? '삭제 중...' : '삭제'}
                                    </button>
                                </div>
                            )}
                        </article>
                    )}

                    {actionError && (
                        <p className="article-feedback" role="alert">
                            {actionError}
                        </p>
                    )}

                    {message && (
                        <p className="article-feedback" role="status">
                            {message}
                        </p>
                    )}
                    <CommentSection
                        key={articleId}
                        articleId={articleId}
                        myId={myId}
                        needsLogin={needsLogin}
                        onUnauthorized={() => setNeedsLogin(true)}
                    />
                </>
            )}
        </main>
    )
}

export default ArticleDetailPage