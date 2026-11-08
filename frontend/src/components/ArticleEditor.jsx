import { useEffect } from 'react'
import { EditorContent, useEditor, useEditorState } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'

function ArticleEditor({
                           initialContent = '',
                           onChange,
                           disabled = false,
                       }) {
    // 에디터 생성 및 사용할 기능 설정
    const editor = useEditor({
        extensions: [
            StarterKit.configure({
                // 링크 기능은 나중에 별도로 구성
                link: false,
            }),
        ],

        // 처음 에디터를 열었을 때 표시할 내용
        content: initialContent,

        // 등록 중이거나 로그인하지 않은 상태에서는 편집 금지
        editable: !disabled,

        // 내용이 바뀌면 부모 화면에 HTML 문자열 전달
        onUpdate: ({ editor }) => {
            onChange(editor.isEmpty ? '' : editor.getHTML())
        },

        // 실제 입력 영역의 속성과 기본 디자인
        editorProps: {
            attributes: {
                role: 'textbox',
                'aria-label': '게시글 내용',
                'aria-multiline': 'true',
                style: [
                    'min-height: 280px',
                    'padding: 16px',
                    'border: 1px solid #cbd5ce',
                    'border-radius: 6px',
                    'overflow-wrap: anywhere',
                ].join(';'),
            },
        },
    })

    // 커서 위치에 적용된 서식을 확인
    // 예: 굵은 글씨 안으로 커서를 옮기면 굵게 버튼도 선택 상태로 변경
    const format = useEditorState({
        editor,
        selector: ({ editor }) => ({
            bold: editor?.isActive('bold') ?? false,
            italic: editor?.isActive('italic') ?? false,
            bulletList: editor?.isActive('bulletList') ?? false,
        }),
    })

    // 부모 화면의 disabled 값이 바뀌면 에디터에도 반영
    useEffect(() => {
        if (editor) {
            editor.setEditable(!disabled)
        }
    }, [editor, disabled])

    if (!editor) {
        return <p>에디터를 준비하고 있습니다.</p>
    }

    return (
        <div className="article-rich-editor">
            <div
                role="group"
                aria-label="본문 서식"
                style={{
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: '8px',
                    marginBottom: '8px',
                }}
            >
                <button
                    type="button"
                    disabled={disabled}
                    aria-pressed={format?.bold ?? false}
                    onClick={() =>
                        editor.chain().focus().toggleBold().run()
                    }
                >
                    굵게
                </button>

                <button
                    type="button"
                    disabled={disabled}
                    aria-pressed={format?.italic ?? false}
                    onClick={() =>
                        editor.chain().focus().toggleItalic().run()
                    }
                >
                    기울임
                </button>

                <button
                    type="button"
                    disabled={disabled}
                    aria-pressed={format?.bulletList ?? false}
                    onClick={() =>
                        editor.chain().focus().toggleBulletList().run()
                    }
                >
                    목록
                </button>
            </div>

            {/* Tiptap의 실제 본문 입력 영역 */}
            <EditorContent editor={editor} />
        </div>
    )
}

export default ArticleEditor