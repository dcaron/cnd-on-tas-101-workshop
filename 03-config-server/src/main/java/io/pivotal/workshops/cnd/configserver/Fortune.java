package io.pivotal.workshops.cnd.configserver;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;


@Entity
public class Fortune {

    public Fortune()    {}

    public Fortune(String text) {
        this.text = text;
    }

    @Id
    @GeneratedValue
    private Long id;

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text)  {
        this.text = text;
    }
}
