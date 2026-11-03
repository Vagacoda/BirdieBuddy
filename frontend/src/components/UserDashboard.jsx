import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

// 이 탭에서 사용할 로그인 토큰의 저장 이름
const TOKEN_KEY = 'birdiebuddy.accessToken'

// 인증된 회원의 정보를 가져오는 함수
async function fetchMyInfo(token, signal) {
    const response = await fetch('/api/users/me', {
        headers: {
            Authorization: `Bearer ${token}`,
        },
        signal,
    })

    if (!response.ok) {
        const error = new Error(
            response.status === 401
                ? '로그인이 만료되었습니다. 다시 로그인해 주세요.'
                : response.status === 403
                    ? '이용할 수 없는 계정입니다.'
                    : '회원 정보를 불러오지 못했습니다.'
        )

        error.status = response.status
        throw error
    }

    return response.json()
}

function UserDashboard() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')

    // 서버에서 확인한 내 정보
    const [user, setUser] = useState(null)

    // 저장된 토큰이 있으면 처음에 로그인 상태를 확인
    const [checking, setChecking] = useState(
        () => Boolean(sessionStorage.getItem(TOKEN_KEY))
    )

    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')

    useEffect(() => {
        const token = sessionStorage.getItem(TOKEN_KEY)

        if (!token) {
            return
        }

        // 화면을 떠나면 진행 중인 요청 취소
        const controller = new AbortController()

        async function restoreLogin() {
            try {
                const myInfo = await fetchMyInfo(token, controller.signal)

                if (!controller.signal.aborted) {
                    setUser(myInfo)
                }
            } catch (error) {
                if (controller.signal.aborted) {
                    return
                }

                // 인증 실패 또는 이용 불가 계정이면 저장된 토큰 제거
                if (error.status === 401 || error.status === 403) {
                    sessionStorage.removeItem(TOKEN_KEY)
                }

                setError(error.message)
            } finally {
                if (!controller.signal.aborted) {
                    setChecking(false)
                }
            }
        }

        restoreLogin()

        return () => {
            controller.abort()
        }
    }, [])

    async function handleLogin(event) {
        // form 제출 시 브라우저가 새로고침하지 않도록 함
        event.preventDefault()

        if (submitting) {
            return
        }

        setSubmitting(true)
        setError('')

        try {
            // 이메일과 비밀번호로 로그인 요청
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email.trim(),
                    password,
                }),
            })

            // 오류 응답에 JSON이 없어도 처리
            const data = await response.json().catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message || '로그인에 실패했습니다.'
                )
            }

            if (!data.accessToken) {
                throw new Error('로그인 토큰을 받지 못했습니다.')
            }

            // 로그인으로 받은 토큰으로 내 정보 조회
            const myInfo = await fetchMyInfo(data.accessToken)

            // 비밀번호는 저장하지 않고 토큰만 저장
            sessionStorage.setItem(TOKEN_KEY, data.accessToken)

            setUser(myInfo)
            setPassword('')
        } catch (error) {
            setError(error.message)
        } finally {
            setSubmitting(false)
        }
    }

    function handleLogout() {
        // 이 브라우저 탭에 저장한 로그인 정보 제거
        sessionStorage.removeItem(TOKEN_KEY)

        setUser(null)
        setPassword('')
        setError('')
    }

    return (
        <aside className="panel user-panel">
            <h2>유저 대시보드</h2>

            {checking ? (
                <p role="status">로그인 상태를 확인하고 있습니다.</p>
            ) : user ? (
                <div className="dashboard-profile">
                    <p>
                        <strong>{user.nickname}</strong>님, 안녕하세요.
                    </p>

                    <p className="dashboard-email">{user.email}</p>
                    <Link className="dashboard-profile-link" to={`/users/${user.id}`}>
                        내 프로필
                    </Link>

                    <button type="button" onClick={handleLogout}>
                        로그아웃
                    </button>
                </div>
            ) : (
                <form className="dashboard-login" onSubmit={handleLogin}>
                    <label htmlFor="dashboard-email">이메일</label>
                    <input
                        id="dashboard-email"
                        type="email"
                        autoComplete="username"
                        placeholder="이메일을 입력하세요"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        disabled={submitting}
                        required
                    />

                    <label htmlFor="dashboard-password">비밀번호</label>
                    <input
                        id="dashboard-password"
                        type="password"
                        autoComplete="current-password"
                        placeholder="비밀번호를 입력하세요"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        disabled={submitting}
                        required
                    />

                    <button type="submit" disabled={submitting}>
                        {submitting ? '로그인 중...' : '로그인'}
                    </button>
                </form>
            )}

            {error && (
                <p className="dashboard-error" role="alert">
                    {error}
                </p>
            )}
        </aside>
    )
}

export default UserDashboard
