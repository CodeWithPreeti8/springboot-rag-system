package com.ailearning.rag_system.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "documents")
public class Document {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable= false,length= 255)
	private String title;
	
	@Column(nullable= false, columnDefinition= "Text")
	private String content;
	
	@Column(columnDefinition= "Text")
	private String embedding;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime cretaedAt;
	
	public Document() {
		
	}

	public Document(Long id, String title, String content, String embedding, LocalDateTime cretaedAt) {
		super();
		this.id = id;
		this.title = title;
		this.content = content;
		this.embedding = embedding;
		this.cretaedAt = cretaedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEmbedding() {
		return embedding;
	}

	public void setEmbedding(String embedding) {
		this.embedding = embedding;
	}

	public LocalDateTime getCretaedAt() {
		return cretaedAt;
	}

	public void setCretaedAt(LocalDateTime cretaedAt) {
		this.cretaedAt = cretaedAt;
	}

	@Override
	public String toString() {
		return "Document [id=" + id + ", title=" + title + ", content=" + content + ", embedding=" + embedding
				+ ", cretaedAt=" + cretaedAt + "]";
	}
	
	

}

