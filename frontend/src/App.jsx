import { BrowserRouter, Link, Route, Routes } from 'react-router-dom'
import HomePage from './pages/HomePage.jsx'
import BoardPage from './pages/BoardPage.jsx'
import ArticleDetailPage from './pages/ArticleDetailPage.jsx'
import ArticleCreatePage from './pages/ArticleCreatePage.jsx'

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<HomePage />} />

                <Route path="/board" element={<BoardPage />} />

                {/* 게시글 작성 */}
                <Route path="/board/new" element={<ArticleCreatePage />} />

                {/* 게시글 상세 */}
                <Route
                    path="/board/:articleId"
                    element={<ArticleDetailPage />}
                />

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