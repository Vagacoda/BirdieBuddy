import { useEffect, useRef, useState } from 'react'
import Image from '@tiptap/extension-image'
import { EditorContent, useEditor, useEditorState } from '@tiptap/react'

import StarterKit from '@tiptap/starter-kit'

function ArticleEditor({
    initialContent = '',
    onChange,
    disabled = false,
    onUploadingChange
}) {
    // 숨겨진 파일 선택창을 버튼에서 열기 위해 사용
    const fileInputRef = useRef(null)

    // 업로드 중복 실행 방지
    const uploadingRef = useRef(false)

    const [uploading, setUploading] = useState(false)
    const [uploadError, setUploadError] = useState('')


    // 에디터 생성 및 사용할 기능 설정
    const editor = useEditor({
        extensions: [
            StarterKit.configure({
                // 링크 기능은 나중에 별도로 구성
                link: false,
            }),
            Image.configure({
                allowBase64: false,
            }),
        ],

        // 처음 에디터를 열었을 때 표시할 내용
        content: initialContent,

        // 등록 중이거나 로그인하지 않은 상태에서는 편집 금지
        editable: !disabled,

        // 내용이 바뀌면 부모 화면에 HTML 문자열 전달
        onUpdate: ({ editor }) => {
            // 공백을 제외한 글자가 있는지 확인
            const hasText = editor.getText().trim().length > 0

            // 이미지로만 작성한 게시글도 허용
            let hasImage = false

            editor.state.doc.descendants((node) => {
                if (node.type.name === 'image') {
                    hasImage = true
                }
            })

            onChange(hasText || hasImage ? editor.getHTML() : '')
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
            editor.setEditable(!disabled && !uploading)
        }
    }, [editor, disabled, uploading])

    async function handleImageUpload(event) {
        const file = event.target.files?.[0]

        // 같은 파일을 다시 선택해도 change 이벤트가 발생하도록 초기화
        event.target.value = ''

        if (!file || !editor || disabled || uploadingRef.current) {
            return
        }

        setUploadError('')

        if (file.size > 5 * 1024 * 1024) {
            setUploadError('이미지는 5MB 이하만 업로드할 수 있습니다.')
            return
        }

        const token = sessionStorage.getItem('birdiebuddy.accessToken')

        if (!token) {
            setUploadError('로그인 후 이미지를 업로드해 주세요.')
            return
        }

        uploadingRef.current = true
        setUploading(true)
        onUploadingChange?.(true)

        try {
            const formData = new FormData()
            formData.append('image', file)

            const response = await fetch('/api/images', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
                // Content-Type은 브라우저가 자동으로 설정
                body: formData,
            })

            const data = await response.json().catch(() => ({}))

            if (response.status === 401) {
                throw new Error(
                    '로그인이 만료되었습니다. 작성 내용을 복사한 뒤 다시 로그인해 주세요.'
                )
            }

            if (response.status === 413) {
                throw new Error('업로드 가능한 파일 용량을 초과했습니다.')
            }

            if (!response.ok) {
                throw new Error(
                    data.message || '이미지를 업로드하지 못했습니다.'
                )
            }

            // 우리 이미지 조회 API 주소인지 확인
            if (
                typeof data.url !== 'string' ||
                !/^\/api\/images\/[1-9]\d*$/.test(data.url)
            ) {
                throw new Error('이미지 주소를 확인하지 못했습니다.')
            }

            // 업로드 도중 다른 화면으로 이동했다면 삽입하지 않음
            if (editor.isDestroyed) {
                return
            }

            const inserted = editor
                .chain()
                .focus()
                .setImage({
                    src: data.url,
                    alt: file.name,
                })
                .run()

            if (!inserted) {
                throw new Error('이미지를 본문에 삽입하지 못했습니다.')
            }
        } catch (error) {
            if (!editor.isDestroyed) {
                setUploadError(error.message)
            }
        } finally {
            uploadingRef.current = false
            setUploading(false)
            onUploadingChange?.(false)
        }
    }

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
                    disabled={disabled || uploading}
                    aria-pressed={format?.bold ?? false}
                    onClick={() =>
                        editor.chain().focus().toggleBold().run()
                    }
                >
                    굵게
                </button>

                <button
                    type="button"
                    disabled={disabled || uploading}
                    aria-pressed={format?.italic ?? false}
                    onClick={() =>
                        editor.chain().focus().toggleItalic().run()
                    }
                >
                    기울임
                </button>

                <button
                    type="button"
                    disabled={disabled || uploading}
                    aria-pressed={format?.bulletList ?? false}
                    onClick={() =>
                        editor.chain().focus().toggleBulletList().run()
                    }
                >
                    목록
                </button>
                <button
                    type="button"
                    disabled={disabled || uploading}
                    onClick={() => fileInputRef.current?.click()}
                >
                    {uploading ? '이미지 업로드 중...' : '이미지'}
                </button>

                <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/jpeg,image/png,image/gif"
                    hidden
                    disabled={disabled || uploading}
                    onChange={handleImageUpload}
                />
            </div>

            {/* Tiptap의 실제 본문 입력 영역 */}
            <EditorContent editor={editor} />
            {uploadError && <p role="alert">{uploadError}</p>}
        </div>
    )
}

export default ArticleEditor