package nl.jpoint.maven.vertx.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"endpoint"})
public class DeployArtifactRequest extends Request {

    private static final String ENDPOINT = "/deploy/artifact";

    private final String context;

    public DeployArtifactRequest(final String group_id, final String artifact_id, final String version, final String classifier, final String context) {
        super(group_id, artifact_id, version, classifier);
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }
}
