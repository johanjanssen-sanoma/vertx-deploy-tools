package nl.soutenet.vertx.mod.integration.cluster;


import junit.framework.Assert;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.fail;

public class ClusterManagerModuleTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterManagerModuleTest.class);
    private static final String POST_URI = "http://localhost:6789/deploy/module";
    private static final String POST_URI_SITE = "http://localhost:6789/deploy/artifact";

    @Test
    public void testInvalidDeployModuleCommand() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(POST_URI);
        try (CloseableHttpResponse response = httpclient.execute(post)) {
            LOG.info("testDeployModuleCommand : Post response status {}", response.getStatusLine().getStatusCode());
            Assert.assertEquals(400, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            LOG.error("testDeployModuleCommand : {}", e);
            fail();
        }
    }

    @Test
    public void testDeployModuleCommand() {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(POST_URI);

        JsonObject postData = createDeployCommand();

        ByteArrayInputStream bos = new ByteArrayInputStream(postData.encode().getBytes());
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(bos);
        entity.setContentLength(postData.encode().getBytes().length);
        post.setEntity(entity);

        try (CloseableHttpResponse response = httpclient.execute(post)) {
            LOG.info("testDeployModuleCommand : Post response status {}", response.getStatusLine().getStatusCode());
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            LOG.error("testDeployModuleCommand : {}", e);
            fail();
        }
    }

    @Test
    public void testDeploySiteModuleCommand() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(POST_URI_SITE);

        JsonObject postData = createDeploySiteCommand2();

        ByteArrayInputStream bos = new ByteArrayInputStream(postData.encode().getBytes());
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(bos);
        entity.setContentLength(postData.encode().getBytes().length);
        post.setEntity(entity);

        try (CloseableHttpResponse response = httpclient.execute(post)) {
            LOG.info("testDeployModuleCommand : Post response status {}", response.getStatusLine().getStatusCode());
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            LOG.error("testDeployModuleCommand : {}", e);
            fail();
        }

    }


    private JsonObject createDeployCommand() {
        return new JsonObject()
                .putString("group_id", "nl.jpoint.vertx-deploy-tools")
                .putString("artifact_id", "vertx-deploy-mod")
                .putString("version", "1.0.0-SNAPSHOT")
                .putNumber("instances", 1);
    }

    private JsonObject createDeploySiteCommand() {

        return new JsonObject()
                .putString("group_id", "nl.malmberg.vooruit.frontend")
                .putString("artifact_id", "vooruit")
                .putString("version", "1.0.0-SNAPSHOT")
                .putString("classifier", "site")
                .putString("context", "/var/www/vooruit");
    }

    private JsonObject createDeploySiteCommand2() {

        return new JsonObject()
                .putString("group_id", "nl.malmberg.vooruit")
                .putString("artifact_id", "static")
                .putString("version", "1.2.0-SNAPSHOT")
                .putString("classifier", "site")
                .putString("context", "/var/www/vooruit-assets");
    }


}
