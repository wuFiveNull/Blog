(function () {
    const form = document.getElementById('post-form');
    if (!form) return;

    const rolesInput = document.getElementById('allowedRoles');
    const roleChoices = Array.from(document.querySelectorAll('.role-choice'));
    const content = document.getElementById('contentMarkdown');
    const preview = document.getElementById('preview');
    const previewButton = document.getElementById('preview-button');
    const csrf = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');

    function syncRoles() {
        rolesInput.value = roleChoices.filter(item => item.checked).map(item => item.value).join(',');
    }

    const initialRoles = (rolesInput.value || '').split(',');
    roleChoices.forEach(item => {
        item.checked = initialRoles.includes(item.value);
        item.addEventListener('change', syncRoles);
    });
    syncRoles();

    async function refreshPreview() {
        const body = new URLSearchParams();
        body.set('contentMarkdown', content.value);
        const response = await fetch('/posts/preview', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader.content]: csrf.content
            },
            body
        });
        preview.innerHTML = await response.text();
    }

    previewButton.addEventListener('click', refreshPreview);

    document.getElementById('upload-button').addEventListener('click', async function () {
        const input = document.getElementById('image-file');
        const message = document.getElementById('upload-message');
        if (!input.files.length) {
            message.textContent = '请先选择图片';
            return;
        }
        const body = new FormData();
        body.append('file', input.files[0]);
        message.textContent = '上传中...';
        const response = await fetch('/files/upload', {
            method: 'POST',
            headers: { [csrfHeader.content]: csrf.content },
            body
        });
        if (!response.ok) {
            message.textContent = '上传失败';
            return;
        }
        const result = await response.json();
        const start = content.selectionStart;
        const markdown = '![图片说明](' + result.url + ')';
        content.value = content.value.slice(0, start) + markdown + content.value.slice(content.selectionEnd);
        content.focus();
        content.selectionStart = content.selectionEnd = start + markdown.length;
        message.textContent = '图片已插入';
    });

    form.addEventListener('submit', syncRoles);
})();
