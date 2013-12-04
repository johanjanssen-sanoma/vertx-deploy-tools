package nl.jpoint.maven.vertx.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"endpoint"})
public class DeployRequest {

    private static final ObjectWriter writer = new ObjectMapper().writer();
    private static final String ENDPOINT = "/deploy/deploy";

    private final List<Request> modules;
    private final List<Request> artifacts;
    @JsonProperty("aws")
    private final boolean aws;

    public DeployRequest(List<Request> modules, List<Request> artifacts, boolean aws) {
        this.modules = modules;
        this.artifacts = artifacts;
        this.aws = aws;
    }

    public List<Request> getModules() {
        return new ArrayList<>(modules);
    }

    public List<Request> getArtifacts() {
        return new ArrayList<>(artifacts);
    }

  /*  public boolean getAws() {
        return this.aws;
    }*/

    public String toJson() {
        try {
            return writer.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getEndpoint() {
        return ENDPOINT;
    }
}
