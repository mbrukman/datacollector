package com.streamsets.pipeline.stage.origin.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.streamsets.pipeline.api.BatchMaker;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.Stage.ConfigIssue;
import com.streamsets.pipeline.config.CsvHeader;
import com.streamsets.pipeline.config.CsvMode;
import com.streamsets.pipeline.config.DataFormat;
import com.streamsets.pipeline.config.JsonMode;
import com.streamsets.pipeline.config.LogMode;
import com.streamsets.pipeline.config.OnParseError;
import com.streamsets.pipeline.lib.Errors;
import com.streamsets.pipeline.lib.KafkaBroker;
import com.streamsets.pipeline.lib.KafkaUtil;
import com.streamsets.pipeline.lib.parser.log.RegExConfig;

public class StandaloneKafkaSource extends BaseKafkaSource {

  private static final Logger LOG = LoggerFactory.getLogger(StandaloneKafkaSource.class);
  private final String consumerGroup;
  private final String zookeeperConnect;
  private KafkaConsumer kafkaConsumer;
  private final int maxBatchSize;
  private final Map<String, String> kafkaConsumerConfigs;

  public StandaloneKafkaSource(String zookeeperConnect, String consumerGroup, String topic, DataFormat dataFormat,
    String charset, boolean produceSingleRecordPerMessage, int maxBatchSize, int maxWaitTime,
    Map<String, String> kafkaConsumerConfigs, int textMaxLineLen, JsonMode jsonContent, int jsonMaxObjectLen,
    CsvMode csvFileFormat, CsvHeader csvHeader, int csvMaxObjectLen, String xmlRecordElement, int xmlMaxObjectLen,
    LogMode logMode, int logMaxObjectLen, boolean retainOriginalLine, String customLogFormat, String regex,
    List<RegExConfig> fieldPathsToGroupName, String grokPatternDefinition, String grokPattern,
    boolean enableLog4jCustomLogFormat, String log4jCustomLogFormat, OnParseError onParseError, int maxStackTraceLines) {
    super(null, zookeeperConnect, consumerGroup, topic, dataFormat, charset, produceSingleRecordPerMessage, maxBatchSize, maxWaitTime, kafkaConsumerConfigs, textMaxLineLen, jsonContent, jsonMaxObjectLen, csvFileFormat, csvHeader, csvMaxObjectLen, xmlRecordElement, xmlMaxObjectLen, logMode, logMaxObjectLen, retainOriginalLine, customLogFormat, regex, fieldPathsToGroupName, grokPatternDefinition, grokPattern, enableLog4jCustomLogFormat, log4jCustomLogFormat, onParseError, maxStackTraceLines);
    this.zookeeperConnect = zookeeperConnect;
    this.consumerGroup = consumerGroup;
    this.maxBatchSize = maxBatchSize;
    this.kafkaConsumerConfigs = kafkaConsumerConfigs;
  }

  @Override
  protected List<ConfigIssue> validateConfigs() throws StageException {
  List<ConfigIssue> issues =  new ArrayList<ConfigIssue>();

  List<KafkaBroker> kafkaBrokers = KafkaUtil.validateBrokerList(issues, zookeeperConnect, Groups.KAFKA.name(),
    "zookeeperConnect", getContext());

   //validate connecting to kafka
   if(kafkaBrokers != null && !kafkaBrokers.isEmpty() && topic !=null && !topic.isEmpty()) {
     kafkaConsumer = new KafkaConsumer(zookeeperConnect, topic, consumerGroup, maxBatchSize, maxWaitTime,
       kafkaConsumerConfigs, getContext());
     kafkaConsumer.validate(issues, getContext());
   }

   //consumerGroup
   if(consumerGroup == null || consumerGroup.isEmpty()) {
     issues.add(getContext().createConfigIssue(Groups.KAFKA.name(), "consumerGroup",
       Errors.KAFKA_33));
   }
   return validateCommonConfigs(issues);
  }

  @Override
  public void init() throws StageException {
    if(getContext().isPreview()) {
      //set fixed batch duration time of 1 second for preview.
      maxWaitTime = 1000;
    }
    kafkaConsumer.init();
    LOG.info("Successfully initialized Kafka Consumer");
  }


  private String getMessageID(MessageAndOffset message) {
    return topic + "::" + message.getPartition() + "::" + message.getOffset();
  }

  @Override
  public String produce(String lastSourceOffset, int maxBatchSize, BatchMaker batchMaker) throws StageException {
    int recordCounter = 0;
    int batchSize = this.maxBatchSize > maxBatchSize ? maxBatchSize : this.maxBatchSize;
    long startTime = System.currentTimeMillis();
    while(recordCounter < batchSize && (startTime + maxWaitTime) > System.currentTimeMillis()) {
      MessageAndOffset message = kafkaConsumer.read();
      if (message != null) {
        String messageId = getMessageID(message);
        List<Record> records = processKafkaMessage(messageId, message.getPayload());
        for (Record record : records) {
          batchMaker.addRecord(record);
        }
        recordCounter += records.size();
      }
    }
    return lastSourceOffset;
  }

  @Override
  public void destroy() {
    kafkaConsumer.destroy();
  }

  @Override
  public void commit(String offset) throws StageException {
    kafkaConsumer.commit();
  }


}