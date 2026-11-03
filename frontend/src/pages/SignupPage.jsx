import { useState } from 'react'
import { Link } from 'react-router-dom'
import '../App.css'

const PASSWORD_RULE =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,20}$/

async function requestJson(url, options) {
    const response = await fetch(url, options)
    const data = await response.json().catch(() => ({}))

    if (!response.ok) {
        throw new Error(data.message || '요청을 처리하지 못했습니다.')
    }

    return data
}

function SignupPage() {
    const [email, setEmail] = useState('')
    const [checkedEmail, setCheckedEmail] = useState('')
    const [sentEmail, setSentEmail] = useState('')
    const [verifiedEmail, setVerifiedEmail] = useState('')
    const [code, setCode] = useState('')
    const [nickname, setNickname] = useState('')
    const [password, setPassword] = useState('')
    const [passwordConfirm, setPasswordConfirm] = useState('')
    const [busy, setBusy] = useState(false)
    const [error, setError] = useState('')
    const [notice, setNotice] = useState('')
    const [complete, setComplete] = useState(false)

    const cleanEmail = email.trim()
    const emailChecked = checkedEmail === cleanEmail && cleanEmail !== ''
    const codeSent = sentEmail === cleanEmail && cleanEmail !== ''
    const emailVerified = verifiedEmail === cleanEmail && cleanEmail !== ''

    function changeEmail(value) {
        setEmail(value)

        // 이메일이 달라지면 이전 중복 확인·인증 결과를 사용할 수 없음
        setCheckedEmail('')
        setSentEmail('')
        setVerifiedEmail('')
        setCode('')
        setError('')
        setNotice('')
    }

    async function run(action) {
        if (busy) return

        setBusy(true)
        setError('')
        setNotice('')

        try {
            await action()
        } catch (caught) {
            setError(caught.message || '요청을 처리하지 못했습니다.')
        } finally {
            setBusy(false)
        }
    }

    function checkEmail() {
        run(async () => {
            if (
                !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanEmail) ||
                cleanEmail.length > 100
            ) {
                throw new Error('올바른 이메일을 100자 이내로 입력해 주세요.')
            }

            const data = await requestJson(
                `/api/users/email-availability?email=${encodeURIComponent(cleanEmail)}`
            )

            if (!data.available) {
                throw new Error(data.message || '이미 사용 중인 이메일입니다.')
            }

            setCheckedEmail(cleanEmail)
            setNotice('사용 가능한 이메일입니다.')
        })
    }

    function sendCode() {
        run(async () => {
            if (!emailChecked) {
                throw new Error('이메일 중복 확인을 먼저 해 주세요.')
            }

            setSentEmail('')
            setVerifiedEmail('')

            const data = await requestJson('/api/auth/email-verifications/send', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: cleanEmail }),
            })

            setSentEmail(cleanEmail)
            setNotice(data.message || '인증번호를 보냈습니다.')
        })
    }

    function confirmCode() {
        run(async () => {
            if (!codeSent) {
                throw new Error('인증번호를 먼저 전송해 주세요.')
            }

            if (!/^\d{6}$/.test(code)) {
                throw new Error('인증번호 숫자 6자리를 입력해 주세요.')
            }

            const data = await requestJson('/api/auth/email-verifications/confirm', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: cleanEmail, code }),
            })

            setVerifiedEmail(cleanEmail)
            setNotice(data.message || '이메일 인증이 완료되었습니다.')
        })
    }

    function signup(event) {
        event.preventDefault()

        run(async () => {
            if (!emailVerified) {
                throw new Error('이메일 인증을 완료해 주세요.')
            }

            if (!nickname.trim() || nickname.trim().length > 16) {
                throw new Error('닉네임을 1~16자로 입력해 주세요.')
            }

            if (!PASSWORD_RULE.test(password)) {
                throw new Error(
                    '비밀번호는 8~20자이며 대문자, 소문자, 숫자, 특수문자를 각각 포함해야 합니다.'
                )
            }

            if (password !== passwordConfirm) {
                throw new Error('비밀번호 확인이 일치하지 않습니다.')
            }

            await requestJson('/api/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    email: cleanEmail,
                    nickname: nickname.trim(),
                    password,
                }),
            })

            setPassword('')
            setPasswordConfirm('')
            setComplete(true)
        })
    }

    return (
        <main className="board-page account-page">
            <nav>
                <Link to="/">홈으로</Link>
            </nav>

            <section className="account-card">
                <h1>회원가입</h1>

                {complete ? (
                    <div className="account-success" role="status">
                        <p>회원가입이 완료되었습니다. 이제 로그인할 수 있습니다.</p>
                        <Link to="/">홈에서 로그인하기</Link>
                    </div>
                ) : (
                    <form className="account-form" onSubmit={signup}>
                        <label htmlFor="signup-email">이메일</label>
                        <div className="account-inline">
                            <input
                                id="signup-email"
                                type="email"
                                autoComplete="email"
                                maxLength={100}
                                value={email}
                                onChange={(event) => changeEmail(event.target.value)}
                                disabled={busy}
                                required
                            />
                            <button
                                type="button"
                                onClick={checkEmail}
                                disabled={busy || !cleanEmail}
                            >
                                중복 확인
                            </button>
                            <button
                                type="button"
                                onClick={sendCode}
                                disabled={busy || !emailChecked}
                            >
                                인증번호 전송
                            </button>
                        </div>

                        <label htmlFor="signup-code">인증번호</label>
                        <div className="account-inline">
                            <input
                                id="signup-code"
                                inputMode="numeric"
                                maxLength={6}
                                value={code}
                                onChange={(event) => setCode(event.target.value)}
                                disabled={busy || !codeSent || emailVerified}
                            />
                            <button
                                type="button"
                                onClick={confirmCode}
                                disabled={busy || !codeSent || emailVerified}
                            >
                                확인
                            </button>
                        </div>

                        {emailVerified && <p className="account-help">이메일 인증 완료</p>}

                        <label htmlFor="signup-nickname">닉네임</label>
                        <input
                            id="signup-nickname"
                            maxLength={16}
                            value={nickname}
                            onChange={(event) => setNickname(event.target.value)}
                            disabled={busy}
                            required
                        />

                        <label htmlFor="signup-password">비밀번호</label>
                        <input
                            id="signup-password"
                            type="password"
                            autoComplete="new-password"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            disabled={busy}
                            required
                        />
                        <p className="account-help">
                            8~20자, 영문 대·소문자, 숫자, 특수문자(!@#$%^&*) 포함
                        </p>

                        <label htmlFor="signup-password-confirm">비밀번호 확인</label>
                        <input
                            id="signup-password-confirm"
                            type="password"
                            autoComplete="new-password"
                            value={passwordConfirm}
                            onChange={(event) => setPasswordConfirm(event.target.value)}
                            disabled={busy}
                            required
                        />

                        {error && <p role="alert">{error}</p>}
                        {notice && <p role="status">{notice}</p>}

                        <button type="submit" disabled={busy || !emailVerified}>
                            {busy ? '처리 중...' : '회원가입'}
                        </button>
                    </form>
                )}
            </section>
        </main>
    )
}

export default SignupPage