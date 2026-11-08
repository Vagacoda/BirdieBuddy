import DOMPurify from 'dompurify'

function ArticleBody({ content = '', contentFormat = 'TEXT' }) {
    // 기존 일반 텍스트 게시글은 그대로 표시
    if (contentFormat !== 'HTML') {
        return (
            <div
                style={{
                    whiteSpace: 'pre-wrap',
                    overflowWrap: 'anywhere',
                }}
            >
                {content}
            </div>
        )
    }

    // HTML 게시글은 허용한 태그와 속성만 남긴 뒤 표시
    const safeHtml = DOMPurify.sanitize(content, {
        ALLOWED_TAGS: [
            'p', 'br',
            'strong', 'b', 'em', 'i', 'u', 's',
            'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
            'ul', 'ol', 'li',
            'blockquote', 'pre', 'code', 'hr',
            'img',
        ],
        ALLOWED_ATTR: ['src', 'alt', 'title', 'start'],
        ALLOW_DATA_ATTR: false,
        ALLOW_ARIA_ATTR: false,
    })

    return (
        <div
            className="article-body"
            dangerouslySetInnerHTML={{ __html: safeHtml }}
        />
    )
}

export default ArticleBody