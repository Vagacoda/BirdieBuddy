import { BrowserRouter, Link, Route, Routes } from 'react-router-dom'
import HomePage from './pages/HomePage.jsx'
import BoardPage from './pages/BoardPage.jsx'

function App() {
  return (
      <BrowserRouter>
        <Routes>
          {/* 홈페이지 */}
          <Route path="/" element={<HomePage />} />

          {/* 전체 게시판 화면 */}
          <Route path="/board" element={<BoardPage />} />

          {/* 등록되지 않은 화면 주소 */}
          <Route
              path="*"
              element={
                <main>
                  <h1>페이지를 찾을 수 없습니다.</h1>
                  <Link to="/">홈으로</Link>
                </main>
              }
          />
        </Routes>
      </BrowserRouter>
  )
}

export default App