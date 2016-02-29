package beans;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by willpride on 1/20/16.
 */
public class AnswerQuestionRequestBean extends SessionBean {
    private String formIndex;
    // This can be an array (multi select, geo point), integer, date, or String.
    // Even though they always come in as Strings, Jackson will try to parse the String into the above classes
    // and so needs this to be an Object to store them in
    private Object answer;

    // our JSON-Object mapping lib (Jackson) requires a default constructor
    public AnswerQuestionRequestBean(){}

    public AnswerQuestionRequestBean(String formIndex, String answer, String sessionId) {
        this.formIndex = formIndex;
        this.answer = answer;
        this.sessionId = sessionId;
    }

    @JsonGetter(value = "ix")
    public String getFormIndex() {
        return formIndex;
    }
    @JsonSetter(value = "ix")
    public void setFormIndex(String formIndex) {
        this.formIndex = formIndex;
    }
    @JsonGetter(value = "answer")
    public Object getAnswer() {
        return answer;
    }
    @JsonSetter(value = "answer")
    public void setAnswer(Object answer) {
        this.answer = answer;
    }

    @Override
    public String toString(){
        return "Answer Question Bean [formIndex: " + formIndex + ", answer: " + answer + ", sessionId: " + sessionId + "]";
    }
}
