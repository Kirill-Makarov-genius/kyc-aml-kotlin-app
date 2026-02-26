package com.study.service

import com.study.exception.KycRequestNotFoundException
import com.study.exception.KycValidationException
import com.study.model.KycRequest
import com.study.model.KycStatus
import com.study.repository.KycRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class KycService (private val repository: KycRepository){

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun createRequest(request: KycRequest): KycRequest{

        log.info("Creating new KYC request for passport: {}", maskPassport(request.passportNumber))
        val savedRequest = repository.save(request)

        // Launch async checking
        serviceScope.launch {
            performAmlCheck(savedRequest)
        }

        return savedRequest

    }

    suspend fun getAllRequests(): List<KycRequest>{
        return repository.findAll()
    }

    suspend fun getRequest(id: String): KycRequest{
        return repository.findById(id) ?: throw KycRequestNotFoundException(id)
    }


    private suspend fun checkFns(passport: String): Int {
        delay(2000)
        return if(passport.startsWith("1234")) 10 else 0
    }

    private suspend fun checkMvd(passport: String): Int {
        delay(3000)
        return if (passport.endsWith("9")) 50 else 0
    }

    private suspend fun performAmlCheck(request: KycRequest) = coroutineScope {

        println("Starting async procedures for ${request.id}")
        val startTime = System.currentTimeMillis()

        val fnsDeferred = async { checkFns(request.passportNumber) }
        val mvdDeferred = async { checkMvd(request.passportNumber) }

        val fnsScore = fnsDeferred.await()
        val mvdScore = mvdDeferred.await()

        val totalRisk = fnsScore + mvdScore

        val timeTaken = System.currentTimeMillis() - startTime
        print("Procedures finished in $timeTaken ms. Total risk: $totalRisk")

        val newStatus = if (totalRisk > 40) KycStatus.BLOCKED else KycStatus.VERIFIED
        val comment = "Checked by FNS & MVD. Score: $totalRisk"

        repository.updateRiskData(request.id,request.status, newStatus, totalRisk, comment)
    }

    private fun maskPassport(p: String): String = p.take(2) + "** ***" + p.takeLast(2)


}