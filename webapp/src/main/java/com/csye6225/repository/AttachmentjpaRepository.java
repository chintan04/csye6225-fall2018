package com.csye6225.repository;

import com.csye6225.model.Attachment;
import com.csye6225.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttachmentjpaRepository extends JpaRepository<Attachment, UUID> {
}
