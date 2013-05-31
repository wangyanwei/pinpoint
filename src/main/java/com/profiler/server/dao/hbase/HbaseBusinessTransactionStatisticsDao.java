package com.profiler.server.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.BUSINESS_TRANSACTION_STATISTICS;
import static com.nhn.pinpoint.common.hbase.HBaseTables.BUSINESS_TRANSACTION_STATISTICS_CF_ERROR;
import static com.nhn.pinpoint.common.hbase.HBaseTables.BUSINESS_TRANSACTION_STATISTICS_CF_NORMAL;

import com.profiler.server.util.AcceptedTimeService;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.dto2.thrift.Span;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.profiler.server.dao.BusinessTransactionStatisticsDao;

/**
 * @author netspider
 */
@Deprecated
public class HbaseBusinessTransactionStatisticsDao implements BusinessTransactionStatisticsDao {

	@Autowired
	private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService ;

	@Override
	public void update(Span span) {

        final long acceptedTime = acceptedTimeService.getAcceptedTime();

        long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);
        byte[] rowTimeSlotBytes = Bytes.toBytes(rowTimeSlot);

        byte[] rowKey = BytesUtils.merge(BytesUtils.toFixedLengthBytes(span.getApplicationName(), 24), rowTimeSlotBytes);
		byte[] cf;
        if (span.getErr() == 0) {
            // 에러 없음.
            cf = BUSINESS_TRANSACTION_STATISTICS_CF_NORMAL;
        } else {
            // 에러 있음.
            cf = BUSINESS_TRANSACTION_STATISTICS_CF_ERROR;
        }
        byte[] columnName = Bytes.toBytes(span.getRpc()); // application url

		hbaseTemplate.incrementColumnValue(BUSINESS_TRANSACTION_STATISTICS, rowKey, cf, columnName, 1L);
	}
}