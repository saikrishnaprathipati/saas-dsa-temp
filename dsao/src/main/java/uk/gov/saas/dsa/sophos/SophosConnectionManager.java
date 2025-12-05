package uk.gov.saas.dsa.sophos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.saas.dsa.model.OverallApplicationStatus;
import uk.gov.saas.dsa.web.controller.uploader.QuoteUploadController;

import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class SophosConnectionManager {
    private static Logger logger = LogManager.getLogger(SophosConnectionManager.class);
    private static String sophosServer;
    private static int sophosServerPort;

    @Value("${dsa.sophos.server.primary}")
    private String sophosServerPrimary;

    @Value("${dsa.sophos.server.failover}")
    private String sophosServerFailover;

    @Value("${dsa.sophos.server.port}")
    private int serverPort;

    @PostConstruct
    public void init() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            sophosServerPort = serverPort;
            logger.info("SophosConnectionManager, hostname is {} ", hostname);

            switch (hostname) {
                case "sedsh356a":
                case "sedsh358a":
                case "sedsh315a":
                case "sedsh317a":
                    sophosServer = sophosServerPrimary;
                    logger.info("sophosServerPrimary {}", sophosServer);
                    break;
                case "sedsh357a":
                case "sedsh359a":
                case "sedsh316a":
                case "sedsh318a":
                    sophosServer = sophosServerFailover;
                    logger.info("sophosServerFailover {}", sophosServer);
                    break;
                default:
                    sophosServer = sophosServerPrimary;
                    logger.info("sophosServerPrimary {}", sophosServer);
                    break;
            }
            logger.info("SophosConnectionManager, sophosServer  {} and sophosServerPort {}", sophosServer, sophosServerPort);
        } catch (UnknownHostException e) {
            logger.error("UnknownHostException, sophosServer is {} ", sophosServer);
            throw new RuntimeException(e);
        }
    }

    public static SophosConnection getConnection() throws Exception {
        logger.info("SophosConnectionManager, sophosServer is {} and sophosServerPort {} ", sophosServer, sophosServerPort);
        return new SophosConnection(sophosServer, sophosServerPort);
    }
}
