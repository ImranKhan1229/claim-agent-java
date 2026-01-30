package com.company.claims.model;

import java.util.List;

public class ClaimOutput {
    public ExtractedFields extractedFields;
    public List<String> missingFields;
    public String recommendedRoute;
    public String reasoning;

    public ClaimOutput(ExtractedFields e, List<String> m, RouteResult r) {
        this.extractedFields = e;
        this.missingFields = m;
        this.recommendedRoute = r.route;
        this.reasoning = r.reason;
    }
}
