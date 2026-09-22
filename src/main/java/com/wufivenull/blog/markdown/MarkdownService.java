package com.wufivenull.blog.markdown;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService() {
        List<Extension> extensions = List.of(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                AutolinkExtension.create()
        );
        this.parser = Parser.builder().extensions(extensions).build();
        this.renderer = HtmlRenderer.builder().extensions(extensions).build();
    }

    public String render(String markdown) {
        Node document = parser.parse(markdown == null ? "" : markdown);
        String html = renderer.render(document);
        Safelist safelist = Safelist.relaxed()
                .addTags("table", "thead", "tbody", "tr", "th", "td", "del")
                .addAttributes(":all", "class")
                .addProtocols("a", "href", "http", "https", "mailto");
        return Jsoup.clean(html, "", safelist,
                new Document.OutputSettings().prettyPrint(false));
    }
}
