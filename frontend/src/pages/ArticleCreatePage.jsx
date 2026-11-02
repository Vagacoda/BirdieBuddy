import { useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import '../App.css'

const TOKEN_KEY = 'birdiebuddy.accessToken'

function ArticleCreatePage() {
    // 작성 성공 후 상세 페이지로 이동할 때 사용
    const navigate = useNavigate()

    // 입력값
    const [category, setCategory] = useState('FREE')
    const [title, setTitle] = useState('')
    const [content, setContent] = useState('')

    // 요청 상태와 오류 메시지
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')

    // 토큰이 없으면 로그인 안내
    // 토큰이 실제로 유효한지는 서버에서 검사
    const [needsLogin, setNeedsLogin] = useState(
        () => !sessionStorage.getItem(TOKEN_KEY)
    )

    // 빠르게 여러 번 눌러도 요청을 중복 전송하지 않도록 구분
    const submittingRef = useRef(false)

    async function handleSubmit(event) {
        event.preventDefault()

        if (submittingRef.current) {
            return
        }

        setError('')

        const token = sessionStorage.getItem(TOKEN_KEY)

        if (!token) {
            setNeedsLogin(true)
            return
        }

        // 공백만 입력한 경우 차단
        if (!title.trim()) {
            setError('제목을 입력해 주세요.')
            return
        }

        if (!content.trim()) {
            setError('내용을 입력해 주세요.')
            return
        }

        submittingRef.current = true
        setSubmitting(true)

        try {
            const response = await fetch('/api/articles', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    category,
                    title: title.trim(),
                    // 본문의 줄바꿈과 들여쓰기는 유지
                    content,
                }),
            })

            const data = await response.json().catch(() => ({}))

            if (response.status === 401) {
                sessionStorage.removeItem(TOKEN_KEY)
                setNeedsLogin(true)
                throw new Error(
                    '로그인이 만료되었습니다. 작성 내용을 복사해 둔 뒤 다시 로그인해 주세요.'
                )
            }

            if (!response.ok) {
                throw new Error(
                    data.message || '게시글을 등록하지 못했습니다.'
                )
            }

            // 백엔드가 반환한 새 게시글 ID로 이동
            // replace를 사용해 뒤로 가기로 제출 완료된 작성 화면에 돌아오지 않도록 함
            navigate(`/board/${data.id}`, { replace: true })
        } catch (error) {
            // 실패해도 입력한 제목과 본문은 그대로 유지
            setError(error.message)
        } finally {
            submittingRef.current = false
            setSubmitting(false)
        }
    }

    return (
        <main className="board-page">
            <Link to="/board">게시판 목록으로</Link>

            <h1>게시글 작성</h1>

            {needsLogin && (
                <p className="article-login-notice">
                    게시글을 작성하려면 로그인이 필요합니다.{' '}
                    <Link to="/">홈에서 로그인</Link>
                </p>
            )}

            <form className="article-editor" onSubmit={handleSubmit}>
                {/* 전송 중에는 입력값 변경과 중복 제출 방지 */}
                <fieldset disabled={submitting || needsLogin}>
                    <label htmlFor="article-category">카테고리</label>
                    <select
                        id="article-category"
                        value={category}
                        onChange={(event) => setCategory(event.target.value)}
                    >
                        <option value="FREE">자유</option>
                        <option value="QUESTION">질문</option>
                    </select>

                    <label htmlFor="article-title">제목</label>
                    <input
                        id="article-title"
                        type="text"
                        placeholder="제목을 입력하세요"
                        maxLength={200}
                        value={title}
                        onChange={(event) => setTitle(event.target.value)}
                        required
                    />

                    <label htmlFor="article-content">내용</label>
                    <textarea
                        id="article-content"
                        placeholder="내용을 입력하세요"
                        rows={12}
                        value={content}
                        onChange={(event) => setContent(event.target.value)}
                        required
                    />

                    <div className="article-editor-actions">
                        <button type="submit">
                            {submitting ? '등록 중...' : '등록'}
                        </button>
                    </div>
                </fieldset>

                {error && <p role="alert">{error}</p>}
            </form>
        </main>
    )
}

export default ArticleCreatePage