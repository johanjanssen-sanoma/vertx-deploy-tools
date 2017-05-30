package nl.jpoint.maven.vertx.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.aether.artifact.Artifact;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"endpoint"})
public class DeployConfigRequest extends Request {

    private static final String ENDPOINT = "/deploy/config";

    public DeployConfigRequest(Artifact artifact) {
        super(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getClassifier(), artifact.getExtension());
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }
}
