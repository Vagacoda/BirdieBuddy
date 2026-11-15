import { useEffect, useRef, useState } from 'react'

// 골프장 좌표를 중심으로 네이버 지도 열기
function getNaverMapUrl(course) {
    const radius = 6378137
    const latitude = Number(course.latitude) * Math.PI / 180
    const x = radius * Number(course.longitude) * Math.PI / 180
    const y = radius * Math.log(Math.tan(Math.PI / 4 + latitude / 2))
    return `https://map.naver.com/p/entry/address/${x},${y},${encodeURIComponent(course.address || course.name)}?c=16,0,0,0,dh`
}

function GolfMap({ courses, myLocation, userLocation }) {
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

    // 위치를 허용하면 지도 중심을 현재 위치로 이동
    useEffect(() => {
        if (!myLocation || !mapRef.current || !window.naver?.maps) {
            return
        }

        mapRef.current.setCenter(
            new window.naver.maps.LatLng(
                myLocation.latitude,
                myLocation.longitude
            )
        )
    }, [myLocation])

    // 현재 위치가 없으면 학교를 기준 위치로 표시
    useEffect(() => {
        if (!myLocation || !mapRef.current || !window.naver?.maps) {
            return
        }

        const markerLabel = userLocation ? '내 위치' : '기준 위치: 인하공업전문대학'
        const marker = new window.naver.maps.Marker({
            map: mapRef.current,
            position: new window.naver.maps.LatLng(
                myLocation.latitude,
                myLocation.longitude
            ),
            title: markerLabel,
            zIndex: 1000,
            icon: {
                content: `<div role="img" aria-label="${markerLabel}" style="width:18px;height:18px;background:#dc2626;border:3px solid white;border-radius:50%;box-shadow:0 1px 6px rgba(0,0,0,.4)"></div>`,
                anchor: new window.naver.maps.Point(12, 12),
            },
        })

        return () => marker.setMap(null)
    }, [myLocation, userLocation])

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
                // 골프장 이름을 클릭하면 해당 좌표의 네이버 지도로 이동
                const content = document.createElement('div')
                content.style.padding = '10px'
                content.style.whiteSpace = 'nowrap'

                const link = document.createElement('a')
                link.textContent = course.name
                link.href = getNaverMapUrl(course)
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
