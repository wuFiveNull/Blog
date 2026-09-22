package com.wufivenull.blog.markdown;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownServiceTest {

    private final MarkdownService markdownService = new MarkdownService();

    @Test
    void removesDangerousHtmlAndKeepsMarkdownContent() {
        String html = markdownService.render("# 标题\n\n<script>alert('x')</script>\n\n**正文**");

        assertThat(html).contains("<h1>标题</h1>");
        assertThat(html).contains("<strong>正文</strong>");
        assertThat(html).doesNotContain("<script>");
    }
}
