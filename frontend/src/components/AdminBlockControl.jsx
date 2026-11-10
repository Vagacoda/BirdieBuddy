import { useState } from 'react'

const TOKEN_KEY = 'birdiebuddy.accessToken'

const BLOCK_DURATIONS = [
    { value: 'ONE_DAY', label: '24시간' },
    { value: 'ONE_WEEK', label: '일주일' },
    { value: 'ONE_MONTH', label: '한 달' },
    { value: 'ONE_YEAR', label: '일 년' },
    { value: 'PERMANENT', label: '영구 차단' },
]

function AdminBlockControl({
                               targetUserId,
                               targetNickname,
                               onUnauthorized,
                           }) {
    const [selecting, setSelecting] = useState(false)
    const [duration, setDuration] = useState('ONE_DAY')
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [message, setMessage] = useState('')

    async function handleBlock() {
        if (submitting) {
            return
        }

        const token = sessionStorage.getItem(TOKEN_KEY)

        if (!token) {
            onUnauthorized?.()
            setError('다시 로그인해 주세요.')
            return
        }

        setSubmitting(true)
        setError('')
        setMessage('')

        try {
            const response = await fetch(
                `/api/admin/users/${targetUserId}/block`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({ duration }),
                }
            )

            const data = await response.json().catch(() => ({}))

            if (response.status === 401) {
                sessionStorage.removeItem(TOKEN_KEY)
                onUnauthorized?.()
                throw new Error('로그인이 만료되었습니다. 다시 로그인해 주세요.')
            }

            if (response.status === 403) {
                throw new Error('관리자 권한이 없습니다.')
            }

            if (!response.ok) {
                throw new Error(data.message || '회원 차단에 실패했습니다.')
            }

            setMessage(
                `${targetNickname} 회원을 ${BLOCK_DURATIONS.find(
                    (item) => item.value === duration
                ).label} 차단했습니다.`
            )
            setSelecting(false)
        } catch (error) {
            setError(error.message)
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <span className="admin-block-control">
            {!selecting ? (
                <button
                    type="button"
                    className="danger-button"
                    onClick={() => {
                        setSelecting(true)
                        setError('')
                        setMessage('')
                    }}
                >
                    차단
                </button>
            ) : (
                <span className="admin-block-picker">
                    <label htmlFor={`block-duration-${targetUserId}`}>
                        차단 기간
                    </label>

                    <select
                        id={`block-duration-${targetUserId}`}
                        value={duration}
                        onChange={(event) => setDuration(event.target.value)}
                        disabled={submitting}
                    >
                        {BLOCK_DURATIONS.map((item) => (
                            <option key={item.value} value={item.value}>
                                {item.label}
                            </option>
                        ))}
                    </select>

                    <button
                        type="button"
                        className="danger-button"
                        onClick={handleBlock}
                        disabled={submitting}
                    >
                        {submitting ? '차단 중...' : '차단 확정'}
                    </button>

                    <button
                        type="button"
                        onClick={() => setSelecting(false)}
                        disabled={submitting}
                    >
                        취소
                    </button>
                </span>
            )}

            {error && (
                <span className="admin-block-error" role="alert">
                    {error}
                </span>
            )}

            {message && (
                <span className="admin-block-message" role="status">
                    {message}
                </span>
            )}
        </span>
    )
}

export default AdminBlockControl