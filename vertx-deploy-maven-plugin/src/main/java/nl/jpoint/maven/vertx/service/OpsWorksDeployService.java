package nl.jpoint.maven.vertx.service;

import nl.jpoint.maven.vertx.mojo.DeployConfiguration;
import nl.jpoint.maven.vertx.executor.AwsRequestExecutor;
import nl.jpoint.maven.vertx.executor.RequestExecutor;
import nl.jpoint.maven.vertx.request.DeployRequest;
import nl.jpoint.maven.vertx.request.Request;
import nl.jpoint.maven.vertx.utils.AwsOpsWorksDeployUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;

import java.util.List;

public class OpsWorksDeployService extends DeployService {
    private final DeployConfiguration activeConfiguration;
    private final String region;
    private final Integer port;
    private final Integer requestTimeout;

    public OpsWorksDeployService(final DeployConfiguration activeConfiguration, final String region, final Integer port, final Integer requestTimeout, final Server server, Log log) {
        super(server, log);
        this.activeConfiguration = activeConfiguration;
        this.region = region;
        this.port = port;
        this.requestTimeout = requestTimeout;
    }

    public void deployWithOpsWorks(List<Request> deployModuleRequests, List<Request> deployArtifactRequests, List<Request> deployConfigRequests) throws MojoFailureException, MojoExecutionException {
        if (activeConfiguration.getOpsWorksLayerId() == null) {
            throw new MojoFailureException("ActiveConfiguration " + activeConfiguration.getTarget() + " has no opsWorksLayerId set");
        }

        AwsOpsWorksDeployUtils awsOpsWorksDeployUtils = new AwsOpsWorksDeployUtils(getServer(), region);
        awsOpsWorksDeployUtils.getHostsOpsWorks(getLog(), activeConfiguration);

        DeployRequest deployRequest = new DeployRequest.Builder()
                .withModules(deployModuleRequests)
                .withArtifacts(deployArtifactRequests)
                .withConfigs(deployConfigRequests)
                .withElb(activeConfiguration.withElb())
                .withRestart(activeConfiguration.doRestart())
                .build();

        for (String host : activeConfiguration.getHosts()) {

            final RequestExecutor executor = new AwsRequestExecutor(getLog(), requestTimeout, port);
            executor.executeRequest(deployRequest, host, false);
        }
    }
}
