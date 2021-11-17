package com.microservice.transaction.repositories;

import com.microservice.transaction.entities.Transaction;
import com.microservice.transaction.entities.dtos.CreateTransactionDto;
import com.microservice.transaction.entities.dtos.ResponseTransactionDto;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITransactionRepository extends MongoRepository<Transaction, ObjectId> {
    List<Transaction> findByAccountId(ObjectId accountId);
}
