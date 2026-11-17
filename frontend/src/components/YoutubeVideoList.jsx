import { useState } from 'react'
import './YoutubeVideoList.css'

function YoutubeVideoList({ videos }) {
    // 현재 펼쳐진 영상 ID. null이면 모두 닫힌 상태
    const [activeVideoId, setActiveVideoId] = useState(null)

    function toggleVideo(videoId) {
        setActiveVideoId(current =>
            current === videoId ? null : videoId
        )
    }

    if (videos.length === 0) {
        return <p>등록된 영상이 없습니다.</p>
    }

    return (
        <ul className="youtube-video-list">
            {videos.map(video => {
                const isOpen = activeVideoId === video.videoId

                return (
                    <li key={video.id}>
                        {/* 열린 영상만 플레이어 생성 */}
                        {isOpen && (
                            <div className="youtube-player">
                                <iframe
                                    src={`https://www.youtube.com/embed/${video.videoId}`}
                                    title={video.title}
                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                    referrerPolicy="strict-origin-when-cross-origin"
                                    allowFullScreen
                                />
                            </div>
                        )}

                        {/* 썸네일 또는 제목을 누르면 열기·닫기 */}
                        <button
                            type="button"
                            className="youtube-video-button"
                            aria-expanded={isOpen}
                            onClick={() => toggleVideo(video.videoId)}
                        >
                            {!isOpen && (
                                <img
                                    src={`https://i.ytimg.com/vi/${video.videoId}/hqdefault.jpg`}
                                    alt=""
                                    loading="lazy"
                                />
                            )}

                            <span className="youtube-video-info">
                                <span className="youtube-video-title">
                                    {video.title}
                                </span>

                                <span className="youtube-channel">
                                    {video.channelName}
                                </span>

                                {isOpen && (
                                    <span className="youtube-close">
                                        접기
                                    </span>
                                )}
                            </span>
                        </button>

                        {/* 외부 재생이 제한된 영상도 유튜브에서 확인 가능 */}
                        {isOpen && (
                            <a
                                className="youtube-original-link"
                                href={`https://www.youtube.com/watch?v=${video.videoId}`}
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                유튜브에서 보기
                            </a>
                        )}
                    </li>
                )
            })}
        </ul>
    )
}

export default YoutubeVideoList