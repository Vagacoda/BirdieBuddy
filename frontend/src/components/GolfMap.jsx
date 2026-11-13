import { useEffect, useRef, useState } from 'react'

function GolfMap() {
    // 지도를 표시할 HTML 요소
    const mapElement = useRef(null)

    // 지도 로딩에 실패했을 때 보여 줄 메시지
    const [error, setError] = useState('')

    useEffect(() => {
        if (!window.naver?.maps || !mapElement.current) {
            setError('네이버 지도를 불러오지 못했습니다.')
            return
        }

        // 지도 중심: 인서울27골프클럽
        const center = new window.naver.maps.LatLng(
            37.5464155,
            126.7935949
        )

        new window.naver.maps.Map(mapElement.current, {
            center,
            zoom: 11,
        })
    }, [])

    return (
        <>
            {error && <p role="alert">{error}</p>}

            <div
                ref={mapElement}
                style={{ width: '100%', height: '400px' }}
            />
        </>
    )
}

export default GolfMap