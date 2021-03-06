package org.bahmni.module.elisatomfeedclient.api.client;

import org.apache.log4j.Logger;
import org.bahmni.module.elisatomfeedclient.api.ElisAtomFeedProperties;
import org.bahmni.webclients.AnonymousAuthenticator;
import org.bahmni.webclients.ClientCookies;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.ict4h.atomfeed.client.service.*;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class OpenElisFeedClient {
    protected AtomFeedClient atomFeedClient;
    private ElisAtomFeedProperties properties;
    private PlatformTransactionManager transactionManager;
    private Logger logger = Logger.getLogger(OpenElisFeedClient.class);

    public OpenElisFeedClient(ElisAtomFeedProperties properties, PlatformTransactionManager transactionManager) {
        this.properties = properties;
        this.transactionManager = transactionManager;
    }


    /**
     *
     * @param feedUri
     * @return
     * @throws java.lang.RuntimeException if feed Uri is invalid
     */
    private URI getURIForFeed(String feedUri) {
        try {
            return new URI(feedUri);
        } catch (URISyntaxException e) {
            logger.error("openelisatomfeedclient:error instantiating client:" + e.getMessage(), e);
            throw new RuntimeException("error for uri:" + feedUri);
        }
    }

    public org.ict4h.atomfeed.client.service.FeedClient getAtomFeedClient() {
        URI uriForFeed = getURIForFeed(getFeedUri(properties));
        if(atomFeedClient == null) {
            ConnectionDetails connectionDetails = createConnectionDetails(properties);
                HttpClient httpClient = new HttpClient(connectionDetails, new AnonymousAuthenticator(connectionDetails));
                ClientCookies cookies = httpClient.getCookies(uriForFeed);
                EventWorker openMRSEventWorker = createWorker(httpClient, properties);
                AtomFeedSpringTransactionManager txMgr = new AtomFeedSpringTransactionManager(transactionManager);
            atomFeedClient = new AtomFeedClient(
                        new AllFeeds(properties, cookies),
                        new AllMarkersJdbcImpl(txMgr),
                        new AllFailedEventsJdbcImpl(txMgr),
                        properties,
                        txMgr,
                        uriForFeed,
                        openMRSEventWorker);
            return atomFeedClient;
        }
        else {
            return atomFeedClient;
        }
    }

    protected abstract String getFeedUri(ElisAtomFeedProperties properties);

    private ConnectionDetails createConnectionDetails(ElisAtomFeedProperties properties) {
        return new ConnectionDetails(properties.getOpenElisUri(),null,null,properties.getConnectTimeout(),properties.getReadTimeout());
    }

    protected abstract EventWorker createWorker(HttpClient authenticatedWebClient, ElisAtomFeedProperties properties);
}
