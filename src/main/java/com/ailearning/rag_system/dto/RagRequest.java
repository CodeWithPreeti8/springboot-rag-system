package com.ailearning.rag_system.dto;

public class RagRequest {
	
	private String question;
	
	public RagRequest() {
		
	}
	public RagRequest(String question) {
		super();
		this.question = question;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	@Override
	public String toString() {
		return "RagRequest [question=" + question + "]";
	}
	
	
}
