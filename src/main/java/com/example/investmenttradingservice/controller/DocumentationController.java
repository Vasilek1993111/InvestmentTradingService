package com.example.investmenttradingservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Контроллер для предоставления документации API.
 * 
 * <p>
 * Предоставляет доступ к документации API через веб-интерфейс.
 * </p>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Controller
@RequestMapping("/api")
public class DocumentationController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentationController.class);

    /**
     * Возвращает HTML-страницу с документацией API.
     * 
     * @return HTML-страница с документацией
     */
    @GetMapping(value = "/docs", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> getApiDocumentation() {
        try {
            logger.info("Запрос HTML документации API");
            Resource resource = new ClassPathResource("docs/API_DOCUMENTATION.md");

            if (!resource.exists()) {
                logger.error("Файл документации не найден: docs/API_DOCUMENTATION.md");
                return getErrorResponse("Файл документации не найден");
            }

            byte[] bytes = resource.getInputStream().readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);

            // Преобразуем Markdown в простой HTML
            String htmlContent = convertMarkdownToHtml(content);
            logger.info("Документация успешно загружена и преобразована в HTML");

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
                    .body(htmlContent);

        } catch (IOException e) {
            logger.error("Ошибка при загрузке документации: {}", e.getMessage(), e);
            return getErrorResponse("Ошибка при загрузке документации: " + e.getMessage());
        }
    }

    /**
     * Возвращает Markdown-версию документации.
     * 
     * @return Markdown-документация
     */
    @GetMapping(value = "/docs.md", produces = "text/markdown;charset=UTF-8")
    public ResponseEntity<String> getApiDocumentationMarkdown() {
        try {
            logger.info("Запрос Markdown документации API");
            Resource resource = new ClassPathResource("docs/API_DOCUMENTATION.md");

            if (!resource.exists()) {
                logger.error("Файл документации не найден: docs/API_DOCUMENTATION.md");
                return ResponseEntity.notFound().build();
            }

            byte[] bytes = resource.getInputStream().readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            logger.info("Markdown документация успешно загружена");

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("text/markdown;charset=UTF-8"))
                    .body(content);

        } catch (IOException e) {
            logger.error("Ошибка при загрузке Markdown документации: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Простое преобразование Markdown в HTML для отображения документации.
     * 
     * @param markdown исходный Markdown текст
     * @return HTML версия
     */
    private String convertMarkdownToHtml(String markdown) {
        StringBuilder html = new StringBuilder();

        // HTML шаблон
        html.append("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                <meta charset="UTF-8">
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Investment Trading Service API Documentation</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 1200px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f8f9fa;
                        }
                        .container {
                            background: white;
                            padding: 40px;
                            border-radius: 8px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        h1 {
                            color: #2c3e50;
                            border-bottom: 3px solid #3498db;
                            padding-bottom: 10px;
                        }
                        h2 {
                            color: #34495e;
                            margin-top: 30px;
                            border-bottom: 1px solid #ecf0f1;
                            padding-bottom: 5px;
                        }
                        h3 {
                            color: #7f8c8d;
                            margin-top: 25px;
                        }
                        code {
                            background-color: #f1f2f6;
                            padding: 2px 6px;
                            border-radius: 3px;
                            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                            font-size: 0.9em;
                        }
                        pre {
                            background-color: #2c3e50;
                            color: #ecf0f1;
                            padding: 20px;
                            border-radius: 6px;
                            overflow-x: auto;
                            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                            font-size: 0.9em;
                            line-height: 1.4;
                        }
                        pre code {
                            background: none;
                            padding: 0;
                            color: inherit;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin: 20px 0;
                        }
                        th, td {
                            border: 1px solid #ddd;
                            padding: 12px;
                            text-align: left;
                        }
                        th {
                            background-color: #3498db;
                            color: white;
                            font-weight: 600;
                        }
                        tr:nth-child(even) {
                            background-color: #f8f9fa;
                        }
                        .status-badge {
                            display: inline-block;
                            padding: 4px 8px;
                            border-radius: 4px;
                            font-size: 0.8em;
                            font-weight: 600;
                            margin-right: 5px;
                        }
                        .status-200 { background: #d4edda; color: #155724; }
                        .status-400 { background: #f8d7da; color: #721c24; }
                        .status-422 { background: #fff3cd; color: #856404; }
                        .status-500 { background: #f5c6cb; color: #721c24; }
                        .endpoint {
                            background: #e8f4fd;
                            padding: 15px;
                            border-left: 4px solid #3498db;
                            margin: 15px 0;
                            border-radius: 0 4px 4px 0;
                        }
                        .endpoint-method {
                            font-weight: bold;
                            color: white;
                            padding: 4px 8px;
                            border-radius: 3px;
                            margin-right: 10px;
                        }
                        .method-post { background: #27ae60; }
                        .method-get { background: #3498db; }
                        .method-delete { background: #e74c3c; }
                        .alert {
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                            border-left: 4px solid;
                        }
                        .alert-info {
                            background: #d1ecf1;
                            border-color: #17a2b8;
                            color: #0c5460;
                        }
                        .alert-warning {
                            background: #fff3cd;
                            border-color: #ffc107;
                            color: #856404;
                        }
                        .alert-danger {
                            background: #f8d7da;
                            border-color: #dc3545;
                            color: #721c24;
                        }
                        .toc {
                            background: #f8f9fa;
                            padding: 20px;
                            border-radius: 6px;
                            margin: 20px 0;
                        }
                        .toc ul {
                            margin: 0;
                            padding-left: 20px;
                        }
                        .toc a {
                            color: #3498db;
                            text-decoration: none;
                        }
                        .toc a:hover {
                            text-decoration: underline;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                """);

        // Простое преобразование Markdown в HTML
        String[] lines = markdown.split("\n");
        boolean inCodeBlock = false;

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("```")) {
                if (!inCodeBlock) {
                    html.append("<pre><code>");
                    inCodeBlock = true;
                } else {
                    html.append("</code></pre>");
                    inCodeBlock = false;
                }
                continue;
            }

            if (inCodeBlock) {
                html.append(escapeHtml(line)).append("\n");
                continue;
            }

            // Заголовки
            if (line.startsWith("# ")) {
                html.append("<h1>").append(escapeHtml(line.substring(2))).append("</h1>");
            } else if (line.startsWith("## ")) {
                html.append("<h2>").append(escapeHtml(line.substring(3))).append("</h2>");
            } else if (line.startsWith("### ")) {
                html.append("<h3>").append(escapeHtml(line.substring(4))).append("</h3>");
            } else if (line.startsWith("#### ")) {
                html.append("<h4>").append(escapeHtml(line.substring(5))).append("</h4>");
            } else if (line.startsWith("**") && line.endsWith("**") && line.length() > 4) {
                // Bold текст
                String boldText = line.substring(2, line.length() - 2);
                html.append("<strong>").append(escapeHtml(boldText)).append("</strong>");
            } else if (line.startsWith("- ")) {
                html.append("<li>").append(escapeHtml(line.substring(2))).append("</li>");
            } else if (line.startsWith("|")) {
                // Таблица - пропускаем, так как сложно парсить
                html.append("<p><em>Таблица в Markdown формате</em></p>");
            } else if (line.isEmpty()) {
                html.append("<br>");
            } else {
                // Обычный текст с обработкой кода
                String processedLine = line.replaceAll("`([^`]+)`", "<code>$1</code>");
                html.append("<p>").append(processedLine).append("</p>");
            }
        }

        html.append("""
                    </div>
                </body>
                </html>
                """);

        return html.toString();
    }

    /**
     * Создает HTML страницу с ошибкой.
     * 
     * @param message сообщение об ошибке
     * @return ResponseEntity с HTML страницей ошибки
     */
    private ResponseEntity<String> getErrorResponse(String message) {
        String errorHtml = String.format("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                    <title>API Documentation - Error</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                            margin: 40px;
                            background-color: #f8f9fa;
                        }
                        .container {
                            background: white;
                            padding: 40px;
                            border-radius: 8px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                            max-width: 800px;
                            margin: 0 auto;
                        }
                        .error {
                            color: #d32f2f;
                            background: #ffebee;
                            padding: 20px;
                            border-radius: 4px;
                            border-left: 4px solid #d32f2f;
                        }
                        h1 { color: #2c3e50; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>API Documentation</h1>
                        <div class="error">
                            <h2>Ошибка загрузки документации</h2>
                            <p>%s</p>
                            <p><strong>Возможные решения:</strong></p>
                            <ul>
                                <li>Проверьте, что приложение запущено</li>
                                <li>Обратитесь к администратору системы</li>
                                <li>Попробуйте обновить страницу</li>
                            </ul>
                        </div>
                    </div>
                </body>
                </html>
                """, escapeHtml(message));

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
                .body(errorHtml);
    }

    /**
     * Экранирует HTML символы.
     * 
     * @param text исходный текст
     * @return экранированный текст
     */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
