import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'

const TOKEN_KEY = 'birdiebuddy.accessToken'

function CommentSection({
                            articleId,
                            myId,
                            needsLogin,
                            onUnauthorized,
                        }) {
    const [comments, setComments] = useState([])
    const [loading, setLoading] = useState(true)
    const [loadError, setLoadError] = useState('')
    const [reloadKey, setReloadKey] = useState(0)

    // 새 댓글 입력값
    const [content, setContent] = useState('')

    // 수정 중인 댓글 ID와 입력값
    const [editingId, setEditingId] = useState(null)
    const [editContent, setEditContent] = useState('')

    const [busy, setBusy] = useState(false)
    const busyRef = useRef(false)
    const [actionError, setActionError] = useState('')
    const [message, setMessage] = useState('')

    const canWrite = myId !== null && !needsLogin
    const baseUrl = `/api/articles/${articleId}/comments`

    useEffect(() => {
        let ignore = false

        async function loadComments() {
            setLoading(true)
            setLoadError('')

            try {
                // 댓글 목록은 로그인하지 않아도 조회 가능
                const response = await fetch(baseUrl)

                if (!response.ok) {
                    throw new Error('댓글 목록을 불러오지 못했습니다.')
                }

                // 댓글 API는 { comments: [...] }가 아닌 배열을 직접 반환
                const data = await response.json()

                if (!ignore) {
                    setComments(data)
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

        loadComments()

        return () => {
            ignore = true
        }
    }, [baseUrl, reloadKey])

    // 작성·수정·삭제에서 공통으로 사용하는 인증 요청
    async function sendRequest(url, method, body) {
        const token = sessionStorage.getItem(TOKEN_KEY)

        if (!token) {
            onUnauthorized()
            throw new Error('홈에서 다시 로그인해 주세요.')
        }

        const response = await fetch(url, {
            method,
            headers: {
                Authorization: `Bearer ${token}`,
                ...(body ? { 'Content-Type': 'application/json' } : {}),
            },
            body: body ? JSON.stringify(body) : undefined,
        })

        const data = await response.json().catch(() => ({}))

        if (response.status === 401) {
            sessionStorage.removeItem(TOKEN_KEY)
            onUnauthorized()
            throw new Error(
                '로그인이 만료되었습니다. 입력 내용을 복사해 둔 뒤 다시 로그인해 주세요.'
            )
        }

        if (response.status === 403) {
            throw new Error('본인이 작성한 댓글만 수정·삭제할 수 있습니다.')
        }

        if (response.status === 404) {
            throw new Error('게시글 또는 댓글이 없거나 삭제되었습니다.')
        }

        if (!response.ok) {
            throw new Error(data.message || '댓글 요청을 처리하지 못했습니다.')
        }

        return data
    }

    // 요청 중에는 다른 댓글 작업도 잠시 막음
    async function runAction(action) {
        if (busyRef.current) {
            return
        }

        busyRef.current = true
        setBusy(true)
        setActionError('')
        setMessage('')

        try {
            await action()
        } catch (error) {
            setActionError(error.message)
        } finally {
            busyRef.current = false
            setBusy(false)
        }
    }

    function handleCreate(event) {
        event.preventDefault()

        if (!content.trim()) {
            setActionError('댓글 내용을 입력해 주세요.')
            return
        }

        runAction(async () => {
            const createdComment = await sendRequest(baseUrl, 'POST', {
                content,
            })

            // 서버가 반환한 새 댓글을 목록 끝에 추가
            setComments((previous) => [...previous, createdComment])
            setContent('')
            setMessage('댓글이 등록되었습니다.')
        })
    }

    function startEditing(comment) {
        setEditingId(comment.id)
        setEditContent(comment.content)
        setActionError('')
        setMessage('')
    }

    function handleUpdate(event, commentId) {
        event.preventDefault()

        if (!editContent.trim()) {
            setActionError('댓글 내용을 입력해 주세요.')
            return
        }

        runAction(async () => {
            const savedContent = editContent

            await sendRequest(`${baseUrl}/${commentId}`, 'PUT', {
                content: savedContent,
            })

            // 저장 성공한 댓글의 내용만 변경
            setComments((previous) =>
                previous.map((comment) =>
                    comment.id === commentId
                        ? { ...comment, content: savedContent }
                        : comment
                )
            )

            setEditingId(null)
            setEditContent('')
            setMessage('댓글이 수정되었습니다.')
        })
    }

    function handleDelete(commentId) {
        if (busyRef.current) {
            return
        }

        if (!window.confirm('이 댓글을 삭제하시겠습니까?')) {
            return
        }

        runAction(async () => {
            await sendRequest(`${baseUrl}/${commentId}`, 'DELETE')

            // 삭제에 성공한 댓글을 화면에서도 제거
            setComments((previous) =>
                previous.filter((comment) => comment.id !== commentId)
            )

            if (editingId === commentId) {
                setEditingId(null)
                setEditContent('')
            }

            setMessage('댓글이 삭제되었습니다.')
        })
    }

    return (
        <section className="comment-section" aria-labelledby="comment-heading">
            <h2 id="comment-heading">
                댓글 {!loading && !loadError && `(${comments.length})`}
            </h2>

            {loading && <p role="status">댓글을 불러오는 중입니다.</p>}

            {loadError && (
                <div>
                    <p role="alert">{loadError}</p>
                    <button
                        type="button"
                        onClick={() => setReloadKey((value) => value + 1)}
                    >
                        다시 불러오기
                    </button>
                </div>
            )}

            {!loading && !loadError && (
                <>
                    {comments.length === 0 && (
                        <p>아직 댓글이 없습니다.</p>
                    )}

                    <ul className="comment-list">
                        {comments.map((comment) => (
                            <li key={comment.id} className="comment-item">
                                <div className="comment-meta">
                                    <strong>{comment.authorNickname}</strong>
                                    <time dateTime={comment.createdAt}>
                                        {comment.createdAt.replace('T', ' ')}
                                    </time>
                                </div>

                                {editingId === comment.id ? (
                                    <form
                                        className="comment-form"
                                        onSubmit={(event) => handleUpdate(event, comment.id)}
                                    >
                                        <label htmlFor={`comment-edit-${comment.id}`}>
                                            댓글 수정
                                        </label>

                                        <textarea
                                            id={`comment-edit-${comment.id}`}
                                            value={editContent}
                                            onChange={(event) => setEditContent(event.target.value)}
                                            rows={3}
                                            disabled={busy}
                                            required
                                        />

                                        <div className="article-actions">
                                            <button
                                                type="button"
                                                disabled={busy}
                                                onClick={() => {
                                                    setEditingId(null)
                                                    setEditContent('')
                                                    setActionError('')
                                                }}
                                            >
                                                취소
                                            </button>

                                            <button
                                                type="submit"
                                                disabled={busy || !canWrite}
                                            >
                                                저장
                                            </button>
                                        </div>
                                    </form>
                                ) : (
                                    <>
                                        <p className="comment-content">{comment.content}</p>

                                        {/* 게시글 작성자가 아니라 댓글 작성자와 비교 */}
                                        {canWrite && myId === comment.authorId && (
                                            <div className="article-actions">
                                                <button
                                                    type="button"
                                                    disabled={busy}
                                                    onClick={() => startEditing(comment)}
                                                >
                                                    수정
                                                </button>

                                                <button
                                                    type="button"
                                                    className="danger-button"
                                                    disabled={busy}
                                                    onClick={() => handleDelete(comment.id)}
                                                >
                                                    삭제
                                                </button>
                                            </div>
                                        )}
                                    </>
                                )}
                            </li>
                        ))}
                    </ul>

                    <form className="comment-form" onSubmit={handleCreate}>
                        <label htmlFor="new-comment">댓글 작성</label>

                        <textarea
                            id="new-comment"
                            value={content}
                            onChange={(event) => setContent(event.target.value)}
                            rows={3}
                            placeholder={
                                canWrite
                                    ? '댓글을 입력하세요'
                                    : '로그인 후 댓글을 작성할 수 있습니다'
                            }
                            readOnly={!canWrite}
                            disabled={busy}
                            required
                        />

                        <div className="article-actions">
                            <button
                                type="submit"
                                disabled={busy || !canWrite}
                            >
                                {busy ? '처리 중...' : '댓글 등록'}
                            </button>
                        </div>
                    </form>

                    {!canWrite && (
                        <p>
                            로그인하지 않았거나 로그인 상태를 확인하지 못했습니다.{' '}
                            <Link to="/">홈에서 확인</Link>
                        </p>
                    )}
                </>
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
        </section>
    )
}

export default CommentSection