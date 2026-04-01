package extension;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BypassLogPanel extends JPanel {
    private final MontoyaApi api;
    private final DefaultTableModel tableModel;
    private final JTable logTable;
    private final HttpRequestEditor requestEditor;
    private final HttpResponseEditor responseEditor;
    private final HttpResponseEditor baselineEditor;
    private final List<LogEntry> logEntries;
    private final JTextField statusCodeFilter;
    private final JComboBox<String> resultFilter;
    private HttpResponse baselineResponse;
    private ResponseComparator comparator;

    public BypassLogPanel(MontoyaApi api) {
        this.api = api;
        this.logEntries = new ArrayList<>();
        this.statusCodeFilter = new JTextField("200", 10);
        this.resultFilter = new JComboBox<>(new String[]{"All", "✓ Success", "? Suspicious", "✗ Failed"});
        this.baselineResponse = null;
        this.comparator = null;

        setLayout(new BorderLayout());

        // 创建请求/响应编辑器
        requestEditor = api.userInterface().createHttpRequestEditor();
        responseEditor = api.userInterface().createHttpResponseEditor();
        baselineEditor = api.userInterface().createHttpResponseEditor();

        // 创建表格
        String[] columns = {"#", "Method", "URL", "Status", "Length", "Result", "Diff%", "Bypass Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logTable = new JTable(tableModel);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 选择行时显示详情
        logTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = logTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // 从表格第一列获取原始索引
                    int originalIndex = (Integer) tableModel.getValueAt(selectedRow, 0) - 1;
                    if (originalIndex >= 0 && originalIndex < logEntries.size()) {
                        LogEntry entry = logEntries.get(originalIndex);
                        requestEditor.setRequest(entry.request);
                        responseEditor.setResponse(entry.response);
                    }
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(logTable);

        // 创建三栏分割面板：请求 | 测试响应 | 基准响应
        JPanel requestPanel = new JPanel(new BorderLayout());
        requestPanel.add(new JLabel("Request"), BorderLayout.NORTH);
        requestPanel.add(requestEditor.uiComponent(), BorderLayout.CENTER);

        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.add(new JLabel("Test Response"), BorderLayout.NORTH);
        responsePanel.add(responseEditor.uiComponent(), BorderLayout.CENTER);

        JPanel baselinePanel = new JPanel(new BorderLayout());
        baselinePanel.add(new JLabel("Baseline Response"), BorderLayout.NORTH);
        baselinePanel.add(baselineEditor.uiComponent(), BorderLayout.CENTER);

        JSplitPane responseSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, responsePanel, baselinePanel);
        responseSplit.setResizeWeight(0.5);

        JSplitPane editorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, requestPanel, responseSplit);
        editorSplitPane.setResizeWeight(0.33);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, editorSplitPane);
        mainSplitPane.setResizeWeight(0.3);

        add(mainSplitPane, BorderLayout.CENTER);

        // 添加控制面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton clearButton = new JButton("Clear Logs");
        clearButton.addActionListener(e -> clearLogs());
        buttonPanel.add(clearButton);

        buttonPanel.add(new JLabel("  Status Code:"));
        buttonPanel.add(statusCodeFilter);

        buttonPanel.add(new JLabel("  Result:"));
        buttonPanel.add(resultFilter);

        JButton applyFilterButton = new JButton("Apply Filter");
        applyFilterButton.addActionListener(e -> applyFilter());
        buttonPanel.add(applyFilterButton);

        add(buttonPanel, BorderLayout.NORTH);
    }

    public void setBaselineResponse(HttpResponse baseline) {
        this.baselineResponse = baseline;
        this.comparator = new ResponseComparator(baseline, 50.0);
        baselineEditor.setResponse(baseline);
    }

    public synchronized void addLog(HttpRequest request, HttpResponse response, String bypassType) {
        ComparisonResult result = null;
        if (comparator != null) {
            result = comparator.compare(response);
        }

        LogEntry entry = new LogEntry(request, response, bypassType, result);
        logEntries.add(entry);

        final ComparisonResult finalResult = result;
        final int entryIndex = logEntries.size();

        SwingUtilities.invokeLater(() -> {
            // 检查是否匹配过滤条件
            if (matchesFilter(response.statusCode()) && matchesResultFilter(finalResult)) {
                Object[] row = {
                    entryIndex,
                    request.method(),
                    request.url(),
                    response.statusCode(),
                    response.body().length(),
                    finalResult != null ? finalResult.getDisplaySymbol() : "-",
                    finalResult != null ? String.format("%.1f%%", finalResult.getLengthDiffPercent()) : "-",
                    bypassType
                };
                tableModel.addRow(row);
            }
        });
    }

    private boolean matchesFilter(int statusCode) {
        String filterText = statusCodeFilter.getText().trim();
        if (filterText.isEmpty()) {
            return true;
        }
        String[] codes = filterText.split(",");
        for (String code : codes) {
            if (code.trim().equals(String.valueOf(statusCode))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesResultFilter(ComparisonResult result) {
        String selected = (String) resultFilter.getSelectedItem();
        if (selected == null || selected.equals("All")) {
            return true;
        }
        if (result == null) {
            return false;
        }
        if (selected.equals("✓ Success")) {
            return result.getStatus() == ComparisonResult.Status.SUCCESS;
        }
        if (selected.equals("? Suspicious")) {
            return result.getStatus() == ComparisonResult.Status.SUSPICIOUS;
        }
        if (selected.equals("✗ Failed")) {
            return result.getStatus() == ComparisonResult.Status.FAILED;
        }
        return true;
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        for (int i = 0; i < logEntries.size(); i++) {
            LogEntry entry = logEntries.get(i);
            if (matchesFilter(entry.response.statusCode()) && matchesResultFilter(entry.result)) {
                Object[] row = {
                    i + 1,  // 原始索引（从1开始）
                    entry.request.method(),
                    entry.request.url(),
                    entry.response.statusCode(),
                    entry.response.body().length(),
                    entry.result != null ? entry.result.getDisplaySymbol() : "-",
                    entry.result != null ? String.format("%.1f%%", entry.result.getLengthDiffPercent()) : "-",
                    entry.bypassType
                };
                tableModel.addRow(row);
            }
        }
    }

    private void clearLogs() {
        logEntries.clear();
        tableModel.setRowCount(0);
        requestEditor.setRequest(null);
        responseEditor.setResponse(null);
    }

    private static class LogEntry {
        HttpRequest request;
        HttpResponse response;
        String bypassType;
        ComparisonResult result;

        LogEntry(HttpRequest request, HttpResponse response, String bypassType, ComparisonResult result) {
            this.request = request;
            this.response = response;
            this.bypassType = bypassType;
            this.result = result;
        }
    }
}
