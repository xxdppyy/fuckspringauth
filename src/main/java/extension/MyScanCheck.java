package extension;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.scanner.AuditResult;
import burp.api.montoya.scanner.ConsolidationAction;
import burp.api.montoya.scanner.ScanCheck;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import burp.api.montoya.scanner.audit.issues.AuditIssue;

public class MyScanCheck implements ScanCheck {
    MontoyaApi api;
    MyContextMenus myContextMenus = new MyContextMenus(api);
    public MyScanCheck(MontoyaApi api) {
        api.logging().logToOutput("bypass success");
        api.logging().logToOutput("myscancheck is"+myContextMenus.getActiveAuditIssues().toString());
        // Save references to usefull objects
        this.api = api;
    }
    public AuditResult activeAudit(HttpRequestResponse baseRequestResponse, AuditInsertionPoint auditInsertionPoint) {
        api.logging().logToOutput("bypass success");
        return AuditResult.auditResult(myContextMenus.getActiveAuditIssues());

    }
    public AuditResult passiveAudit(HttpRequestResponse baseRequestResponse) {
        api.logging().logToOutput("bypass success");
        return AuditResult.auditResult(myContextMenus.getActiveAuditIssues());
    }
    @Override
    public ConsolidationAction consolidateIssues(AuditIssue newIssue, AuditIssue existingIssue) {
        if(newIssue.name().equals(existingIssue.name()) && newIssue.baseUrl().equals(existingIssue.baseUrl())) {
            return ConsolidationAction.KEEP_EXISTING;
        } else {
            // Otherwise keep both the issues
            return ConsolidationAction.KEEP_BOTH;
        }
    }



}
