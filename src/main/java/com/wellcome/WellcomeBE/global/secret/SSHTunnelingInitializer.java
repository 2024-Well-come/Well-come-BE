package com.wellcome.WellcomeBE.global.secret;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Properties;

import static java.lang.System.exit;



@Slf4j
@Component
@ConfigurationProperties(prefix = "ssh")
@Validated
@Setter
public class SSHTunnelingInitializer {

    @NotNull
    @Value("${ssh.remote_jump_host}")
    private String remoteJumpHost;
    @NotNull
    @Value("${ssh.user}")
    private String user;
    @NotNull
    @Value("${ssh.private_key_path}")
    private String privateKey;
    @NotNull
    @Value("${ssh.database_endpoint}")
    private String databaseEndpoint;
    @NotNull
    @Value("${ssh.database_port}")
    private int databasePort;

    @NotNull
    @Value("${ssh.local_port}")
    private int localPort;

    private Session session;

    @PreDestroy
    public void closeSSH() {
        if (session.isConnected())
            session.disconnect();
    }

    public Integer buildSshConnection() {
        System.out.println("start");
        Integer forwardedPort = null;

        try {
            log.info("{}@{} {}:{}:{} with privateKey",user, remoteJumpHost, localPort, databaseEndpoint, "3306");

            log.info("start ssh tunneling..");

            JSch jSch = new JSch();
            log.info("creating ssh session");
            jSch.addIdentity(privateKey);  // 개인키
            log.info("success add Identity");
            session = jSch.getSession(user, remoteJumpHost);  // 세션 설정
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            log.info("complete creating ssh session");

            log.info("start connecting ssh connection");
            session.connect();  // ssh 연결
            log.info("success connecting ssh connection ");

            // 로컬pc의 남는 포트 하나와 원격 접속한 pc의 db포트 연결
            log.info("start forwarding");
            forwardedPort = session.setPortForwardingL(localPort, databaseEndpoint, databasePort);
            log.info("successfully connected to database");

        } catch (JSchException e){
            log.error("fail to make ssh tunneling");
            this.closeSSH();
            e.printStackTrace();
            exit(1);
        }

        return forwardedPort;
    }
}


