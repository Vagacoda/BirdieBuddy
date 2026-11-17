import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import YoutubeVideoList from '../components/YoutubeVideoList.jsx'
import '../App.css'

function YoutubePage() {
    const [page, setPage] = useState(0)
    const [data, setData] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [isAdmin, setIsAdmin] = useState(false)
    const [reloadKey, setReloadKey] = useState(0)
    const [deletingId, setDeletingId] = useState(null)
    const [deleteError, setDeleteError] = useState('')

    // 관리자에게만 영상 등록 링크 표시
    useEffect(() => {
        const token = sessionStorage.getItem('birdiebuddy.accessToken')
        if (!token) return

        const controller = new AbortController()

        async function checkAdmin() {
            try {
                const response = await fetch('/api/users/me', {
                    headers: { Authorization: `Bearer ${token}` },
                    signal: controller.signal,
                })

                if (!response.ok) return

                const user = await response.json()

                if (!controller.signal.aborted) {
                    setIsAdmin(user.role === 'ADMIN')
                }
            } catch {
                // 로그인 확인 실패와 관계없이 공개 영상 목록은 사용 가능
            }
        }

        checkAdmin()
        return () => controller.abort()
    }, [])

    async function deleteVideo(video) {
        if (deletingId !== null) return

        if (!window.confirm(`"${video.title}" 영상을 삭제하시겠습니까?`)) {
            return
        }

        const token = sessionStorage.getItem('birdiebuddy.accessToken')

        if (!token) {
            setDeleteError('관리자 계정으로 다시 로그인해 주세요.')
            return
        }

        setDeletingId(video.id)
        setDeleteError('')

        try {
            const response = await fetch(`/api/admin/youtube/${video.id}`, {
                method: 'DELETE',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })

            if (response.status === 401) {
                throw new Error('로그인이 만료되었습니다. 다시 로그인해 주세요.')
            }

            if (response.status === 403) {
                throw new Error('관리자만 영상을 삭제할 수 있습니다.')
            }

            // 이미 삭제된 영상도 목록을 새로 조회
            if (!response.ok && response.status !== 404) {
                throw new Error('영상을 삭제하지 못했습니다.')
            }

            setReloadKey(current => current + 1)
        } catch (error) {
            setDeleteError(error.message)
        } finally {
            setDeletingId(null)
        }
    }

    // 페이지당 영상 20개 조회
    useEffect(() => {
        const controller = new AbortController()

        async function loadVideos() {
            try {
                setLoading(true)
                setError('')

                const response = await fetch(
                    `/api/youtube?page=${page}&size=20`,
                    { signal: controller.signal }
                )

                if (!response.ok) {
                    throw new Error('영상을 불러오지 못했습니다.')
                }

                const result = await response.json()

                if (!controller.signal.aborted) {
                    // 마지막 페이지의 마지막 영상을 삭제했다면 이전 페이지로 이동
                    const lastPage = Math.max(0, result.totalPages - 1)

                    if (page > lastPage) {
                        setPage(lastPage)
                        return
                    }

                    setData(result)
                }
            } catch (error) {
                if (!controller.signal.aborted) {
                    setError(error.message)
                }
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false)
                }
            }
        }

        loadVideos()
        return () => controller.abort()
    }, [page, reloadKey])

    function changePage(nextPage) {
        setLoading(true)
        setError('')
        setPage(nextPage)
    }

    return (
        <main className="board-page">
            <Link to="/">홈으로</Link>

            <div className="panel-heading">
                <h1>골프 영상</h1>
                {isAdmin && (
                    <Link to="/youtube/new">영상 등록</Link>
                )}
            </div>

            {loading && <p role="status">영상을 불러오는 중입니다.</p>}
            {error && <p role="alert">{error}</p>}
            {deleteError && <p role="alert">{deleteError}</p>}

            {!loading && !error && data && (
                <>
                    <p>총 {data.totalElements}개 · 등록 최신순</p>

                    {/* 페이지 이동 시 펼쳐진 플레이어 초기화 */}
                    <YoutubeVideoList
                        key={`${page}-${reloadKey}`}
                        videos={data.videos}
                        onDelete={isAdmin ? deleteVideo : undefined}
                        deletingId={deletingId}
                    />

                    {data.totalPages > 0 && (
                        <nav
                            className="news-pagination"
                            aria-label="영상 페이지 이동"
                        >
                            <button
                                type="button"
                                disabled={page === 0}
                                onClick={() => changePage(page - 1)}
                            >
                                이전
                            </button>

                            <span>
                                {page + 1} / {data.totalPages}
                            </span>

                            <button
                                type="button"
                                disabled={page >= data.totalPages - 1}
                                onClick={() => changePage(page + 1)}
                            >
                                다음
                            </button>
                        </nav>
                    )}
                </>
            )}
        </main>
    )
}

export default YoutubePage