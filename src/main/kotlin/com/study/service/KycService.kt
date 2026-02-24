package com.study.service

import com.study.model.KycRequest
import com.study.model.KycStatus
import com.study.repository.KycRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KycService (private val repository: KycRepository){

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun createRequest(request: KycRequest): KycRequest{

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

    suspend fun getRequest(id: String): KycRequest?{
        return repository.findById(id)
    }


    private suspend fun performAmlCheck(request: KycRequest){
        delay(5000)

        val newStatus = if (request.passportNumber.endsWith("9")){
            KycStatus.BLOCKED
        } else{
            KycStatus.VERIFIED
        }

        println("AML Check finished for ${request.id}. Result: ${newStatus}")

        repository.updateStatus(request.id, newStatus)

    }

}