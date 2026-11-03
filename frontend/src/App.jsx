import {BrowserRouter, Link, Route, Routes} from 'react-router-dom'
import HomePage from './pages/HomePage.jsx'
import BoardPage from './pages/BoardPage.jsx'
import ArticleDetailPage from './pages/ArticleDetailPage.jsx'
import ArticleCreatePage from './pages/ArticleCreatePage.jsx'
import UserProfilePage from './pages/UserProfilePage.jsx'
import SignupPage from './pages/SignupPage.jsx'
import AccountEditPage from './pages/AccountEditPage.jsx'

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<HomePage/>}/>

                <Route path="/board" element={<BoardPage/>}/>

                <Route path="/board/new" element={<ArticleCreatePage/>}/>

                <Route
                    path="/board/:articleId"
                    element={<ArticleDetailPage/>}
                />

                {/* 내 프로필과 다른 사람 프로필에서 공통 사용 */}
                <Route
                    path="/users/:userId"
                    element={<UserProfilePage/>}
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

                <Route path="/signup" element={<SignupPage/>}/>

                <Route path="/users/me/edit" element={<AccountEditPage/>}/>
            </Routes>
        </BrowserRouter>
    )
}

export default App
