package com.ailearning.rag_system.dto;

public class RagResponse {
	private String question;
	private String answer;
	private String sourceDocument;
	private Long DocumentId;
	private Long timeStamp;
	
	public RagResponse() {
		
	}
	public RagResponse(String question, String answer, String sourceDocument, Long documentId, Long timeStamp) {
		super();
		this.question = question;
		this.answer = answer;
		this.sourceDocument = sourceDocument;
		DocumentId = documentId;
		this.timeStamp = timeStamp;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getSourceDocument() {
		return sourceDocument;
	}
	public void setSourceDocument(String sourceDocument) {
		this.sourceDocument = sourceDocument;
	}
	public Long getDocumentId() {
		return DocumentId;
	}
	public void setDocumentId(Long documentId) {
		DocumentId = documentId;
	}
	public Long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	@Override
	public String toString() {
		return "RagResponse [question=" + question + ", answer=" + answer + ", sourceDocument=" + sourceDocument
				+ ", DocumentId=" + DocumentId + ", timeStamp=" + timeStamp + "]";
	}
	
	

}
