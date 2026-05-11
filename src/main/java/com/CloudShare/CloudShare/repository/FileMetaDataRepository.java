package com.CloudShare.CloudShare.repository;

import com.CloudShare.CloudShare.document.FileMetaDetaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMetaDataRepository extends MongoRepository<FileMetaDetaDocument,String> {
    List<FileMetaDetaDocument> findByClerkId(String clerkId);
   Long countByClerkId(String clerkId);
}
