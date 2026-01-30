package com.company.claims.routing;

import com.company.claims.model.*;

import java.util.List;

public class ClaimRouter {

    public RouteResult route(ExtractedFields d, List<String> missing) {

        if (!missing.isEmpty())
            return new RouteResult("Manual Review", "Mandatory fields missing");

        String desc = d.incidentInformation.description == null
                ? "" : d.incidentInformation.description.toLowerCase();

        if (desc.contains("fraud") || desc.contains("staged") || desc.contains("inconsistent"))
            return new RouteResult("Investigation Flag", "Suspicious keywords detected");

        if ("injury".equalsIgnoreCase(d.otherMandatoryFields.claimType))
            return new RouteResult("Specialist Queue", "Injury claim");

        int est = Integer.parseInt(d.otherMandatoryFields.initialEstimate);
        if (est < 25000) {
            return new RouteResult("Fast-track", "Low estimated damage"
            );
        }

        return new RouteResult("Manual Review", "Default fallback");
    }
}
