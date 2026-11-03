import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import '../App.css'

const TOKEN_KEY = 'birdiebuddy.accessToken'
const PASSWORD_RULE =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,20}$/

async function requestJson(url, token, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...(options.body ? { 'Content-Type': 'application/json' } : {}),
            Authorization: `Bearer ${token}`,
        },
    })

    const data = await response.json().catch(() => ({}))

    if (!response.ok) {
        const error = new Error(data.message || '요청을 처리하지 못했습니다.')
        error.status = response.status
        throw error
    }

    return data
}

function AccountEditPage() {
    const [user, setUser] = useState(null)
    const [nickname, setNickname] = useState('')
    const [currentPassword, setCurrentPassword] = useState('')
    const [newPassword, setNewPassword] = useState('')
    const [passwordConfirm, setPasswordConfirm] = useState('')
    const [loading, setLoading] = useState(true)
    const [busy, setBusy] = useState(false)
    const [error, setError] = useState('')
    const [notice, setNotice] = useState('')
    const [needsLogin, setNeedsLogin] = useState(false)

    useEffect(() => {
        let ignore = false
        const token = sessionStorage.getItem(TOKEN_KEY)

        if (!token) {
            setNeedsLogin(true)
            setLoading(false)
            return
        }

        // 저장된 토큰을 서버가 검증한 뒤 내 정보를 보여 줌
        requestJson('/api/users/me', token)
            .then((me) => {
                if (ignore) return
                setUser(me)
                setNickname(me.nickname)
            })
            .catch((caught) => {
                if (ignore) return

                if (caught.status === 401 || caught.status === 403) {
                    sessionStorage.removeItem(TOKEN_KEY)
                    setNeedsLogin(true)
                } else {
                    setError(caught.message)
                }
            })
            .finally(() => {
                if (!ignore) setLoading(false)
            })

        return () => {
            ignore = true
        }
    }, [])

    async function submitUpdate(url, body, onSuccess) {
        if (busy) return

        setBusy(true)
        setError('')
        setNotice('')

        try {
            const token = sessionStorage.getItem(TOKEN_KEY)

            if (!token) {
                setNeedsLogin(true)
                return
            }

            const data = await requestJson(url, token, {
                method: 'PATCH',
                body: JSON.stringify(body),
            })

            onSuccess(data)
        } catch (caught) {
            if (caught.status === 401 || caught.status === 403) {
                sessionStorage.removeItem(TOKEN_KEY)
                setNeedsLogin(true)
            } else {
                setError(caught.message)
            }
        } finally {
            setBusy(false)
        }
    }

    function updateNickname(event) {
        event.preventDefault()
        const cleanNickname = nickname.trim()

        if (!cleanNickname || cleanNickname.length > 16) {
            setError('닉네임을 1~16자로 입력해 주세요.')
            return
        }

        submitUpdate(
            '/api/users/me/nickname',
            { nickname: cleanNickname },
            (updated) => {
                setUser(updated)
                setNickname(updated.nickname)
                setNotice('닉네임이 변경되었습니다.')
            }
        )
    }

    function updatePassword(event) {
        event.preventDefault()

        if (!PASSWORD_RULE.test(newPassword)) {
            setError(
                '새 비밀번호는 8~20자이며 대문자, 소문자, 숫자, 특수문자를 각각 포함해야 합니다.'
            )
            return
        }

        if (newPassword !== passwordConfirm) {
            setError('새 비밀번호 확인이 일치하지 않습니다.')
            return
        }

        submitUpdate(
            '/api/users/me/password',
            { currentPassword, newPassword },
            () => {
                setCurrentPassword('')
                setNewPassword('')
                setPasswordConfirm('')

                // 비밀번호 변경 뒤 기존 JWT는 사용할 수 없음
                sessionStorage.removeItem(TOKEN_KEY)
                setNeedsLogin(true)
                setNotice('비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인해 주세요.')
            }
        )
    }

    return (
        <main className="board-page account-page">
            <nav>
                <Link to="/">홈으로</Link>
                {user && (
                    <>
                        {' / '}
                        <Link to={`/users/${user.id}`}>내 프로필</Link>
                    </>
                )}
            </nav>

            <section className="account-card">
                <h1>개인정보 수정</h1>

                {loading && <p role="status">내 정보를 불러오는 중입니다.</p>}

                {needsLogin ? (
                    <div className="account-success">
                        <p>{notice || '로그인이 필요합니다.'}</p>
                        <Link to="/">홈에서 로그인하기</Link>
                    </div>
                ) : user ? (
                    <>
                        <div className="account-readonly">
                            <strong>이메일</strong>
                            <p>{user.email}</p>
                            <small>이메일 변경은 재인증 기능이 준비되면 제공됩니다.</small>
                        </div>

                        <form className="account-form" onSubmit={updateNickname}>
                            <h2>닉네임 변경</h2>
                            <label htmlFor="edit-nickname">새 닉네임</label>
                            <input
                                id="edit-nickname"
                                maxLength={16}
                                value={nickname}
                                onChange={(event) => setNickname(event.target.value)}
                                disabled={busy}
                                required
                            />
                            <button type="submit" disabled={busy}>
                                닉네임 저장
                            </button>
                        </form>

                        <form className="account-form" onSubmit={updatePassword}>
                            <h2>비밀번호 변경</h2>

                            <label htmlFor="edit-current-password">현재 비밀번호</label>
                            <input
                                id="edit-current-password"
                                type="password"
                                autoComplete="current-password"
                                maxLength={20}
                                value={currentPassword}
                                onChange={(event) => setCurrentPassword(event.target.value)}
                                disabled={busy}
                                required
                            />

                            <label htmlFor="edit-new-password">새 비밀번호</label>
                            <input
                                id="edit-new-password"
                                type="password"
                                autoComplete="new-password"
                                value={newPassword}
                                onChange={(event) => setNewPassword(event.target.value)}
                                disabled={busy}
                                required
                            />

                            <p className="account-help">
                                8~20자, 영문 대·소문자, 숫자, 특수문자(!@#$%^&*) 포함
                            </p>

                            <label htmlFor="edit-confirm-password">새 비밀번호 확인</label>
                            <input
                                id="edit-confirm-password"
                                type="password"
                                autoComplete="new-password"
                                value={passwordConfirm}
                                onChange={(event) => setPasswordConfirm(event.target.value)}
                                disabled={busy}
                                required
                            />

                            <button type="submit" disabled={busy}>
                                비밀번호 변경
                            </button>
                        </form>
                    </>
                ) : null}

                {error && <p role="alert">{error}</p>}
                {!needsLogin && notice && <p role="status">{notice}</p>}
            </section>
        </main>
    )
}

export default AccountEditPage