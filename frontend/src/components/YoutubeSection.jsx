import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import YoutubeVideoList from './YoutubeVideoList.jsx'

function YoutubeSection() {
    const [videos, setVideos] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        const controller = new AbortController()

        async function loadVideos() {
            try {
                const response = await fetch(
                    '/api/youtube?page=0&size=10',
                    { signal: controller.signal }
                )

                if (!response.ok) {
                    throw new Error('영상을 불러오지 못했습니다.')
                }

                const data = await response.json()

                if (!controller.signal.aborted) {
                    setVideos(data.videos)
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
    }, [])

    return (
        <section className="panel youtube-panel">
            <div className="panel-heading">
                <h2>유튜브</h2>
                <Link to="/youtube">더 보기</Link>
            </div>

            {loading && <p role="status">영상을 불러오는 중입니다.</p>}
            {error && <p role="alert">{error}</p>}

            {!loading && !error && (
                <YoutubeVideoList videos={videos} />
            )}
        </section>
    )
}

export default YoutubeSection