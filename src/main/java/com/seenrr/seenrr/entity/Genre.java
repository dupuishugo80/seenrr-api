package com.seenrr.seenrr.entity;

import java.util.List;

import jakarta.persistence.*;

@Entity
public class Genre {

    @Id
    private int id;

    private String name;

    @OneToMany(mappedBy = "genre")
    private List<LinkMediaGenre> mediaGenres;
    
    public Genre() {
    }

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}