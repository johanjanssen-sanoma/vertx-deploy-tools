package nl.jpoint.vertx.mod.deploy.command;

import io.netty.handler.codec.http.HttpResponseStatus;
import nl.jpoint.vertx.mod.deploy.request.ModuleRequest;
import nl.jpoint.vertx.mod.deploy.util.MetadataXPathUtil;
import nl.jpoint.vertx.mod.deploy.util.PlatformUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ResolveSnapshotVersion implements Command<ModuleRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(ResolveSnapshotVersion.class);
    private final JsonObject config;
    private final String logId;

    public ResolveSnapshotVersion(JsonObject config, String logId) {

        this.config = config;
        this.logId = logId;
    }

    @Override
    public JsonObject execute(ModuleRequest request) {
        List<String> repoList = PlatformUtils.initializeRepoList(logId, config.getString("vertx.home"));

        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        boolean secure = true;

        if (config.containsField("http.authSecure")) {
            secure = config.getBoolean("http.authSecure");
            LOG.warn("[{} - {}]: Unsecure request to artifact repository.", logId, request.getId());
        }

        credsProvider.setCredentials(
                new AuthScope(config.getString("http.authUri"), secure ? 443 : 80),
                new UsernamePasswordCredentials(config.getString("http.authUser"), config.getString("http.authPass")));

        boolean resolved = false;
        String realSnapshotVersion = request.isSnapshot() ? request.getVersion() : null;

        Iterator<String> it = repoList.iterator();
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
            while (it.hasNext() && !resolved) {
                String uri = it.next();
                if (request.isSnapshot()) {
                    LOG.info("[{} - {}]: Artifact is -SNAPSHOT, trying to parse metadata for last version {}.", logId, request.getId(), request.getModuleId());
                    realSnapshotVersion = this.retrieveAndParseMetadata(request, httpclient, uri);
                    if (realSnapshotVersion != null) {
                        LOG.info("[{} - {}]: Parsed metadata. Snapshot version is {} ", logId, request.getId(), realSnapshotVersion);
                        resolved = true;
                    }

                }
            }
        } catch (IOException e) {
            LOG.error("[{} - {}]: IO Exception while downloading artifact {}. Reason '{}'.", logId, request.getId(), request.getArtifactId(), e.getMessage());
        }
        return new JsonObject().putBoolean("success", resolved).putString("version", realSnapshotVersion);

    }

    private String retrieveAndParseMetadata(ModuleRequest request, CloseableHttpClient httpclient, String repoUri) {
        HttpGet getMetadata = new HttpGet(repoUri + "/" + request.getMetadataLocation());
        try (CloseableHttpResponse response = httpclient.execute(getMetadata)) {
            if (response.getStatusLine().getStatusCode() != HttpResponseStatus.OK.code()) {
                LOG.error("[{} - {}]: No metadata found for module {} with error code {} with request {}", logId, request.getId(), request.getModuleId(), response.getStatusLine().getStatusCode(), getMetadata.getURI());
                return null;
            }
            byte[] metadata = EntityUtils.toByteArray(response.getEntity());
            response.close();
            String result = MetadataXPathUtil.getRealSnapshotVersionFromMetadata(metadata, request);
            return result;

        } catch (IOException e) {
            LOG.error("[{} - {}]: Error while downloading metadata for module {} : {}", logId, request.getId(), request.getModuleId(), e.getMessage());
            return null;
        }
    }
}