package com.study.service

import com.study.model.KycAuditModel
import com.study.model.KycRequestWithHistory
import com.study.repository.KycAuditRepository
import org.slf4j.LoggerFactory

class KycAuditService(private val repository: KycAuditRepository) {



    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun getRequestAndLogsById(requestId: String ): KycRequestWithHistory?{
        log.info("Create request for find all logs for one request - $requestId")
        return repository.findRequestWithHistory(requestId)
    }

    suspend fun getAllRequestsWithLogs(): List<KycRequestWithHistory>{
        log.info("Create request for all requests with their history")
        return repository.findAllRequestsWithHistory()
    }


}