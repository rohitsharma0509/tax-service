package com.scb.rider.tax.client.impl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.scb.rider.tax.config.sftp.SftpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SftpClient {

    private static final String SESSION_CONFIG_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    private static final String PRIVATE_KEY_NAME = "PCC-PRIVATE-KEY";

    @Autowired
    private SftpProperties sftpProperties;

    public ChannelSftp connect() throws JSchException {
        JSch jsch = new JSch();
        Channel channel = null;

        log.info("Try to connect sftp[" + sftpProperties.getUsername() + "@" + sftpProperties.getHost() + "]");
        if (!StringUtils.isEmpty(sftpProperties.getPrivateKey())) {
            if (!StringUtils.isEmpty(sftpProperties.getPassphrase())) {
                log.info("Connection uses both private key and passphrase");
                jsch.addIdentity(PRIVATE_KEY_NAME, sftpProperties.getPrivateKey(), null, sftpProperties.getPassphrase().getBytes(StandardCharsets.UTF_8));
            } else {
                log.info("Connection uses private key only");
                jsch.addIdentity(PRIVATE_KEY_NAME, sftpProperties.getPrivateKey(), null, null);
            }
        }
        Session session = jsch.getSession(sftpProperties.getUsername(), sftpProperties.getHost(), sftpProperties.getPort());
        session.setConfig(SESSION_CONFIG_STRICT_HOST_KEY_CHECKING, sftpProperties.getSessionStrictHostKeyChecking());
        session.setPassword(sftpProperties.getPassword());
        session.connect(sftpProperties.getSessionConnectTimeout());
        log.info("Session connected to " + sftpProperties.getHost() + ".");

        // Create sftp communication channel
        channel = session.openChannel(sftpProperties.getProtocol());
        channel.connect(sftpProperties.getChannelConnectedTimeout());
        log.info("Channel created to " + sftpProperties.getHost() + ".");
        return (ChannelSftp) channel;
    }

    public void disconnect(ChannelSftp channelSftp) {
        try {
            if (channelSftp == null)
                return;

            if (channelSftp.isConnected())
                channelSftp.disconnect();

            if (channelSftp.getSession() != null)
                channelSftp.getSession().disconnect();

        } catch (Exception ex) {
            log.error("SFTP disconnect error", ex);
        }
    }
}
