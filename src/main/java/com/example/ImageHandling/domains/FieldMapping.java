package com.example.ImageHandling.domains;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldMapping {
    private String name;

    private String key;

    private String regex;

    private String position;

    private String example;


    @Override
    public String toString() {
        return "FieldMapping{" +
                "name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", regex='" + regex + '\'' +
                ", position='" + position + '\'' +
                ", example='" + example + '\'' +
                '}';
    }

}
