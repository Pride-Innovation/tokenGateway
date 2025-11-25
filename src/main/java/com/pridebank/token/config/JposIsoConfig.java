package com.pridebank.token.config;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JposIsoConfig {

    @Bean
    public ISO87APackager isoPackager() {
        return new ISO87APackager();
    }

    @Bean
    public ISOMsg isoMsg(ISO87APackager packager) {
        ISOMsg m = new ISOMsg();
        m.setPackager(packager);
        return m;
    }
}