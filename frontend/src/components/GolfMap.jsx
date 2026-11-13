import { useEffect, useRef, useState } from 'react'

function GolfMap({ courses }) {
    const mapElement = useRef(null)
    const mapRef = useRef(null)
    const [error, setError] = useState('')

    // 지도를 처음 한 번 생성
    useEffect(() => {
        if (!window.naver?.maps || !mapElement.current) {
            setError('네이버 지도를 불러오지 못했습니다.')
            return
        }

        const center = new window.naver.maps.LatLng(
            37.5464155,
            126.7935949
        )

        const map = new window.naver.maps.Map(mapElement.current, {
            center,
            zoom: 11,
        })

        mapRef.current = map

        return () => {
            mapRef.current = null
        }
    }, [])

    // 골프장 목록이 준비되면 마커 생성
    useEffect(() => {
        const map = mapRef.current

        if (!map || !window.naver?.maps) {
            return
        }

        const infoWindow = new window.naver.maps.InfoWindow()
        const markers = []
        const listeners = []

        courses.forEach((course) => {
            const position = new window.naver.maps.LatLng(
                Number(course.latitude),
                Number(course.longitude)
            )

            const marker = new window.naver.maps.Marker({
                map,
                position,
                title: course.name,
            })

            markers.push(marker)

            function showInfo() {
                // 골프장 이름을 클릭하면 네이버 지도 검색으로 이동
                const content = document.createElement('div')
                content.style.padding = '10px'
                content.style.whiteSpace = 'nowrap'

                const link = document.createElement('a')
                link.textContent = course.name
                link.href =
                    'https://map.naver.com/p/search/' +
                    encodeURIComponent(`${course.name} ${course.address}`)
                link.target = '_blank'
                link.rel = 'noopener noreferrer'

                content.appendChild(link)

                infoWindow.setContent(content)
                infoWindow.open(map, marker)
            }

            // PC에서는 마우스를 올리면 이름 표시
            listeners.push(
                window.naver.maps.Event.addListener(
                    marker,
                    'mouseover',
                    showInfo
                )
            )

            // 모바일에서는 마커를 눌러 이름 표시
            listeners.push(
                window.naver.maps.Event.addListener(
                    marker,
                    'click',
                    showInfo
                )
            )
        })

        return () => {
            infoWindow.close()
            listeners.forEach((listener) => {
                window.naver.maps.Event.removeListener(listener)
            })
            markers.forEach((marker) => marker.setMap(null))
        }
    }, [courses])

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