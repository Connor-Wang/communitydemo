package com.wcaaotr.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Connor
 * @create 2021-07-14-10:28
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
