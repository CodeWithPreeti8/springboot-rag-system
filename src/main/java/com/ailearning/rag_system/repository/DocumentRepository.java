package com.ailearning.rag_system.repository;

import org.springframework.stereotype.Repository;
import com.ailearning.rag_system.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
	
	

}
