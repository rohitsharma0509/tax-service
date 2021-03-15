package com.scb.rider.tax.config.sftp;

import com.scb.rider.tax.service.impl.AmazonS3Service;
import com.scb.rider.tax.util.CommonUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
@Setter
@Component
@ConfigurationProperties(ignoreUnknownFields = false, prefix = "sftp.client")
public class SftpProperties {

    private static final String SECRETS = "secrets";

    @Autowired
    private AmazonS3Service amazonS3Service;

    @Value("${secretsPath}")
    private String secretsPath;

    private String host;
    private Integer port;
    private String protocol;
    private String username;
    @Setter(AccessLevel.NONE)
    private String password;
    private String root;
    private String privateKeyFile;
    @Setter(AccessLevel.NONE)
    private byte[] privateKey;
    @Setter(AccessLevel.NONE)
    private String passphrase;
    private String sessionStrictHostKeyChecking;
    private Integer sessionConnectTimeout;
    private Integer channelConnectedTimeout;

    @SneakyThrows
    @PostConstruct
    public void setSftpSecrets() {
        URI pswrdFilePath = ResourceUtils.getURL(secretsPath + "/TAXINVOICE_SFTP_PASSWORD").toURI();
        URI passphraseFilePath = ResourceUtils.getURL(secretsPath + "/TAXINVOICE_SFTP_PASSPHRASE").toURI();
        String pswrd = CommonUtils.sanitize(Files.readAllBytes(Paths.get(pswrdFilePath)));
        String passphrase = CommonUtils.sanitize(Files.readAllBytes(Paths.get(passphraseFilePath)));
        this.password = pswrd;
        this.passphrase = passphrase;
        this.privateKey = amazonS3Service.downloadFile(SECRETS, "tax-invoice/sftp", privateKeyFile);
    }
}
