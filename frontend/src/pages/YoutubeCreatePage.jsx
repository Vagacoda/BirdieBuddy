import { useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import '../App.css'

function YoutubeCreatePage() {
    const navigate = useNavigate()

    const [url, setUrl] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')

    // 빠른 연속 클릭으로 중복 요청이 전송되는 것 방지
    const submittingRef = useRef(false)

    async function handleSubmit(event) {
        event.preventDefault()

        if (submittingRef.current) return

        const token = sessionStorage.getItem('birdiebuddy.accessToken')

        if (!token) {
            setError('홈에서 관리자 계정으로 로그인해 주세요.')
            return
        }

        if (!url.trim()) {
            setError('유튜브 링크를 입력해 주세요.')
            return
        }

        submittingRef.current = true
        setSubmitting(true)
        setError('')

        try {
            const response = await fetch('/api/admin/youtube', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ url: url.trim() }),
            })

            const data = await response.json().catch(() => ({}))

            if (response.status === 401) {
                throw new Error('로그인이 만료되었습니다. 다시 로그인해 주세요.')
            }

            if (response.status === 403) {
                throw new Error('관리자만 영상을 등록할 수 있습니다.')
            }

            if (!response.ok) {
                throw new Error(
                    data.message || '영상 등록에 실패했습니다. 링크를 확인해 주세요.'
                )
            }

            // 등록 후 최신 영상이 보이는 목록 첫 페이지로 이동
            navigate('/youtube', { replace: true })
        } catch (error) {
            setError(error.message)
        } finally {
            submittingRef.current = false
            setSubmitting(false)
        }
    }

    return (
        <main className="board-page">
            <Link to="/youtube">영상 목록으로</Link>

            <h1>유튜브 영상 등록</h1>
            <p>관리자가 선택한 영상의 제목과 채널 이름을 자동으로 가져옵니다.</p>

            <form className="youtube-create-form" onSubmit={handleSubmit}>
                <label htmlFor="youtube-url">유튜브 영상 링크</label>

                <input
                    id="youtube-url"
                    type="url"
                    required
                    maxLength={2048}
                    placeholder="https://www.youtube.com/watch?v=..."
                    value={url}
                    disabled={submitting}
                    onChange={event => setUrl(event.target.value)}
                />

                {error && <p role="alert">{error}</p>}

                <button type="submit" disabled={submitting}>
                    {submitting ? '영상 정보 확인 중…' : '영상 등록'}
                </button>
            </form>
        </main>
    )
}

export default YoutubeCreatePage