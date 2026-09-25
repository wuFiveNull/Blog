(function () {
    const form = document.getElementById('post-form');
    if (!form) return;

    const rolesInput = document.getElementById('allowedRoles');
    const roleChoices = Array.from(document.querySelectorAll('.role-choice'));
    const content = document.getElementById('contentMarkdown');
    const preview = document.getElementById('preview');
    const previewButton = document.getElementById('preview-button');
    const previewStatus = document.getElementById('preview-status');
    const csrf = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    let previewTimer;
    let previewController;

    function syncRoles() {
        rolesInput.value = roleChoices.filter(item => item.checked).map(item => item.value).join(',');
    }

    const initialRoles = (rolesInput.value || '').split(',');
    roleChoices.forEach(item => {
        item.checked = initialRoles.includes(item.value);
        item.addEventListener('change', syncRoles);
    });
    syncRoles();

    function setPreviewStatus(state, message) {
        previewStatus.dataset.state = state;
        previewStatus.querySelector('span:last-child').textContent = message;
    }

    async function refreshPreview() {
        window.clearTimeout(previewTimer);
        if (previewController) {
            previewController.abort();
            previewController = null;
        }

        if (!content.value.trim()) {
            preview.innerHTML = '<div class=\"preview-empty\"><span class=\"preview-empty-mark\" aria-hidden=\"true\">✦</span><strong>预览会显示在这里</strong><p>在左侧输入 Markdown，稍等片刻即可看到效果。</p></div>';
            setPreviewStatus('idle', '等待输入');
            return;
        }

        previewController = new AbortController();
        const requestController = previewController;
        setPreviewStatus('updating', '正在更新');
        const body = new URLSearchParams();
        body.set('contentMarkdown', content.value);
        try {
            const response = await fetch('/posts/preview', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [csrfHeader.content]: csrf.content
                },
                body,
                signal: requestController.signal
            });
            if (!response.ok) throw new Error('Preview request failed');
            const html = await response.text();
            if (previewController !== requestController) return;
            preview.innerHTML = html || '<div class=\"preview-empty\"><strong>这段内容暂时没有可显示的内容</strong></div>';
            setPreviewStatus('ready', '已同步');
        } catch (error) {
            if (error.name === 'AbortError') return;
            if (previewController === requestController) {
                preview.innerHTML = '<div class=\"preview-empty\"><strong>预览暂时无法更新</strong><p>请检查网络后重试。</p></div>';
                setPreviewStatus('error', '更新失败');
            }
        } finally {
            if (previewController === requestController) previewController = null;
        }
    }

    previewButton.addEventListener('click', refreshPreview);
    content.addEventListener('input', function () {
        window.clearTimeout(previewTimer);
        if (previewController) {
            previewController.abort();
            previewController = null;
        }
        setPreviewStatus('waiting', '输入后自动同步');
        previewTimer = window.setTimeout(refreshPreview, 250);
    });
    refreshPreview();

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
        content.dispatchEvent(new Event('input', { bubbles: true }));
        message.textContent = '图片已插入';
    });

    form.addEventListener('submit', syncRoles);
})();
