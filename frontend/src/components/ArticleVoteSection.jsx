import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

const TOKEN_KEY = 'birdiebuddy.accessToken'

function ArticleVoteSection({ articleId, onUnauthorized }) {
    const [vote, setVote] = useState(null)
    const [loading, setLoading] = useState(true)
    const [busy, setBusy] = useState(false)
    const [error, setError] = useState('')

    useEffect(() => {
        let ignore = false
        const token = sessionStorage.getItem(TOKEN_KEY)

        async function loadVote() {
            try {
                const response = await fetch(`/api/articles/${articleId}/votes`, {
                    headers: token
                        ? { Authorization: `Bearer ${token}` }
                        : {},
                })

                if (!response.ok) {
                    throw new Error('추천 정보를 불러오지 못했습니다.')
                }

                const data = await response.json()
                if (!ignore) setVote(data)
            } catch (caught) {
                if (!ignore) setError(caught.message)
            } finally {
                if (!ignore) setLoading(false)
            }
        }

        loadVote()

        return () => {
            ignore = true
        }
    }, [articleId])

    async function handleVote(voteType) {
        if (busy) return

        const token = sessionStorage.getItem(TOKEN_KEY)

        if (!token) {
            setError('추천·비추천은 로그인 후 사용할 수 있습니다.')
            return
        }

        setBusy(true)
        setError('')

        try {
            const response = await fetch(`/api/articles/${articleId}/votes`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ voteType }),
            })

            const data = await response.json().catch(() => ({}))

            if (response.status === 401) {
                sessionStorage.removeItem(TOKEN_KEY)
                onUnauthorized?.()
                throw new Error('로그인이 만료되었습니다. 다시 로그인해 주세요.')
            }

            if (!response.ok) {
                throw new Error(data.message || '투표를 처리하지 못했습니다.')
            }

            // 서버가 돌려준 최신 개수와 내 선택으로 화면 갱신
            setVote(data)
        } catch (caught) {
            setError(caught.message)
        } finally {
            setBusy(false)
        }
    }

    return (
        <section className="article-votes" aria-label="게시글 추천">
            {loading ? (
                <p role="status">추천 정보를 불러오는 중입니다.</p>
            ) : vote ? (
                <div className="article-vote-buttons">
                    <button
                        type="button"
                        aria-pressed={vote.myVote === 'LIKE'}
                        disabled={busy}
                        onClick={() => handleVote('LIKE')}
                    >
                        추천 {vote.voteCount}
                    </button>

                    <button
                        type="button"
                        aria-pressed={vote.myVote === 'DISLIKE'}
                        disabled={busy}
                        onClick={() => handleVote('DISLIKE')}
                    >
                        비추천 {vote.dislikeCount}
                    </button>
                </div>
            ) : null}

            {error && (
                <p role="alert">
                    {error}{' '}
                    {!sessionStorage.getItem(TOKEN_KEY) && (
                        <Link to="/">홈에서 로그인</Link>
                    )}
                </p>
            )}
        </section>
    )
}

export default ArticleVoteSection