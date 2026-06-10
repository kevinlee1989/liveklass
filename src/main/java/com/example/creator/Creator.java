package com.example.creator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Creator {

    private String id;
    private String name;

    public static Creator of(String id, String name) {
        Creator creator = new Creator();
        creator.id = id;
        creator.name = name;
        return creator;
    }
}
