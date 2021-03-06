// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.aws.amazonmq.blog.testcases;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aws.amazonmq.blog.util.MsgConsumer_CustomPrefetch;
import com.aws.amazonmq.blog.util.MsgProducer_FIFO;

/**
 * This class demonstrates FIFO Test case # 1 where:
 *  - a queue has three producers 
 *  - a queue has one consumers
 *  - a queue has three message groups
 *  - each consumer uses a pre-fetch size of 1
 * 
 * @author Ravi Itha
 *
 */
public class FIFO_Testcase_1 {

	public static void main(String[] args) throws InterruptedException {

		Logger logger = LoggerFactory.getLogger(FIFO_Testcase_1.class);
		String amazonMQSSLEndPoint = args[0]; // OpenWire Endpoint for AmazonMQ Broker
		String username = args[1]; // Username
		String password = args[2]; // Password
		String queueName = args[3]; // name of the AmazonMQ Queue Name
		int numMsgs = Integer.valueOf(args[4]); // number of messages to be inserted by each producer
		String useCaseId = args[5];
		String prefetchSize = args[6];
		try {
			// Grab the Scheduler instance from the Factory and start it off
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			// define producer jobs and tie them to MsgProducer which does the actual work
			JobDetail job1 = newJob(MsgProducer_FIFO.class)
					.withIdentity(useCaseId.concat("-").concat("producer-job1"), "group1")
					.usingJobData("producerName", "producer_1").usingJobData("queueName", queueName)
					.usingJobData("amazonMQSSLEndPoint", amazonMQSSLEndPoint).usingJobData("username", username)
					.usingJobData("password", password).usingJobData("useCaseId", useCaseId)
					.usingJobData("msgGroup", "Group-A").usingJobData("numMsgs", numMsgs)
					.usingJobData("msgIdSequence", 10000).usingJobData("msgPrefix", "A-").build();
			JobDetail job2 = newJob(MsgProducer_FIFO.class)
					.withIdentity(useCaseId.concat("-").concat("producer-job2"), "group1")
					.usingJobData("producerName", "producer_2").usingJobData("queueName", queueName)
					.usingJobData("amazonMQSSLEndPoint", amazonMQSSLEndPoint).usingJobData("username", username)
					.usingJobData("password", password).usingJobData("useCaseId", useCaseId)
					.usingJobData("msgGroup", "Group-B").usingJobData("numMsgs", numMsgs)
					.usingJobData("msgIdSequence", 15000).usingJobData("msgPrefix", "B-").build();
			JobDetail job3 = newJob(MsgProducer_FIFO.class)
					.withIdentity(useCaseId.concat("-").concat("producer-job3"), "group1")
					.usingJobData("producerName", "producer_3").usingJobData("queueName", queueName)
					.usingJobData("amazonMQSSLEndPoint", amazonMQSSLEndPoint).usingJobData("username", username)
					.usingJobData("password", password).usingJobData("useCaseId", useCaseId)
					.usingJobData("msgGroup", "Group-C").usingJobData("numMsgs", numMsgs)
					.usingJobData("msgIdSequence", 20000).usingJobData("msgPrefix", "C-").build();
			// define consumer jobs and tie them to MsgConsumer which does the actual work
			JobDetail job4 = newJob(MsgConsumer_CustomPrefetch.class).withIdentity(useCaseId.concat("-").concat("consumer-job1"), "group1")
			.usingJobData("consumerName", "consumer_1")
			.usingJobData("queueName", queueName)
			.usingJobData("amazonMQSSLEndPoint", amazonMQSSLEndPoint)
			.usingJobData("username", username)
			.usingJobData("password", password)
			.usingJobData("useCaseId", useCaseId)
			.usingJobData("numMsgs", numMsgs * 4)
			.usingJobData("prefetchSize", prefetchSize).build();
			
			
			// Trigger the jobs
			Trigger trigger1 = newTrigger().withIdentity("trigger1", "group1").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(5).withRepeatCount(0)).build();
			Trigger trigger2 = newTrigger().withIdentity("trigger2", "group1").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(5).withRepeatCount(0)).build();
			Trigger trigger3 = newTrigger().withIdentity("trigger3", "group1").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(10).withRepeatCount(0)).build();
			Trigger trigger4 = newTrigger().withIdentity("trigger4", "group1").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(10).withRepeatCount(0)).build();
			logger.info("scheduling producer jobs");
			scheduler.scheduleJob(job1, trigger1);
			scheduler.scheduleJob(job2, trigger2);
			scheduler.scheduleJob(job3, trigger3);
//			Thread.sleep(120000);
			logger.info("scheduling consumer job");
			scheduler.scheduleJob(job4, trigger4);
			Thread.sleep(60000);
			logger.info("unscheduling jobs and stopping the scheduler. Use case id: " + useCaseId);
			scheduler.unscheduleJob(trigger1.getKey());
			scheduler.unscheduleJob(trigger2.getKey());
			scheduler.unscheduleJob(trigger3.getKey());
			scheduler.unscheduleJob(trigger4.getKey());
			scheduler.shutdown();
		} catch (SchedulerException se) {
			se.printStackTrace();
		}
	}
}